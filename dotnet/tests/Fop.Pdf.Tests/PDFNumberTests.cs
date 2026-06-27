using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFNumberTestCase</c>. Tests PDFNumber.DoubleOut().</summary>
public class PDFNumberTests
{
    // Java float literals are widened to double; reproduce by storing as float first.
    private static double F(float f) => f;

    [Fact]
    public void DoubleOut1()
    {
        // Default is 6 decimal digits.
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.0f)));
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.0000000000000000000123f)));
        Assert.Equal("0.1", PDFNumber.DoubleOut(F(0.1f)));
        Assert.Equal("100", PDFNumber.DoubleOut(F(100.0f)));
        Assert.Equal("100", PDFNumber.DoubleOut(F(99.99999999999999999999999f)));

        // ROUND_HALF_EVEN strategy (see Java comment in original test).
        Assert.Equal("100.123459", PDFNumber.DoubleOut(F(100.12345611111111f)));
        Assert.Equal("-100.123459", PDFNumber.DoubleOut(F(-100.12345611111111f)));
    }

    [Fact]
    public void DoubleOut2()
    {
        // 4 decimal digits.
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.0f), 4));
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.0000000000000000000123f), 4));
        Assert.Equal("0.1", PDFNumber.DoubleOut(F(0.1f), 4));
        Assert.Equal("100", PDFNumber.DoubleOut(F(100.0f), 4));
        Assert.Equal("100", PDFNumber.DoubleOut(F(99.99999999999999999999999f), 4));
        Assert.Equal("100.1234", PDFNumber.DoubleOut(F(100.12341111111111f), 4));
        Assert.Equal("-100.1234", PDFNumber.DoubleOut(F(-100.12341111111111f), 4));
    }

    [Fact]
    public void DoubleOut3()
    {
        // 0 decimal digits.
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.0f), 0));
        Assert.Equal("0", PDFNumber.DoubleOut(F(0.1f), 0));
        Assert.Equal("1", PDFNumber.DoubleOut(F(0.6f), 0));
        Assert.Equal("100", PDFNumber.DoubleOut(F(100.1234f), 0));
        Assert.Equal("-100", PDFNumber.DoubleOut(F(-100.1234f), 0));
    }

    [Fact]
    public void DoubleOut4()
    {
        double d = double.Parse("5.7220458984375E-6", System.Globalization.CultureInfo.InvariantCulture);
        Assert.Equal("0.000006", PDFNumber.DoubleOut(d));
        Assert.Equal("0", PDFNumber.DoubleOut(d, 4));
        Assert.Equal("0.00000572", PDFNumber.DoubleOut(d, 8));
    }

    [Fact]
    public void DoubleOutWrongParameters()
    {
        Assert.Throws<ArgumentException>(() => PDFNumber.DoubleOut(F(0.1f), -1));
        Assert.Throws<ArgumentException>(() => PDFNumber.DoubleOut(F(0.1f), 17));
        Assert.Throws<ArgumentException>(() => PDFNumber.DoubleOut(F(0.1f), 98274659));
    }

    [Fact]
    public void GetSetNumber()
    {
        var pdfNum = new PDFNumber();
        pdfNum.Number = 1.111f;
        Assert.Equal(1.111f, pdfNum.Number);
        pdfNum.Number = 2;
        Assert.Equal(2, pdfNum.Number);
        pdfNum.Number = null;
        Assert.Null(pdfNum.Number);
    }

    [Fact]
    public void ToPDFString()
    {
        var testSubject = new PDFNumber { Number = 1.0001 };
        PdfTestSupport.AssertOutput("1.0001", testSubject);
        testSubject.Number = 999;
        PdfTestSupport.AssertOutput("999", testSubject);
    }

    [Fact]
    public void SupportsObjectStreamIsFalse() => Assert.False(new PDFNumber().SupportsObjectStream);
}
