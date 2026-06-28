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

using System.Security.Cryptography;
using System.Text;

namespace Fop.Render.Pdf.Native;

/// <summary>
/// Options for encrypting a generated PDF with the standard security handler: optional owner/user
/// passwords (empty means "open without a password") and the permission flags a conforming viewer
/// enforces. Passing this to the native renderer turns on 128-bit RC4 encryption.
/// </summary>
/// <param name="OwnerPassword">The owner password (full access); empty for none.</param>
/// <param name="UserPassword">The user password required to open the document; empty for none.</param>
/// <param name="AllowPrinting">Whether printing is permitted.</param>
/// <param name="AllowCopying">Whether text/graphics extraction is permitted.</param>
/// <param name="AllowModifying">Whether modifying the document is permitted.</param>
/// <param name="AllowAnnotating">Whether adding/modifying annotations is permitted.</param>
public sealed record PdfEncryptionOptions(
    string OwnerPassword = "",
    string UserPassword = "",
    bool AllowPrinting = true,
    bool AllowCopying = true,
    bool AllowModifying = true,
    bool AllowAnnotating = true);

/// <summary>Encrypts an indirect object's strings and streams under its per-object key.</summary>
internal interface IObjectEncryptor
{
    /// <summary>Encrypts a stream's payload for the object numbered <paramref name="objectNumber"/>.</summary>
    byte[] EncryptStream(int objectNumber, byte[] data);

    /// <summary>Renders <paramref name="text"/> as a PDF string token for object <paramref name="objectNumber"/>.</summary>
    string LiteralString(int objectNumber, string text);
}

/// <summary>The no-op encryptor used when encryption is off: streams pass through, strings are literal.</summary>
internal sealed class NoEncryption : IObjectEncryptor
{
    public static readonly NoEncryption Instance = new();

    public byte[] EncryptStream(int objectNumber, byte[] data) => data;

    public string LiteralString(int objectNumber, string text) => StandardSecurityHandler.EscapeLiteral(text);
}

/// <summary>
/// The PDF "Standard" security handler with 128-bit RC4 (<c>/V 2 /R 3</c>), the classic PDF encryption
/// (Algorithms 3.1-3.5 of the PDF spec). Computes the owner (<c>/O</c>) and user (<c>/U</c>) entries
/// and the document key from the passwords, permissions and file id, then encrypts each string and
/// stream under a per-object RC4 key. An empty user password lets any reader open the file while still
/// enforcing the permission flags.
/// </summary>
internal sealed class StandardSecurityHandler : IObjectEncryptor
{
    private const int KeyLengthBytes = 16; // 128-bit

    // The fixed 32-byte password padding from the PDF spec.
    private static readonly byte[] Padding =
    [
        0x28, 0xBF, 0x4E, 0x5E, 0x4E, 0x75, 0x8A, 0x41, 0x64, 0x00, 0x4E, 0x56, 0xFF, 0xFA, 0x01, 0x08,
        0x2E, 0x2E, 0x00, 0xB6, 0xD0, 0x68, 0x3E, 0x80, 0x2F, 0x0C, 0xA9, 0xFE, 0x64, 0x53, 0x69, 0x7A,
    ];

    private readonly byte[] key;

    public StandardSecurityHandler(PdfEncryptionOptions options, byte[] fileId)
    {
        Permissions = BuildPermissions(options);
        byte[] o = ComputeOwnerEntry(options);
        byte[] userPad = Pad(options.UserPassword);
        key = ComputeKey(userPad, o, Permissions, fileId);
        OwnerEntry = o;
        UserEntry = ComputeUserEntry(key, fileId);
    }

    /// <summary>The 32-byte owner entry (<c>/O</c>).</summary>
    public byte[] OwnerEntry { get; }

    /// <summary>The 32-byte user entry (<c>/U</c>).</summary>
    public byte[] UserEntry { get; }

    /// <summary>The permission bit field (<c>/P</c>).</summary>
    public int Permissions { get; }

    /// <summary>The <c>/Encrypt</c> dictionary entries (without the enclosing <c>&lt;&lt; &gt;&gt;</c>).</summary>
    public string EncryptDictionary() =>
        $"/Filter /Standard /V 2 /R 3 /Length 128 /P {Permissions} " +
        $"/O <{Hex(OwnerEntry)}> /U <{Hex(UserEntry)}>";

    public byte[] EncryptStream(int objectNumber, byte[] data) => Rc4(ObjectKey(objectNumber), data);

    public string LiteralString(int objectNumber, string text)
    {
        byte[] raw = Encoding.Latin1.GetBytes(Sanitize(text));
        return $"<{Hex(Rc4(ObjectKey(objectNumber), raw))}>";
    }

    /// <summary>The per-object RC4 key: MD5(docKey + obj[3] + gen[2]) truncated to keyLen+5 (max 16).</summary>
    private byte[] ObjectKey(int objectNumber)
    {
        Span<byte> input = stackalloc byte[KeyLengthBytes + 5];
        key.CopyTo(input);
        input[KeyLengthBytes] = (byte)objectNumber;
        input[KeyLengthBytes + 1] = (byte)(objectNumber >> 8);
        input[KeyLengthBytes + 2] = (byte)(objectNumber >> 16);
        input[KeyLengthBytes + 3] = 0; // generation low
        input[KeyLengthBytes + 4] = 0; // generation high
        byte[] digest = MD5.HashData(input);
        int n = Math.Min(KeyLengthBytes + 5, 16);
        return digest[..n];
    }

