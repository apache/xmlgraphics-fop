using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A PDF array object.
/// </summary>
public class PDFArray : PDFObject
{
    /// <summary>The values held by this array.</summary>
    protected List<object?> Values { get; } = [];

    /// <summary>Creates a new, empty array object.</summary>
    /// <param name="parent">The array's parent, if any.</param>
    public PDFArray(PDFObject? parent) : base(parent)
    {
    }

    /// <summary>Creates a new, empty array object with no parent.</summary>
    public PDFArray() : this((PDFObject?)null)
    {
    }

    /// <summary>Creates an array object from the given int values.</summary>
    public PDFArray(PDFObject? parent, int[] values) : base(parent)
    {
        foreach (int value in values)
        {
            Values.Add(value);
        }
    }

    /// <summary>Creates an array object from the given double values.</summary>
    public PDFArray(PDFObject? parent, double[] values) : base(parent)
    {
        foreach (double value in values)
        {
            Values.Add(value);
        }
    }

    /// <summary>Creates an array object from the given values.</summary>
    public PDFArray(PDFObject? parent, IEnumerable<object?> values) : base(parent) =>
        Values.AddRange(values);

    /// <summary>Creates an array object made of the given elements (no parent).</summary>
    public PDFArray(IEnumerable<object?> elements) : this(null, elements)
    {
    }

    /// <summary>Indicates whether the given object exists in the array.</summary>
    public bool Contains(object? obj) => Values.Contains(obj);

    /// <summary>The length of the array.</summary>
    public int Length => Values.Count;

    /// <summary>Gets or sets the entry at a given index.</summary>
    public object? this[int index]
    {
        get => Values[index];
        set => Values[index] = value;
    }

    /// <summary>Sets an entry at a given location.</summary>
    public void Set(int index, object? obj) => Values[index] = obj;

    /// <summary>Sets an entry at a given location to a double value.</summary>
    public void Set(int index, double value) => Values[index] = value;

    /// <summary>Gets an entry at a given location.</summary>
    public object? Get(int index) => Values[index];

    /// <summary>Adds a new value to the array.</summary>
    public void Add(object? obj)
    {
        if (obj is PDFObject pdfObj && !pdfObj.HasObjectNumber)
        {
            pdfObj.Parent = this;
        }
        Values.Add(obj);
    }

    /// <summary>Adds a new double value to the array.</summary>
    public void Add(double value) => Values.Add(value);

    /// <summary>Clears the array.</summary>
    public void Clear() => Values.Clear();

    /// <inheritdoc/>
    public override int Output(Stream stream)
    {
        var cout = new CountingStream(stream);
        var textBuffer = new StringBuilder(64);
        textBuffer.Append('[');
        for (int i = 0; i < Values.Count; i++)
        {
            if (i > 0)
            {
                textBuffer.Append(' ');
            }
            FormatObject(Values[i], cout, textBuffer);
        }
        textBuffer.Append(']');
        FlushTextBuffer(textBuffer, cout);
        return (int)cout.Count;
    }

    /// <inheritdoc/>
    public override void GetChildren(ISet<PDFObject> children)
    {
        var contents = new List<object?>();
        foreach (object? c in Values)
        {
            if (c is not PDFReference)
            {
                contents.Add(c);
            }
        }
        PDFDictionary.GetChildren(contents, children);
    }
}
