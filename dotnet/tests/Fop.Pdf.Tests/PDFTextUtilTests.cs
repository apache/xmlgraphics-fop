using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Coverage for <see cref="PDFTextUtil"/>.</summary>
public class PDFTextUtilTests
{
    private sealed class CapturingTextUtil : PDFTextUtil
    {
        public StringBuilder Output { get; } = new();

        protected override void Write(string code) => Output.Append(code);

        protected override void Write(StringBuilder code) => Output.Append(code);
    }

    [Fact]
    public void BeginAndEndTextObject()
    {
        var util = new CapturingTextUtil();
        Assert.False(util.IsInTextObject);
        util.BeginTextObject();
        Assert.True(util.IsInTextObject);
        util.EndTextObject();
        Assert.False(util.IsInTextObject);
        Assert.Equal("BT\nET\n", util.Output.ToString());
    }

    [Fact]
    public void BeginTwiceThrows()
    {
        var util = new CapturingTextUtil();
        util.BeginTextObject();
        Assert.Throws<InvalidOperationException>(() => util.BeginTextObject());
    }

    [Fact]
    public void WriteTfBeforeBeginThrows()
    {
        var util = new CapturingTextUtil();
        Assert.Throws<InvalidOperationException>(() => util.WriteTf("F1", 12));
    }

    [Fact]
    public void WriteTd()
    {
        var util = new CapturingTextUtil();
        util.WriteTd(10.5, -20.25);
        Assert.Equal("10.5 -20.25 Td\n", util.Output.ToString());
    }

    [Fact]
    public void ConcatMatrixSkipsIdentity()
    {
        var util = new CapturingTextUtil();
        util.ConcatMatrix(new PdfAffineTransform(1, 0, 0, 1, 0, 0));
        Assert.Equal("", util.Output.ToString());

        util.ConcatMatrix(new PdfAffineTransform(2, 0, 0, 2, 5, 6));
        Assert.Equal("2 0 0 2 5 6 cm\n", util.Output.ToString());
    }

    [Fact]
    public void SingleByteTjMappedCharsAndAdjust()
    {
        var util = new CapturingTextUtil();
        util.BeginTextObject();
        util.UpdateTf("F1", 12, multiByte: false, cid: false);
        util.WriteTJMappedChar('A');
        util.WriteTJMappedChar('B');
        util.AdjustGlyphTJ(120);
        util.WriteTJMappedChar('C');
        util.WriteTJ();
        // Tf command followed by the TJ array.
        Assert.Contains("/F1 12 Tf\n", util.Output.ToString());
        Assert.Contains("[(AB) 120 (C)] TJ\n", util.Output.ToString());
    }

    [Fact]
    public void SetTextRenderingModeOutOfRangeThrows()
    {
        var util = new CapturingTextUtil();
        Assert.Throws<ArgumentException>(() => util.SetTextRenderingMode(8));
    }
}
