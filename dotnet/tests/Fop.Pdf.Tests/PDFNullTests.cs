using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFNullTestCase</c>.</summary>
public class PDFNullTests
{
    [Fact]
    public void OutputInline()
    {
        PDFNull obj = PDFNull.Instance;
        using var output = new MemoryStream();
        var text = new StringBuilder();
        obj.OutputInline(output, text);
        Assert.Equal("null", text.ToString());

        // Previously written text is not discarded.
        obj.OutputInline(output, text);
        Assert.Equal("nullnull", text.ToString());
    }

    [Fact]
    public void ToStringIsNull() => Assert.Equal("null", PDFNull.Instance.ToString());
}
