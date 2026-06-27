using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

/// <summary>Coverage for <see cref="PDFText"/> string/text escaping and encoding helpers.</summary>
public class PDFTextTests
{
    [Fact]
    public void EscapeStringWrapsInParens()
    {
        Assert.Equal("()", PDFText.EscapeString(null));
        Assert.Equal("()", PDFText.EscapeString(""));
        Assert.Equal("(hello)", PDFText.EscapeString("hello"));
    }

    [Fact]
    public void EscapeStringEscapesParensAndBackslash()
    {
        Assert.Equal("(a\\(b\\)c)", PDFText.EscapeString("a(b)c"));
        Assert.Equal("(a\\\\b)", PDFText.EscapeString("a\\b"));
    }

    [Fact]
    public void EscapeStringEscapesControlChars()
    {
        Assert.Equal("(\\n\\r\\t\\b\\f)", PDFText.EscapeString("\n\r\t\b\f"));
    }

    [Fact]
    public void EscapeStringEscapesHighBytesAsOctal()
    {
        // 0xE9 = 233 -> octal 351
        Assert.Equal("(\\351)", PDFText.EscapeString("\u00E9"));
    }

    [Fact]
    public void EscapeTextAsciiWrapsInParens()
    {
        Assert.Equal("(Hello World)", PDFText.EscapeText("Hello World"));
        Assert.Equal("()", PDFText.EscapeText(null, false));
    }

    [Fact]
    public void EscapeTextWithUnicodeUsesHexWithBom()
    {
        // A char >= 128 forces hex mode using UTF-16BE with a leading BOM (FEFF).
        // 0x20AC (euro sign) -> FEFF 20AC.
        Assert.Equal("<FEFF20AC>", PDFText.EscapeText("\u20AC"));
    }

    [Fact]
    public void EscapeTextForceHexMode()
    {
        // 'A' (0x41) in UTF-16BE with BOM -> FEFF0041.
        Assert.Equal("<FEFF0041>", PDFText.EscapeText("A", true));
    }

    [Fact]
    public void ToHexWithBrackets()
    {
        byte[] data = [0x00, 0x0F, 0xFF, 0xAB];
        Assert.Equal("<000FFFAB>", PDFText.ToHex(data));
        Assert.Equal("000FFFAB", PDFText.ToHex(data, false));
    }

    [Fact]
    public void EscapeStringCharSpecialCases()
    {
        var sb = new StringBuilder();
        PDFText.EscapeStringChar('(', sb);
        PDFText.EscapeStringChar(')', sb);
        PDFText.EscapeStringChar('\\', sb);
        Assert.Equal("\\(\\)\\\\", sb.ToString());
    }

    [Fact]
    public void EscapeByteArrayEscapesSpecials()
    {
        byte[] data = Encoding.ASCII.GetBytes("a(b)\\c");
        byte[] escaped = PDFText.EscapeByteArray(data);
        Assert.Equal("(a\\(b\\)\\\\c)", Encoding.ASCII.GetString(escaped));
    }

    [Fact]
    public void ToUnicodeHexBmp()
    {
        var sb = new StringBuilder();
        PDFText.ToUnicodeHex(0x20AC, sb);
        Assert.Equal("20AC", sb.ToString());
    }

    [Fact]
    public void ToUnicodeHexNonBmp()
    {
        var sb = new StringBuilder();
        // U+1F600 (non-BMP) -> 6-character upper-hex.
        PDFText.ToUnicodeHex(0x1F600, sb);
        Assert.Equal("01F600", sb.ToString());
    }

    [Fact]
    public void ToUnicodeHexChar()
    {
        Assert.Equal("0041", PDFText.ToUnicodeHex('A'));
    }

    [Fact]
    public void ToPDFStringReplacesNonAscii()
    {
        Assert.Equal("a?c", PDFText.ToPDFString("a\u00E9c"));
        Assert.Equal("a#c", PDFText.ToPDFString("a\u00E9c", '#'));
    }

    [Fact]
    public void ToUTF16HasBom()
    {
        byte[] bytes = PDFText.ToUTF16("A");
        Assert.Equal([0xFE, 0xFF, 0x00, 0x41], bytes);
    }

    [Fact]
    public void TextObjectSerializesEscaped()
    {
        var text = new PDFText { Text = "a(b)" };
        text.SetObjectNumber(1);
        using var stream = new MemoryStream();
        text.Output(stream);
        Assert.Equal("((a\\(b\\)))", PdfTestSupport.Latin1(stream));
    }
}
