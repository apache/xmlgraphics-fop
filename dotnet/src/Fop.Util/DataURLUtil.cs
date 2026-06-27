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

using System.IO;
using System.Text;

namespace Fop.Util;

/// <summary>
/// Utility class for generating RFC 2397 "data:" URLs.
/// <para>
/// The Java <c>org.apache.fop.util.DataURLUtil</c> is a deprecated wrapper that delegates to
/// <c>org.apache.xmlgraphics.util.uri.DataURLUtil</c>. Since xmlgraphics-commons is not part of this
/// port, the self-contained encoding logic of that class is folded directly into this type.
/// </para>
/// <para>
/// The produced URLs always use base64 encoding, of the form
/// <c>data:[&lt;mediatype&gt;];base64,&lt;base64-data&gt;</c>. When the media type is <c>null</c> or
/// empty it is omitted, matching the upstream behaviour.
/// </para>
/// </summary>
public static class DataURLUtil
{
    /// <summary>
    /// Generates a "data:" URL from the given input stream.
    /// </summary>
    /// <param name="input">an input stream to read the payload from.</param>
    /// <param name="mediatype">a MIME media type (may be <c>null</c> or empty).</param>
    /// <returns>a "data:" URL as a string.</returns>
    public static string CreateDataUrl(Stream input, string? mediatype)
    {
        ArgumentNullException.ThrowIfNull(input);
        StringBuilder sb = new();
        using (StringWriter writer = new(sb))
        {
            WriteDataUrl(input, mediatype, writer);
        }

        return sb.ToString();
    }

    /// <summary>
    /// Generates a "data:" URL from the given input stream and writes it to the given writer.
    /// </summary>
    /// <param name="input">an input stream to read the payload from.</param>
    /// <param name="mediatype">a MIME media type (may be <c>null</c> or empty).</param>
    /// <param name="writer">the writer to which the "data:" URL is written.</param>
    public static void WriteDataUrl(Stream input, string? mediatype, TextWriter writer)
    {
        ArgumentNullException.ThrowIfNull(input);
        ArgumentNullException.ThrowIfNull(writer);

        writer.Write("data:");
        if (!string.IsNullOrEmpty(mediatype))
        {
            writer.Write(mediatype);
        }

        writer.Write(";base64,");

        // System.Convert handles the base64 encoding (RFC 4648) the same way the
        // commons-codec Base64 stream used upstream does for the standard alphabet.
        using MemoryStream buffer = new();
        input.CopyTo(buffer);
        writer.Write(Convert.ToBase64String(buffer.GetBuffer(), 0, (int)buffer.Length));
    }
}