    private static byte[] ComputeKey(byte[] userPad, byte[] ownerEntry, int permissions, byte[] fileId)
    {
        using var md5 = IncrementalHash.CreateHash(HashAlgorithmName.MD5);
        md5.AppendData(userPad);
        md5.AppendData(ownerEntry);
        md5.AppendData([(byte)permissions, (byte)(permissions >> 8), (byte)(permissions >> 16), (byte)(permissions >> 24)]);
        md5.AppendData(fileId);
        byte[] digest = md5.GetHashAndReset();

        // R >= 3: re-hash the first keyLen bytes 50 times.
        for (int i = 0; i < 50; i++)
        {
            digest = MD5.HashData(digest.AsSpan(0, KeyLengthBytes));
        }

        return digest[..KeyLengthBytes];
    }

    private static byte[] ComputeOwnerEntry(PdfEncryptionOptions options)
    {
        // Owner password defaults to the user password when unset.
        string ownerPwd = string.IsNullOrEmpty(options.OwnerPassword) ? options.UserPassword : options.OwnerPassword;
        byte[] digest = MD5.HashData(Pad(ownerPwd));
        for (int i = 0; i < 50; i++) // R >= 3
        {
            digest = MD5.HashData(digest.AsSpan(0, KeyLengthBytes));
        }

        byte[] rc4Key = digest[..KeyLengthBytes];
        byte[] result = Rc4(rc4Key, Pad(options.UserPassword));
        for (int i = 1; i <= 19; i++) // R >= 3
        {
            result = Rc4(XorKey(rc4Key, i), result);
        }

        return result;
    }

    private static byte[] ComputeUserEntry(byte[] key, byte[] fileId)
    {
        using var md5 = IncrementalHash.CreateHash(HashAlgorithmName.MD5);
        md5.AppendData(Padding);
        md5.AppendData(fileId);
        byte[] hash = md5.GetHashAndReset();

        byte[] result = Rc4(key, hash);
        for (int i = 1; i <= 19; i++)
        {
            result = Rc4(XorKey(key, i), result);
        }

        // R >= 3: the 16-byte result, padded out to 32 bytes (the trailing 16 are arbitrary).
        var u = new byte[32];
        result.AsSpan(0, 16).CopyTo(u);
        return u;
    }

    private static int BuildPermissions(PdfEncryptionOptions o)
    {
        // Bits 7,8 and 13..32 are reserved-1; bits 1,2 reserved-0; bits 3..6 and 9..12 are the flags.
        int p = unchecked((int)0xFFFFF0C0);
        if (o.AllowPrinting) { p |= 1 << 2 | 1 << 11; }    // print + high-quality print
        if (o.AllowModifying) { p |= 1 << 3 | 1 << 10; }   // modify + assemble
        if (o.AllowCopying) { p |= 1 << 4 | 1 << 9; }      // copy/extract + accessibility extract
        if (o.AllowAnnotating) { p |= 1 << 5 | 1 << 8; }   // annotate + fill forms
        return p;
    }

    private static byte[] Pad(string password)
    {
        byte[] pwd = Encoding.Latin1.GetBytes(password ?? string.Empty);
        var result = new byte[32];
        int n = Math.Min(pwd.Length, 32);
        Array.Copy(pwd, result, n);
        Array.Copy(Padding, 0, result, n, 32 - n);
        return result;
    }

    private static byte[] XorKey(byte[] k, int value)
    {
        var result = new byte[k.Length];
        for (int i = 0; i < k.Length; i++)
        {
            result[i] = (byte)(k[i] ^ value);
        }

        return result;
    }

    /// <summary>The RC4 stream cipher (symmetric: the same routine encrypts and decrypts).</summary>
    internal static byte[] Rc4(byte[] key, byte[] data)
    {
        var s = new byte[256];
        for (int i = 0; i < 256; i++)
        {
            s[i] = (byte)i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++)
        {
            j = (j + s[i] + key[i % key.Length]) & 0xFF;
            (s[i], s[j]) = (s[j], s[i]);
        }

        var output = new byte[data.Length];
        int a = 0, b = 0;
        for (int n = 0; n < data.Length; n++)
        {
            a = (a + 1) & 0xFF;
            b = (b + s[a]) & 0xFF;
            (s[a], s[b]) = (s[b], s[a]);
            output[n] = (byte)(data[n] ^ s[(s[a] + s[b]) & 0xFF]);
        }

        return output;
    }

    private static string Hex(byte[] data)
    {
        var sb = new StringBuilder(data.Length * 2);
        foreach (byte b in data)
        {
            sb.Append(b.ToString("X2"));
        }

        return sb.ToString();
    }

    /// <summary>Maps a string to a Latin-1-safe byte sequence (non-Latin-1 characters become '?').</summary>
    private static string Sanitize(string text)
    {
        var sb = new StringBuilder(text.Length);
        foreach (char c in text)
        {
            sb.Append(c <= 0xFF ? c : '?');
        }

        return sb.ToString();
    }

    /// <summary>Escapes a string as a plain PDF literal <c>(...)</c> (used when encryption is off).</summary>
    public static string EscapeLiteral(string text)
    {
        var sb = new StringBuilder(text.Length + 2);
        sb.Append('(');
        foreach (char ch in text)
        {
            switch (ch)
            {
                case '(': sb.Append("\\("); break;
                case ')': sb.Append("\\)"); break;
                case '\\': sb.Append("\\\\"); break;
                case '\r': sb.Append("\\r"); break;
                case '\n': sb.Append("\\n"); break;
                case '\t': sb.Append("\\t"); break;
                default: sb.Append(ch is >= ' ' and <= (char)255 ? ch : '?'); break;
            }
        }

        sb.Append(')');
        return sb.ToString();
    }
}
