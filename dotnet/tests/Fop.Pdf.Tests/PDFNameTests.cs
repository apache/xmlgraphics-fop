using System.Globalization;
using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Ported from <c>PDFNameTestCase</c>.</summary>
public class PDFNameTests
{
    private readonly FakePdfDocument _doc = new();
    private readonly PDFName _pdfName;

    public PDFNameTests()
    {
        _pdfName = new PDFName("TestName");
        _pdfName.Parent = new DummyPDFObject();
        _pdfName.Document = _doc;
    }

    [Fact]
    public void EscapeName()
    {
        Assert.Throws<ArgumentNullException>(() => PDFName.EscapeName(null!));

        // All names are prefixed by "/".
        Assert.Equal("/Test", PDFName.EscapeName("Test"));
        // A leading "/" is not duplicated.
        Assert.Equal("/Test", PDFName.EscapeName("/Test"));
        // Space in the middle is escaped.
        Assert.Equal("/Test#20test", PDFName.EscapeName("Test test"));
        // Chars whose code points are over 256: "BCDEEE+" + fullwidth M (U+FF2D), fullwidth S
        // (U+FF33), space, katakana KO (U+30B4), SHI (U+30B7), small TU (U+30C3), KU (U+30AF).
        Assert.Equal("/BCDEEE+#EF#BC#AD#EF#BC#B3#20#E3#82#B4#E3#82#B7#E3#83#83#E3#82#AF",
            PDFName.EscapeName("BCDEEE+\uFF2D\uFF33 \u30B4\u30B7\u30C3\u30AF"));

        NonEscapedCharactersTests();
        EscapedCharactersTests();
    }

    private static void EscapedCharactersTests()
    {
        for (char i = (char)0; i < '!'; i++)
        {
            string str = ((i >>> 4) & 0x0f).ToString("X", CultureInfo.InvariantCulture)
                + (i & 0x0f).ToString("X", CultureInfo.InvariantCulture);
            Assert.Equal("/#" + str, PDFName.EscapeName(i.ToString()));
        }
        for (char i = (char)128; i < 256; i++)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(i.ToString());
            var str = new StringBuilder("/");
            foreach (byte b in bytes)
            {
                str.Append('#');
                str.Append(((b >>> 4) & 0x0f).ToString("X", CultureInfo.InvariantCulture));
                str.Append((b & 0x0f).ToString("X", CultureInfo.InvariantCulture));
            }
            Assert.Equal(str.ToString(), PDFName.EscapeName(i.ToString()));
        }

        foreach (char c in "#%()<>[]>")
        {
            CheckCharacterIsEscaped(c);
        }
    }

    private static void CheckCharacterIsEscaped(char c)
    {
        string str = ((c >>> 4) & 0x0f).ToString("X", CultureInfo.InvariantCulture)
            + (c & 0x0f).ToString("X", CultureInfo.InvariantCulture);
        Assert.Equal("/#" + str, PDFName.EscapeName(c.ToString()));
    }

    private static void NonEscapedCharactersTests()
    {
        CharactersNotEscapedBetween('!', '"');
        CharactersNotEscapedBetween('*', ';');
        CharactersNotEscapedBetween('?', 'Z');
        CharactersNotEscapedBetween('^', '~');
    }

    private static void CharactersNotEscapedBetween(char c1, char c2)
    {
        for (char i = c1; i <= c2; i++)
        {
            string str = i.ToString();
            string expected = str != "/" ? "/" + str : str;
            Assert.Equal(expected, PDFName.EscapeName(str));
        }
    }

    [Fact]
    public void ToStringTest()
    {
        var test1 = new PDFName("test1");
        Assert.Equal("/test1", test1.ToString());
        var test2 = new PDFName("another test");
        Assert.Equal("/another#20test", test2.ToString());
        Assert.Throws<ArgumentNullException>(() => new PDFName(null!));
    }

    [Fact]
    public void Output()
    {
        PdfTestSupport.AssertOutput("/TestName", _pdfName);
        PdfTestSupport.AssertOutput("/test#20test", new PDFName("test test"));
    }

    [Fact]
    public void OutputInline()
    {
        using var outStream = new MemoryStream();
        var textBuffer = new StringBuilder();

        // No object number set.
        _pdfName.OutputInline(outStream, textBuffer);
        PDFObject.FlushTextBuffer(textBuffer, outStream);
        Assert.Equal("/TestName", PdfTestSupport.Latin1(outStream));

        outStream.SetLength(0);
        // Object number set.
        _pdfName.SetObjectNumber(1);
        _pdfName.OutputInline(outStream, textBuffer);
        PDFObject.FlushTextBuffer(textBuffer, outStream);
        Assert.Equal("1 0 R", PdfTestSupport.Latin1(outStream));
    }

    [Fact]
    public void NameWithoutLeadingSlash()
    {
        Assert.Equal("TestName", _pdfName.Name);
        Assert.Equal("another#20test", new PDFName("another test").Name);
    }
}
