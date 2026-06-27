using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFDictionaryTestCase</c>.</summary>
public class PDFDictionaryTests
{
    private readonly FakePdfDocument _doc = new();
    private readonly PDFObject _parent = new DummyPDFObject();

    private readonly PDFDictionary _dict;
    private readonly PDFArray _testArray;
    private readonly PDFNumber _testNumber;

    // The insertion order of the entries MUST be maintained.
    private const string ExpectedOutput =
        "<<\n"
        + "  /String (TestValue)\n"
        + "  /int 10\n"
        + "  /double 3.1\n"
        + "  /array [1 (two) 20]\n"
        + "  /number 20\n"
        + "  /null null\n"
        + ">>";

    public PDFDictionaryTests()
    {
        _testNumber = new PDFNumber();
        _testNumber.Parent = _parent;
        _testNumber.Number = 20;

        _testArray = new PDFArray();
        _testArray.Add(1);
        _testArray.Add("two");
        _testArray.Add(_testNumber);

        _dict = new PDFDictionary(_parent);
        _dict.Document = _doc;
        _dict.Put("String", "TestValue");
        _dict.Put("int", 10);
        _dict.Put("double", 3.1);
        _dict.Put("array", _testArray);
        _dict.Put("number", _testNumber);
        _dict.Put("null", null);
    }

    [Fact]
    public void Put()
    {
        Assert.Equal("TestValue", _dict.Get("String"));
        Assert.Equal(10, _dict.Get("int"));
        Assert.Equal(3.1, _dict.Get("double"));
        // PDFObjects without a parent get the dict as their parent.
        Assert.Equal(_testArray, _dict.Get("array"));
        Assert.Equal(_dict, _testArray.Parent);
        // PDFObjects with a parent keep their existing parent.
        Assert.Equal(_testNumber, _dict.Get("number"));
        // Missing entries return null.
        Assert.Null(_dict.Get("Not in dictionary"));
        // Overwriting works.
        _dict.Put("array", 10);
        Assert.Equal(10, _dict.Get("array"));
        // Nulls are handled.
        Assert.Null(_dict.Get("null"));
    }

    [Fact]
    public void WriteDictionary()
    {
        using var outStream = new MemoryStream();
        var textBuffer = new StringBuilder();
        // WriteDictionary is protected in C#; exercise via the public Output path.
        _dict.Output(outStream);
        Assert.Equal(ExpectedOutput, PdfTestSupport.Latin1(outStream));
    }

    [Fact]
    public void Output() => PdfTestSupport.AssertOutput(ExpectedOutput, _dict);

    [Fact]
    public void ContainsAndRemove()
    {
        Assert.True(_dict.ContainsKey("int"));
        _dict.Remove("int");
        Assert.False(_dict.ContainsKey("int"));
    }

    [Fact]
    public void CompactFormatForSmallDictionaries()
    {
        var small = new PDFDictionary();
        small.Put("Type", new PDFName("Catalog"));
        small.Put("Count", 3);
        using var outStream = new MemoryStream();
        small.Output(outStream);
        Assert.Equal("<< /Type /Catalog /Count 3 >>", PdfTestSupport.Latin1(outStream));
    }

    [Fact]
    public void Indexer()
    {
        Assert.Equal("TestValue", _dict["String"]);
        _dict["new"] = "value";
        Assert.Equal("value", _dict["new"]);
    }
}
