using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A PDF dictionary object.
/// </summary>
public class PDFDictionary : PDFObject
{
    private bool _visited;

    /// <summary>The entry map.</summary>
    protected Dictionary<string, object?> Entries { get; } = [];

    /// <summary>
    /// Maintains the insertion order of the entries. Kept in sync with <see cref="Entries"/>.
    /// </summary>
    protected List<string> Order { get; } = [];

    /// <summary>Creates a new dictionary object.</summary>
    public PDFDictionary()
    {
    }

    /// <summary>Creates a new dictionary object.</summary>
    /// <param name="parent">The object's parent, if any.</param>
    public PDFDictionary(PDFObject? parent) : base(parent)
    {
    }

    /// <summary>Puts a new name/value pair.</summary>
    public void Put(string name, object? value)
    {
        if (value is PDFObject pdfObj && !pdfObj.HasObjectNumber)
        {
            pdfObj.Parent = this;
        }
        if (!Entries.ContainsKey(name))
        {
            Order.Add(name);
        }
        Entries[name] = value;
    }

    /// <summary>Puts a new name/value pair with an int value.</summary>
    public void Put(string name, int value)
    {
        if (!Entries.ContainsKey(name))
        {
            Order.Add(name);
        }
        Entries[name] = value;
    }

    /// <summary>Returns the value for a name, or <see langword="null"/> if absent.</summary>
    public object? Get(string name) => Entries.GetValueOrDefault(name);

    /// <summary>Gets or sets the value for a name. The getter returns <see langword="null"/> if absent.</summary>
    public object? this[string name]
    {
        get => Get(name);
        set => Put(name, value);
    }

    /// <inheritdoc/>
    public override int Output(Stream stream)
    {
        var cout = new CountingStream(stream);
        var textBuffer = new StringBuilder(64);
        WriteDictionary(cout, textBuffer);
        FlushTextBuffer(textBuffer, cout);
        return (int)cout.Count;
    }

    /// <summary>Writes the contents of the dictionary to the text buffer (and stream for binary).</summary>
    protected void WriteDictionary(Stream output, StringBuilder textBuffer)
    {
        textBuffer.Append("<<");
        bool compact = Order.Count <= 2;
        foreach (string key in Order)
        {
            textBuffer.Append(compact ? " " : "\n  ");
            textBuffer.Append(PDFName.EscapeName(key));
            textBuffer.Append(' ');
            // Use GetValueOrDefault so a key left in Order after Remove() (matching Java's
            // entries/order desync) serializes as "null" rather than throwing.
            FormatObject(Entries.GetValueOrDefault(key), output, textBuffer);
        }
        textBuffer.Append(compact ? " " : "\n");
        textBuffer.Append(">>");
    }

    /// <inheritdoc/>
    public override void GetChildren(ISet<PDFObject> children)
    {
        if (!_visited)
        {
            _visited = true;
            var childrenMap = new Dictionary<string, object?>(Entries);
            childrenMap.Remove("Parent");
            GetChildren(childrenMap.Values, children);
            _visited = false;
        }
    }

    /// <summary>Collects the descendant PDF objects (with assigned object numbers) of the given values.</summary>
    public static void GetChildren(IEnumerable<object?> values, ISet<PDFObject> children)
    {
        foreach (object? value in values)
        {
            object? x = value;
            if (x is PDFReference reference)
            {
                x = reference.Object;
            }
            if (x is PDFObject pdfObject)
            {
                if (pdfObject.HasObjectNumber)
                {
                    children.Add(pdfObject);
                }
                pdfObject.GetChildren(children);
            }
        }
    }

    /// <summary>The set of keys in this dictionary.</summary>
    public ICollection<string> Keys => Entries.Keys;

    /// <summary>Indicates whether the dictionary contains the given key.</summary>
    public bool ContainsKey(string name) => Entries.ContainsKey(name);

    /// <summary>Removes the mapping for the specified key.</summary>
    public void Remove(string name) => Entries.Remove(name);
}
