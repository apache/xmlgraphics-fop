using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A PDF text object plus the static helpers FOP uses to escape and encode strings/text for PDF
/// output.
/// </summary>
public class PDFText : PDFObject
{
    private static readonly char[] Digits =
        ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'];

    private string? _text;

    /// <summary>The wrapped text.</summary>
    public string? Text
    {
        get => _text;
        set => _text = value;
    }

    /// <inheritdoc/>
    protected override string ToPDFString()
    {
        if (Text is null)
        {
            throw new ArgumentException("The text of this PDFText must not be empty");
        }
        var sb = new StringBuilder(64);
        sb.Append('(');
        sb.Append(EscapeText(Text));
        sb.Append(')');
        return sb.ToString();
    }

    /// <summary>Escapes text (see 4.4.1 in PDF 1.3 specs).</summary>
    public static string EscapeText(string text) => EscapeText(text, false);

    /// <summary>Escapes text (see 4.4.1 in PDF 1.3 specs).</summary>
    /// <param name="text">The text to encode.</param>
    /// <param name="forceHexMode">True if the output should follow the hex encoding rules.</param>
    public static string EscapeText(string? text, bool forceHexMode)
    {
        if (!string.IsNullOrEmpty(text))
        {
            bool unicode = false;
            bool hexMode = false;
            if (forceHexMode)
            {
                hexMode = true;
            }
            else
            {
                for (int i = 0, c = text.Length; i < c; i++)
                {
                    if (text[i] >= 128)
                    {
                        unicode = true;
                        hexMode = true;
                        break;
                    }
                }
            }

            if (hexMode)
            {
                byte[] uniBytes = Utf16WithBom(text);
                return ToHex(uniBytes);
            }
            else
            {
                var result = new StringBuilder(text.Length * 2);
                result.Append('(');
                int l = text.Length;

                if (unicode)
                {
                    // byte order marker (0xfeff)
                    result.Append("\\376\\377");

                    for (int i = 0; i < l; i++)
                    {
                        char ch = text[i];
                        int high = (ch & 0xff00) >>> 8;
                        int low = ch & 0xff;
                        result.Append('\\');
                        result.Append(ToOctal(high));
                        result.Append('\\');
                        result.Append(ToOctal(low));
                    }
                }
                else
                {
                    for (int i = 0; i < l; i++)
                    {
                        char ch = text[i];
                        if (ch < 256)
                        {
                            EscapeStringChar(ch, result);
                        }
                        else
                        {
                            throw new InvalidOperationException(
                                "Can only treat text in 8-bit ASCII/PDFEncoding");
                        }
                    }
                }
                result.Append(')');
                return result.ToString();
            }
        }
        return "()";
    }

    /// <summary>Converts a byte array to a hexadecimal string (3.2.3 in PDF 1.4 specs).</summary>
    /// <param name="data">The data to encode.</param>
    /// <param name="brackets">True if enclosing angle brackets should be included.</param>
    public static string ToHex(byte[] data, bool brackets)
    {
        var sb = new StringBuilder(data.Length * 2);
        if (brackets)
        {
            sb.Append('<');
        }
        foreach (byte aData in data)
        {
            sb.Append(Digits[(aData >>> 4) & 0x0F]);
            sb.Append(Digits[aData & 0x0F]);
        }
        if (brackets)
        {
            sb.Append('>');
        }
        return sb.ToString();
    }

    /// <summary>Converts a byte array to a hexadecimal string with enclosing brackets.</summary>
    public static string ToHex(byte[] data) => ToHex(data, true);

    /// <summary>Converts a string to UTF-16 big-endian (with BOM), matching Java's UnicodeBig.</summary>
    public static byte[] ToUTF16(string text) => Utf16WithBom(text);

    /// <summary>Converts a char to a multi-byte (UTF-16BE) hexadecimal representation.</summary>
    public static string ToUnicodeHex(char c)
    {
        var buf = new StringBuilder(4);
        byte[] uniBytes = Encoding.BigEndianUnicode.GetBytes([c]);
        foreach (byte uniByte in uniBytes)
        {
            buf.Append(Digits[(uniByte >>> 4) & 0x0F]);
            buf.Append(Digits[uniByte & 0x0F]);
        }
        return buf.ToString();
    }

