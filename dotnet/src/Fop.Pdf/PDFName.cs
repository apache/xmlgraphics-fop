using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A PDF name object (e.g. <c>/Type</c>).
/// </summary>
public class PDFName : PDFObject
{
    private const string EscapedNameChars = "/()<>[]%#";

    private static readonly char[] Digits =
        ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'];

    private readonly string _name;

    /// <summary>Creates a new PDF name object.</summary>
    /// <param name="name">The name value (without or with a leading slash).</param>
    public PDFName(string name)
    {
        ArgumentNullException.ThrowIfNull(name);
        _name = EscapeName(name);
    }

    /// <summary>
    /// Escapes a PDF name: adds the leading slash and escapes characters as necessary. A leading
    /// slash already present on the input is not duplicated.
    /// </summary>
    public static string EscapeName(string name)
    {
        ArgumentNullException.ThrowIfNull(name);
        var sb = new StringBuilder(Math.Min(16, name.Length + 4));
        bool skipFirst = name.StartsWith('/');
        sb.Append('/');

        name = name[(skipFirst ? 1 : 0)..];

        byte[] nameBytes = Encoding.UTF8.GetBytes(name);
        foreach (byte nameByte in nameBytes)
        {
            int currentChar = nameByte & 255;
            if (currentChar < 33 || currentChar > 126 || EscapedNameChars.IndexOf((char)currentChar) >= 0)
            {
                ToHex(currentChar, sb);
            }
            else
            {
                sb.Append((char)nameByte);
            }
        }
        return sb.ToString();
    }

    private static void ToHex(int currentChar, StringBuilder sb)
    {
        sb.Append('#');
        sb.Append(Digits[(currentChar >>> 4) & 0x0F]);
        sb.Append(Digits[currentChar & 0x0F]);
    }

    /// <inheritdoc/>
    public override string ToString() => _name;

    /// <summary>Returns the name without the leading slash.</summary>
    public string Name => _name[1..];

    /// <inheritdoc/>
    public override bool Equals(object? obj) => obj is PDFName other && _name == other._name;

    /// <inheritdoc/>
    public override int GetHashCode() => _name.GetHashCode(StringComparison.Ordinal);

    /// <inheritdoc/>
    public override int Output(Stream output)
    {
        var textBuffer = new StringBuilder(64);
        textBuffer.Append(ToString());
        int count = PdfEncoding.Encoding.GetByteCount(textBuffer.ToString());
        FlushTextBuffer(textBuffer, output);
        return count;
    }

    /// <inheritdoc/>
    public override void OutputInline(Stream? output, StringBuilder textBuffer)
    {
        if (HasObjectNumber)
        {
            textBuffer.Append(ReferencePDF());
        }
        else
        {
            textBuffer.Append(ToString());
        }
    }
}
