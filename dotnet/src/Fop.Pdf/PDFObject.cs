using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// Generic PDF object.
/// </summary>
/// <remarks>
/// A PDF document is essentially a collection of these objects. A PDF object has a number and a
/// generation (the generation is always 0 in new documents).
/// </remarks>
public abstract class PDFObject : IPDFWritable, ICompressedObject
{
    private bool _hasObjNum;
    private PDFObjectNumber _objNum = new();

    // The parent PDFDocument (typed as the minimal IPdfDocument abstraction).
    // TODO: full PDFDocument port.
    private IPdfDocument? _document;

    // The direct parent PDFObject (may be null; needed for encryption).
    private PDFObject? _parent;

    /// <summary>Default constructor.</summary>
    protected PDFObject()
    {
    }

    /// <summary>Constructor for direct objects.</summary>
    /// <param name="parent">The containing <see cref="PDFObject"/> instance.</param>
    protected PDFObject(PDFObject? parent) => Parent = parent;

    /// <summary>Returns the object's number.</summary>
    /// <exception cref="InvalidOperationException">If the object has no number assigned.</exception>
    public PDFObjectNumber ObjectNumber =>
        _hasObjNum
            ? _objNum
            : throw new InvalidOperationException("Object has no number assigned: " + ToString());

    /// <summary>Indicates whether this object has already been assigned an object number.</summary>
    public bool HasObjectNumber => _hasObjNum;

    /// <summary>Sets the object number, lazily allocated from the given document.</summary>
    public void SetObjectNumber(IPdfDocument document)
    {
        _objNum.SetDocument(document);
        _hasObjNum = true;
        IPdfDocument? doc = Document;
        Parent = null;
        Document = doc; // Restore reference to the document after setting parent to null.
    }

    /// <summary>Sets the object number from an existing <see cref="PDFObjectNumber"/>.</summary>
    public void SetObjectNumber(PDFObjectNumber objectNumber)
    {
        _objNum = objectNumber;
        _hasObjNum = true;
    }

    /// <summary>Sets the object number to a fixed value.</summary>
    public void SetObjectNumber(int objectNumber)
    {
        _objNum = new PDFObjectNumber(objectNumber);
        _hasObjNum = true;
    }

    /// <summary>Returns this object's generation. Always 0 in new documents.</summary>
    public int Generation => 0;

    /// <summary>
    /// The parent document, resolved through the parent chain if not set directly. May be
    /// <see langword="null"/> if no document has been assigned.
    /// </summary>
    public IPdfDocument? Document
    {
        get => _document ?? _parent?.Document;
        set => _document = value;
    }

    /// <summary>
    /// Returns the parent document, throwing an informative exception if it is unavailable
    /// (unlike <see cref="Document"/>, which returns <see langword="null"/>).
    /// </summary>
    public IPdfDocument DocumentSafely =>
        Document ?? throw new InvalidOperationException(
            "Parent PDFDocument is unavailable on " + GetType().FullName);

    /// <summary>
    /// This object's direct parent. Null for a "direct object" (or if it has not been set).
    /// </summary>
    public PDFObject? Parent
    {
        get => _parent;
        set => _parent = value;
    }

    /// <summary>Returns the PDF representation of the object ID, e.g. <c>"10 0 obj\n"</c>.</summary>
    public string ObjectID =>
        ObjectNumber + " " + Generation.ToString(CultureInfo.InvariantCulture) + " obj\n";

    /// <summary>Returns the PDF representation of a reference to this object.</summary>
    /// <exception cref="ArgumentException">If the object has no object number.</exception>
    public string ReferencePDF()
    {
        if (!HasObjectNumber)
        {
            throw new ArgumentException(
                "Cannot reference this object. It doesn't have an object number");
        }
        return MakeReference().ToString();
    }

    /// <summary>Creates and returns a reference to this object.</summary>
    public PDFReference MakeReference() => new(this);

    /// <summary>Writes the PDF representation of this object.</summary>
    /// <param name="output">The stream to write the PDF to.</param>
    /// <returns>The number of bytes written.</returns>
    public virtual int Output(Stream output)
    {
        byte[] pdf = ToPDF();
        output.Write(pdf, 0, pdf.Length);
        return pdf.Length;
    }

    /// <inheritdoc/>
    public virtual void OutputInline(Stream? output, StringBuilder textBuffer)
    {
        if (HasObjectNumber)
        {
            textBuffer.Append(ReferencePDF());
        }
        else
        {
            FlushTextBuffer(textBuffer, output!);
            Output(output!);
        }
    }

