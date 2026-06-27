namespace Fop.Pdf;

/// <summary>
/// Minimal abstraction of the owning PDF document, as seen by the low-level object model.
/// </summary>
/// <remarks>
/// In Apache FOP this is the concrete <c>PDFDocument</c> class, which is large and out of scope
/// for this slice of the port. <see cref="PDFObject"/>, <see cref="PDFObjectNumber"/> and friends
/// only need a tiny part of it: the running object counter used to lazily assign object numbers,
/// and the knowledge of whether encryption is active. This interface captures exactly that subset.
/// </remarks>
// TODO: full PDFDocument port. Replace this interface with the real document type once it lands.
public interface IPdfDocument
{
    /// <summary>
    /// Allocates and returns the next object number. Mirrors the Java field increment
    /// <c>num = ++doc.objectcount</c> used by <c>PDFObjectNumber</c>.
    /// </summary>
    /// <returns>The newly assigned (1-based) object number.</returns>
    int AllocateObjectNumber();

    /// <summary>
    /// Indicates whether encryption is active for this document. When encryption is active,
    /// string and binary content must be encrypted before being written.
    /// </summary>
    bool IsEncryptionActive { get; }
}
