using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFReferenceTestCase</c>.</summary>
public class PDFReferenceTests
{
    [Fact]
    public void OutputInline()
    {
        var name = new PDFName("Test name");
        name.SetObjectNumber(2);
        var pdfRef = new PDFReference(name);

        using var output = new MemoryStream();
        var textBuffer = new StringBuilder();
        // Text before OutputInline() is kept.
        textBuffer.Append("Text ");

        pdfRef.OutputInline(output, textBuffer);
        Assert.Equal("Text 2 0 R", textBuffer.ToString());
    }

    [Fact]
    public void ToStringTest()
    {
        var name = new PDFName("arbitrary");
        name.SetObjectNumber(10);
        var reference = new PDFReference(name);
        Assert.Equal("10 0 R", reference.ToString());
    }

    [Fact]
    public void ParsedFromStringHasNoObject()
    {
        var reference = new PDFReference("8 0 R");
        Assert.Equal(8, reference.ObjectNumber.Number);
        Assert.Equal(0, reference.Generation);
        Assert.Null(reference.Object);
    }

    [Fact]
    public void NullStringThrows() => Assert.Throws<ArgumentNullException>(() => new PDFReference((string)null!));
}
