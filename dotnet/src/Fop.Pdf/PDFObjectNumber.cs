using System.Globalization;

namespace Fop.Pdf;

/// <summary>
/// Holds a PDF object number. The number may be assigned eagerly (a fixed value) or lazily, in
/// which case it is allocated from the owning document the first time it is read.
/// </summary>
public class PDFObjectNumber
{
    private int _num;
    private IPdfDocument? _doc;

    /// <summary>Creates an unassigned object number (lazily allocated from a document).</summary>
    public PDFObjectNumber()
    {
    }

    /// <summary>Creates an object number with a fixed value.</summary>
    /// <param name="num">The object number.</param>
    public PDFObjectNumber(int num) => _num = num;

    /// <summary>Sets the document this number is allocated from when read lazily.</summary>
    public void SetDocument(IPdfDocument? doc) => _doc = doc;

    /// <summary>
    /// Returns the object number, allocating it from the document on first access if it has not
    /// yet been assigned. Mirrors the Java <c>num = ++doc.objectcount</c> behaviour.
    /// </summary>
    public int Number
    {
        get
        {
            if (_num == 0 && _doc != null)
            {
                _num = _doc.AllocateObjectNumber();
            }
            return _num;
        }
    }

    /// <inheritdoc/>
    public override string ToString() => Number.ToString(CultureInfo.InvariantCulture);
}
