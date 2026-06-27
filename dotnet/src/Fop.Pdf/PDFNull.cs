using System.Text;

namespace Fop.Pdf;

/// <summary>
/// The PDF <c>null</c> object.
/// </summary>
public sealed class PDFNull : IPDFWritable
{
    /// <summary>The singleton instance for the "null" object.</summary>
    public static readonly PDFNull Instance = new();

    private PDFNull()
    {
    }

    /// <inheritdoc/>
    public override string ToString() => "null";

    /// <inheritdoc/>
    public void OutputInline(Stream? output, StringBuilder textBuffer) => textBuffer.Append(ToString());
}
