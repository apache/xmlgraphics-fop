using System.Text;
using Fop.Pdf;
using Xunit;

namespace Fop.Pdf.Tests;

internal static class PdfTestSupport
{
    /// <summary>
    /// Mirrors <c>PDFObjectTestCase.testOutputStreams</c>: outputs the object both with object
    /// number 0 and 1, checking the serialized bytes and the returned length each time.
    /// </summary>
    public static void AssertOutput(string expected, PDFObject obj)
    {
        using var outStream = new MemoryStream();
        obj.SetObjectNumber(0);
        Assert.Equal(expected.Length, obj.Output(outStream));
        Assert.Equal(expected, Latin1(outStream));

        outStream.SetLength(0);
        obj.SetObjectNumber(1);
        Assert.Equal(expected.Length, obj.Output(outStream));
        Assert.Equal(expected, Latin1(outStream));
    }

    public static string Latin1(MemoryStream stream) => Encoding.Latin1.GetString(stream.ToArray());
}
