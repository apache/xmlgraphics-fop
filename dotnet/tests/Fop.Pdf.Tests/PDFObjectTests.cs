using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFObjectTestCase</c>.</summary>
public class PDFObjectTests
{
    private readonly FakePdfDocument _doc = new();
    private readonly PDFObject _parent = new DummyPDFObject();
    private readonly PDFObject _obj;

    public PDFObjectTests()
    {
        _obj = new DummyPDFObject();
        _obj.Document = _doc;
        _obj.Parent = _parent;
    }

    [Fact]
    public void SetObjectNumber()
    {
        _obj.SetObjectNumber(1);
        Assert.Equal(1, _obj.ObjectNumber.Number);

        _obj.SetObjectNumber(5);
        Assert.Equal(5, _obj.ObjectNumber.Number);
    }

    [Fact]
    public void HasObjectNumber()
    {
        Assert.False(_obj.HasObjectNumber);
        _obj.SetObjectNumber(1);
        Assert.True(_obj.HasObjectNumber);
    }

    [Fact]
    public void GetGeneration() => Assert.Equal(0, _obj.Generation);

    [Fact]
    public void SetDocument()
    {
        Assert.Equal(_doc, _obj.Document);
        var anotherDoc = new FakePdfDocument();
        _obj.Document = anotherDoc;
        Assert.Equal(anotherDoc, _obj.Document);
    }

    [Fact]
    public void SetParent()
    {
        Assert.Equal(_parent, _obj.Parent);
        var anotherParent = new DummyPDFObject();
        _obj.Parent = anotherParent;
        Assert.Equal(anotherParent, _obj.Parent);
    }

    [Fact]
    public void GetObjectID()
    {
        _obj.SetObjectNumber(10);
        Assert.Equal("10 0 obj\n", _obj.ObjectID);
    }

    [Fact]
    public void ReferencePDF()
    {
        Assert.Throws<ArgumentException>(() => _obj.ReferencePDF());
        _obj.SetObjectNumber(10);
        Assert.Equal("10 0 R", _obj.ReferencePDF());
    }

    [Fact]
    public void MakeReference()
    {
        _obj.SetObjectNumber(10);
        PDFReference reference = _obj.MakeReference();
        Assert.Equal(_obj.ObjectNumber, reference.ObjectNumber);
        Assert.Equal(_obj, reference.Object);
        Assert.Equal(_obj.ReferencePDF(), reference.ToString());
    }

    [Fact]
    public void Reference()
    {
        var dict = new PDFDictionary();
        dict.SetObjectNumber(7);
        PDFReference reference = dict.MakeReference();
        Assert.Equal(7, reference.ObjectNumber.Number);
        Assert.Equal(0, reference.Generation);
        Assert.Equal("7 0 R", reference.ToString());

        reference = new PDFReference("8 0 R");
        Assert.Equal(8, reference.ObjectNumber.Number);
        Assert.Equal(0, reference.Generation);
        Assert.Equal("8 0 R", reference.ToString());
    }
}