    /// <summary>Encodes the object as a byte array for output to a PDF file.</summary>
    protected virtual byte[] ToPDF() => Encode(ToPDFString());

    /// <summary>
    /// Returns a string representation of the PDF object. Only use this to implement serialization
    /// when the object can be fully represented as text; binary content must use
    /// <see cref="Output(Stream)"/> instead.
    /// </summary>
    protected virtual string ToPDFString() =>
        throw new NotSupportedException("Not implemented. Use Output(Stream) instead.");

    /// <summary>Converts text to a byte array for writing to a PDF file (ISO-8859-1).</summary>
    public static byte[] Encode(string text) => PdfEncoding.Encode(text);

    /// <summary>
    /// Flushes a text buffer to a stream with the right encoding and resets the buffer, so that
    /// binary content can follow. Exposed for <see cref="IPDFWritable"/> implementors.
    /// </summary>
    public static void FlushTextBuffer(StringBuilder textBuffer, Stream output) =>
        PdfEncoding.FlushTextBuffer(textBuffer, output);

    /// <summary>Encodes a Text String (3.8.1 in PDF 1.4 specs).</summary>
    protected byte[] EncodeText(string text)
    {
        // TODO: full PDFDocument port (encryption path). When encryption is active, FOP encrypts
        // the UTF-16 bytes; that collaborator is out of scope for this slice.
        if (DocumentSafely.IsEncryptionActive)
        {
            throw new NotSupportedException(
                "Encryption is not supported by this slice of the PDF object model.");
        }
        return Encode(PDFText.EscapeText(text, false));
    }

    /// <summary>Encodes a String (3.2.3 in PDF 1.4 specs).</summary>
    protected byte[] EncodeString(string str) => EncodeText(str);

    /// <summary>Encodes binary data as a hexadecimal string object.</summary>
    protected void EncodeBinaryToHexString(byte[] data, Stream output)
    {
        output.WriteByte((byte)'<');
        // TODO: full PDFDocument port (encryption path).
        if (DocumentSafely.IsEncryptionActive)
        {
            throw new NotSupportedException(
                "Encryption is not supported by this slice of the PDF object model.");
        }
        string hex = PDFText.ToHex(data, false);
        byte[] encoded = Encoding.ASCII.GetBytes(hex);
        output.Write(encoded, 0, encoded.Length);
        output.WriteByte((byte)'>');
    }

    /// <summary>
    /// Formats an object for serialization to PDF.
    /// </summary>
    /// <remarks>
    /// IMPORTANT: To write binary output, the text buffer is flushed first (see
    /// <see cref="FlushTextBuffer"/>) before writing any content to <paramref name="output"/>.
    /// </remarks>
    protected void FormatObject(object? obj, Stream? output, StringBuilder textBuffer)
    {
        switch (obj)
        {
            case null:
                textBuffer.Append("null");
                break;
            case IPDFWritable writable:
                writable.OutputInline(output, textBuffer);
                break;
            case double d:
                textBuffer.Append(PDFNumber.DoubleOut(d));
                break;
            case float f:
                textBuffer.Append(PDFNumber.DoubleOut(f));
                break;
            // All other numeric types serialize via their invariant string form, matching Java's
            // Number.toString() for the non-Double/Float case (e.g. Integer, Long).
            case sbyte or byte or short or ushort or int or uint or long or ulong:
                textBuffer.Append(Convert.ToInt64(obj, CultureInfo.InvariantCulture)
                    .ToString(CultureInfo.InvariantCulture));
                break;
            case bool b:
                // Java Boolean.toString() emits lowercase "true"/"false".
                textBuffer.Append(b ? "true" : "false");
                break;
            case byte[] bytes:
                FlushTextBuffer(textBuffer, output!);
                EncodeBinaryToHexString(bytes, output!);
                break;
            default:
                FlushTextBuffer(textBuffer, output!);
                byte[] encoded = EncodeText(obj.ToString() ?? string.Empty);
                output!.Write(encoded, 0, encoded.Length);
                break;
        }
    }

    /// <summary>
    /// Checks whether the other object has the same content. The contract is weaker than
    /// <see cref="object.Equals(object)"/>: it need not check the object ID.
    /// </summary>
    protected virtual bool ContentEquals(PDFObject? o) => Equals(o);

    /// <summary>Collects child objects (no-op by default).</summary>
    public virtual void GetChildren(ISet<PDFObject> children)
    {
    }

    /// <summary>Indicates whether this object may appear in an object stream.</summary>
    public virtual bool SupportsObjectStream => true;
}
