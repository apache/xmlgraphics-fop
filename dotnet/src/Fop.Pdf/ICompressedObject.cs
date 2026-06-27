namespace Fop.Pdf;

/// <summary>
/// Represents a PDF object that may appear in an object stream. An object stream is a PDF stream
/// whose content is a sequence of PDF objects (see Section 3.4.6 of the PDF 1.5 Reference).
/// </summary>
public interface ICompressedObject
{
    /// <summary>
    /// Returns the object number of this indirect object. A compressed object must have a
    /// generation number of 0.
    /// </summary>
    PDFObjectNumber ObjectNumber { get; }

    /// <summary>
    /// Outputs this object's content into the given stream.
    /// </summary>
    /// <param name="output">A stream, likely to be provided by the containing object stream.</param>
    /// <returns>The number of bytes written to the stream.</returns>
    int Output(Stream output);
}
