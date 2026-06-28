// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Globalization;
using System.Text;

namespace Fop.Render.Pdf.Native;

/// <summary>
/// A minimal writer for a PDF file's physical structure: indirect objects (reserved by number and
/// filled in any order), the cross-reference table and the trailer. Object bodies are PDF source
/// fragments (dictionaries, streams) encoded in Latin-1, which is the byte-faithful encoding the PDF
/// content uses. Object 1 is taken to be the document catalog (the <c>/Root</c>).
/// </summary>
internal sealed class PdfFile
{
    private readonly List<byte[]?> bodies = new() { null }; // index 0 unused (objects are 1-based)

    /// <summary>The object number of the <c>/Encrypt</c> dictionary, added to the trailer when set.</summary>
    public int? EncryptReference { get; set; }

    /// <summary>The hex file id emitted as the trailer <c>/ID</c> (both array entries), when set.</summary>
    public string? FileIdHex { get; set; }

    /// <summary>Reserves the next object number (its body is supplied later via <c>Write</c>).</summary>
    public int Reserve()
    {
        bodies.Add(null);
        return bodies.Count - 1;
    }

    /// <summary>Sets the body of a previously reserved object (the inner content, without the obj wrapper).</summary>
    public void Write(int number, string body) => Write(number, Encoding.Latin1.GetBytes(body));

    /// <summary>Sets the body of a previously reserved object to raw bytes (e.g. a stream object).</summary>
    public void Write(int number, byte[] body)
    {
        if (number <= 0 || number >= bodies.Count)
        {
            throw new ArgumentOutOfRangeException(nameof(number));
        }

        bodies[number] = body;
    }

    /// <summary>
    /// Builds a stream object body: the stream dictionary entries in <paramref name="dictEntries"/>
    /// (without the enclosing <c>&lt;&lt; &gt;&gt;</c>), an automatically-added <c>/Length</c>, and the
    /// raw <paramref name="data"/> between <c>stream</c>/<c>endstream</c>.
    /// </summary>
    public static byte[] StreamObject(string dictEntries, byte[] data)
    {
        byte[] head = Encoding.Latin1.GetBytes($"<< {dictEntries} /Length {data.Length} >>\nstream\n");
        byte[] tail = Encoding.Latin1.GetBytes("\nendstream");
        var result = new byte[head.Length + data.Length + tail.Length];
        Buffer.BlockCopy(head, 0, result, 0, head.Length);
        Buffer.BlockCopy(data, 0, result, head.Length, data.Length);
        Buffer.BlockCopy(tail, 0, result, head.Length + data.Length, tail.Length);
        return result;
    }

    /// <summary>Serializes the whole document to <paramref name="output"/>.</summary>
    public void WriteTo(Stream output)
    {
        var buffer = new MemoryStream();

        void Emit(string s) => buffer.Write(Encoding.Latin1.GetBytes(s));

        // Header with a binary marker comment so tools treat the file as binary.
        Emit("%PDF-1.7\n");
        buffer.Write([(byte)'%', 0xE2, 0xE3, 0xCF, 0xD3, (byte)'\n']);

        int count = bodies.Count - 1; // highest object number
        var offsets = new long[bodies.Count];
        for (int n = 1; n < bodies.Count; n++)
        {
            offsets[n] = buffer.Position;
            Emit($"{n} 0 obj\n");
            buffer.Write(bodies[n] ?? Encoding.Latin1.GetBytes("null")); // unwritten -> null object
            Emit("\nendobj\n");
        }

        long xrefOffset = buffer.Position;
        Emit($"xref\n0 {count + 1}\n");
        Emit("0000000000 65535 f \n");
        for (int n = 1; n <= count; n++)
        {
            Emit(offsets[n].ToString("D10", CultureInfo.InvariantCulture) + " 00000 n \n");
        }

        string encrypt = EncryptReference is int e ? $" /Encrypt {e} 0 R" : string.Empty;
        string id = FileIdHex is { } fid ? $" /ID [<{fid}> <{fid}>]" : string.Empty;
        Emit($"trailer\n<< /Size {count + 1} /Root 1 0 R{encrypt}{id} >>\n");
        Emit($"startxref\n{xrefOffset.ToString(CultureInfo.InvariantCulture)}\n%%EOF\n");

        buffer.Position = 0;
        buffer.CopyTo(output);
    }
}
