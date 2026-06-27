using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFArrayTestCase</c>.</summary>
public class PDFArrayTests
{
    private readonly FakePdfDocument _doc = new();
    private readonly PDFObject _parent = new DummyPDFObject();

    private readonly PDFArray _intArray;
    private readonly PDFArray _doubleArray;
    private readonly PDFArray _collectionArray;
    private readonly PDFArray _objArray;
    private readonly PDFNumber _num;

    private const string IntArrayOutput = "[1 2 3 4 5]";
    private const string DoubleArrayOutput = "[1.1 2.2 3.3 4.4 5.5]";
    private const string CollectionArrayOutput = "[(one) (two) (three)]";
    private const string ObjArrayOutput = "[(one) 2 3 4 0 R]";

    public PDFArrayTests()
    {
        _intArray = new PDFArray(_parent, new[] { 1, 2, 3, 4, 5 });
        _doubleArray = new PDFArray(_parent, new[] { 1.1, 2.2, 3.3, 4.4, 5.5 });

        var strList = new List<object?> { "one", "two", "three" };
        _collectionArray = new PDFArray(_parent, strList);

        _num = new PDFNumber();
        _num.Number = 20;
        _num.SetObjectNumber(4);
        _objArray = new PDFArray(_parent, new object?[] { "one", 2, 3.0f, _num });

        _intArray.Document = _doc;
        _doubleArray.Document = _doc;
        _collectionArray.Document = _doc;
        _objArray.Document = _doc;
        _objArray.Parent = _parent;
    }

    [Fact]
    public void Contains()
    {
        for (int i = 1; i <= 5; i++)
        {
            Assert.True(_intArray.Contains(i));
        }
        Assert.False(_intArray.Contains(6));
        Assert.False(_intArray.Contains(0));

        Assert.True(_doubleArray.Contains(1.1));
        Assert.True(_doubleArray.Contains(5.5));
        Assert.False(_doubleArray.Contains(10.0));

        Assert.True(_collectionArray.Contains("one"));
        Assert.True(_collectionArray.Contains("three"));
        Assert.False(_collectionArray.Contains("zero"));

        Assert.True(_objArray.Contains("one"));
        Assert.True(_objArray.Contains(2));
        Assert.True(_objArray.Contains(3.0f));
        Assert.True(_objArray.Contains(_num));
        Assert.False(_objArray.Contains("four"));
    }

    [Fact]
    public void Length()
    {
        Assert.Equal(5, _intArray.Length);
        Assert.Equal(5, _doubleArray.Length);
        Assert.Equal(3, _collectionArray.Length);
        Assert.Equal(4, _objArray.Length);

        _intArray.Add(6);
        Assert.Equal(6, _intArray.Length);
    }

    [Fact]
    public void Set()
    {
        var name = new PDFName("zero test");
        _objArray.Set(0, name);
        Assert.Equal(name, _objArray.Get(0));

        _objArray.Set(1, "test");
        Assert.Equal("test", _objArray.Get(1));

        // Goes through the Set(int, double) overload.
        _objArray.Set(2, 5);
        Assert.Equal(5.0, _objArray.Get(2));

        Assert.Throws<ArgumentOutOfRangeException>(() => _objArray.Set(4, 2));
    }

    [Fact]
    public void Get()
    {
        for (int i = 1; i <= 5; i++)
        {
            Assert.Equal(i, _intArray.Get(i - 1));
        }
        Assert.Equal(1.1, _doubleArray.Get(0));
        Assert.Equal(5.5, _doubleArray.Get(4));

        Assert.Equal("one", _collectionArray.Get(0));
        Assert.Equal("three", _collectionArray.Get(2));

        Assert.Equal("one", _objArray.Get(0));
        Assert.Equal(2, _objArray.Get(1));
        Assert.Equal(0, ((float)3.0).CompareTo((float)_objArray.Get(2)!));
        Assert.Equal(_num, _objArray.Get(3));
    }

    [Fact]
    public void Add()
    {
        // Mirror Java's Integer.valueOf(6): force the Add(object?) overload so the element stays an
        // int (a bare literal would bind to Add(double)).
        _intArray.Add((object)6);
        _doubleArray.Add(6.6);
        for (int i = 1; i <= 6; i++)
        {
            Assert.Equal(i, _intArray.Get(i - 1));
        }
        Assert.Equal(6.6, _doubleArray.Get(5));

        _collectionArray.Add(1.0);
        Assert.Equal(1.0, _collectionArray.Get(3));

        _objArray.Add("four");
        Assert.Equal("four", _objArray.Get(4));
    }

    [Fact]
    public void Output()
    {
        PdfTestSupport.AssertOutput(IntArrayOutput, _intArray);
        PdfTestSupport.AssertOutput(DoubleArrayOutput, _doubleArray);
        PdfTestSupport.AssertOutput(CollectionArrayOutput, _collectionArray);
        PdfTestSupport.AssertOutput(ObjArrayOutput, _objArray);
    }

    [Fact]
    public void Indexer()
    {
        Assert.Equal(1, _intArray[0]);
        _intArray[0] = 99;
        Assert.Equal(99, _intArray[0]);
    }

    [Fact]
    public void Clear()
    {
        _intArray.Clear();
        Assert.Equal(0, _intArray.Length);
    }
}