    /// <summary>
    /// Converts a code point to a multi-byte hex representation appending to a string buffer.
    /// The created string is a 4-character string for a BMP character and 6-character for non-BMP.
    /// </summary>
    public static void ToUnicodeHex(int c, StringBuilder sb)
    {
        // CharUtilities.isBmpCodePoint(c) inlined: a BMP code point fits in the basic plane.
        if (c <= 0xFFFF)
        {
            sb.Append((c + 0x10000).ToString("X", CultureInfo.InvariantCulture)[1..]);
        }
        else
        {
            sb.Append((c + 0x1000000).ToString("X", CultureInfo.InvariantCulture)[1..]);
        }
    }

    /// <summary>Escapes a string as described in section 4.4 of the PDF 1.3 specs.</summary>
    public static string EscapeString(string? s)
    {
        if (string.IsNullOrEmpty(s))
        {
            return "()";
        }
        var sb = new StringBuilder(64);
        sb.Append('(');
        for (int i = 0; i < s.Length; i++)
        {
            EscapeStringChar(s[i], sb);
        }
        sb.Append(')');
        return sb.ToString();
    }

    /// <summary>
    /// Escapes a character conforming to the rules established in the PostScript Language
    /// Reference ("Literal Text Strings").
    /// </summary>
    public static void EscapeStringChar(char c, StringBuilder target)
    {
        if (c > 127)
        {
            target.Append('\\');
            target.Append(ToOctal(c));
        }
        else
        {
            switch (c)
            {
                case '\n': target.Append("\\n"); break;
                case '\r': target.Append("\\r"); break;
                case '\t': target.Append("\\t"); break;
                case '\b': target.Append("\\b"); break;
                case '\f': target.Append("\\f"); break;
                case '\\': target.Append("\\\\"); break;
                case '(': target.Append("\\("); break;
                case ')': target.Append("\\)"); break;
                default: target.Append(c); break;
            }
        }
    }

    /// <summary>Escapes a byte array for output to PDF (used for encrypted strings).</summary>
    public static byte[] EscapeByteArray(byte[] data)
    {
        using var bout = new MemoryStream(data.Length);
        bout.WriteByte((byte)'(');
        foreach (byte b in data)
        {
            switch ((sbyte)b)
            {
                case (sbyte)'\n': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'n'); break;
                case (sbyte)'\r': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'r'); break;
                case (sbyte)'\t': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'t'); break;
                case (sbyte)'\b': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'b'); break;
                case (sbyte)'\f': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'f'); break;
                case (sbyte)'\\': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'\\'); break;
                case (sbyte)'(': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)'('); break;
                case (sbyte)')': bout.WriteByte((byte)'\\'); bout.WriteByte((byte)')'); break;
                default: bout.WriteByte(b); break;
            }
        }
        bout.WriteByte((byte)')');
        return bout.ToArray();
    }

    /// <summary>
    /// Converts text to PDF's "string" data type. Unsupported (non-US-ASCII) characters are
    /// converted to '?'.
    /// </summary>
    public static string ToPDFString(string text) => ToPDFString(text, '?');

    /// <summary>
    /// Converts text to PDF's "string" data type. Unsupported characters get converted to the
    /// given replacement character. The conversion restricts "string" to US-ASCII.
    /// </summary>
    public static string ToPDFString(string text, char replacement)
    {
        var sb = new StringBuilder();
        for (int i = 0, c = text.Length; i < c; i++)
        {
            char ch = text[i];
            sb.Append(ch > 127 ? replacement : ch);
        }
        return sb.ToString();
    }

    // Java's StandardCharsets.UTF_16 encodes big-endian with a leading BOM (0xFE 0xFF).
    private static byte[] Utf16WithBom(string text)
    {
        byte[] body = Encoding.BigEndianUnicode.GetBytes(text);
        byte[] result = new byte[body.Length + 2];
        result[0] = 0xFE;
        result[1] = 0xFF;
        Array.Copy(body, 0, result, 2, body.Length);
        return result;
    }

    // Java Integer.toOctalString: unsigned octal, no leading zeros. Values passed here are always
    // small (a byte 0..255 or a 16-bit char half), but we treat them as unsigned 32-bit to match.
    private static string ToOctal(int value)
    {
        if (value == 0)
        {
            return "0";
        }
        uint u = unchecked((uint)value);
        var sb = new StringBuilder();
        while (u != 0)
        {
            sb.Insert(0, (char)('0' + (int)(u & 7)));
            u >>= 3;
        }
        return sb.ToString();
    }
}
