using System.Text;

namespace Fop.Pdf;

/// <summary>
/// Implemented by classes that can be serialized to a PDF file, either by serializing the object
/// inline or by writing an indirect reference to the actual object.
/// </summary>
public interface IPDFWritable
{
    /// <summary>
    /// Writes a "direct object" (inline object) representation. A text buffer is given for
    /// optimized encoding of text content.
    /// </summary>
    /// <remarks>
    /// IMPORTANT: If you need to write out binary output, flush the <paramref name="textBuffer"/>
    /// (see <see cref="PDFObject.FlushTextBuffer"/>) before writing any content to
    /// <paramref name="output"/>.
    /// </remarks>
    /// <param name="output">The stream (for binary content); may be <see langword="null"/> when only text is produced.</param>
    /// <param name="textBuffer">The text buffer for text content.</param>
    void OutputInline(Stream? output, StringBuilder textBuffer);
}
