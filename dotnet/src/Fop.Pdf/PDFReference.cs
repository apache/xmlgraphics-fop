using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A PDF object reference. The reference holds a weak reference to the actual PDF object so the
/// garbage collector can free it if it is not referenced elsewhere; the important part is the
/// reference information (object number + generation) to the object in the PDF file.
/// </summary>
public class PDFReference : IPDFWritable
{
    private readonly PDFObjectNumber _objectNumber;
    private readonly int _generation;

    // Soft reference equivalent: lets the GC reclaim the target object when unreferenced.
    private WeakReference<PDFObject>? _objReference;

    /// <summary>Creates a new PDF reference to the given object.</summary>
    public PDFReference(PDFObject obj)
    {
        _objectNumber = obj.ObjectNumber;
        _generation = obj.Generation;
        _objReference = new WeakReference<PDFObject>(obj);
    }

    /// <summary>
    /// Creates a new PDF reference from an object reference string (e.g. <c>"8 0 R"</c>), without
    /// a reference to the original object.
    /// </summary>
    public PDFReference(string reference)
    {
        ArgumentNullException.ThrowIfNull(reference);
        string[] parts = reference.Split(' ');
        if (parts.Length != 3 || parts[2] != "R")
        {
            throw new ArgumentException("Invalid PDF reference: " + reference, nameof(reference));
        }
        _objectNumber = new PDFObjectNumber(int.Parse(parts[0], CultureInfo.InvariantCulture));
        _generation = int.Parse(parts[1], CultureInfo.InvariantCulture);
    }

    /// <summary>Returns the referenced PDF object, or <see langword="null"/> if it has been released.</summary>
    public PDFObject? Object
    {
        get
        {
            if (_objReference is null)
            {
                return null;
            }
            if (_objReference.TryGetTarget(out PDFObject? obj))
            {
                return obj;
            }
            _objReference = null;
            return null;
        }
    }

    /// <summary>Returns the object number.</summary>
    public PDFObjectNumber ObjectNumber => _objectNumber;

    /// <summary>Returns the generation.</summary>
    public int Generation => _generation;

    /// <inheritdoc/>
    public override string ToString()
    {
        var textBuffer = new StringBuilder();
        OutputInline(null, textBuffer);
        return textBuffer.ToString();
    }

    /// <inheritdoc/>
    public void OutputInline(Stream? output, StringBuilder textBuffer) =>
        textBuffer.Append(ObjectNumber.Number)
            .Append(' ')
            .Append(Generation)
            .Append(" R");
}
