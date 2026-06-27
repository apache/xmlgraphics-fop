using System.Text;

namespace Fop.Pdf;

/// <summary>
/// Central character encoding for PDF command/text output.
/// </summary>
/// <remarks>
/// Mirrors <c>PDFDocument.ENCODING = "ISO-8859-1"</c> and the static
/// <c>PDFDocument.encode(String)</c> / <c>PDFDocument.flushTextBuffer(...)</c> helpers from
/// Apache FOP. Kept here (rather than on a full <c>PDFDocument</c> port) so the object model is
/// self-contained.
/// </remarks>
internal static class PdfEncoding
{
    /// <summary>The encoding used when converting strings to PDF commands (ISO-8859-1 / Latin-1).</summary>
    internal static readonly Encoding Encoding = Encoding.Latin1;

    /// <summary>Converts text to a byte array for writing to a PDF file (ISO-8859-1).</summary>
    internal static byte[] Encode(string text) => Encoding.GetBytes(text);

    /// <summary>
    /// Flushes the given text buffer to a stream with the right encoding and resets the buffer.
    /// This is used to efficiently switch between outputting text and binary content.
    /// </summary>
    internal static void FlushTextBuffer(StringBuilder textBuffer, Stream output)
    {
        byte[] bytes = Encode(textBuffer.ToString());
        output.Write(bytes, 0, bytes.Length);
        textBuffer.Length = 0;
    }
}
