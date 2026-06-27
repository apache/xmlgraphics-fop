using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// A simple PDF number object. Also exposes the static helpers FOP uses to format numbers for PDF
/// output.
/// </summary>
public class PDFNumber : PDFObject
{
    private object? _number;

    /// <summary>Creates a number object initialized to 0.</summary>
    public PDFNumber() => _number = 0;

    /// <summary>Creates a number object wrapping the given value.</summary>
    public PDFNumber(object? number) => _number = number;

    /// <summary>The wrapped number (boxed; may be any numeric type or <see langword="null"/>).</summary>
    public object? Number
    {
        get => _number;
        set => _number = value;
    }

    /// <summary>Outputs a double value to a string suitable for PDF (6 decimal digits).</summary>
    public static string DoubleOut(double doubleDown) => DoubleOut(doubleDown, 6);

    /// <summary>
    /// Outputs a double value to a string suitable for PDF, with at most <paramref name="dec"/>
    /// decimal places.
    /// </summary>
    /// <param name="doubleDown">The value.</param>
    /// <param name="dec">The number of decimal places to output (0..16).</param>
    public static string DoubleOut(double doubleDown, int dec)
    {
        if (dec < 0 || dec > 16)
        {
            throw new ArgumentException("Parameter dec must be between 1 and 16");
        }
        var buf = new StringBuilder();
        DoubleFormatUtil.FormatDouble(doubleDown, dec, dec, buf);
        return buf.ToString();
    }

    /// <summary>
    /// Appends a double value, formatted for PDF with at most <paramref name="dec"/> decimal
    /// places, to <paramref name="buf"/>.
    /// </summary>
    public static StringBuilder DoubleOut(double doubleDown, int dec, StringBuilder buf)
    {
        if (dec < 0 || dec > 16)
        {
            throw new ArgumentException("Parameter dec must be between 1 and 16");
        }
        DoubleFormatUtil.FormatDouble(doubleDown, dec, dec, buf);
        return buf;
    }

    /// <inheritdoc/>
    protected override string ToPDFString()
    {
        if (Number is null)
        {
            throw new ArgumentException("The number of this PDFNumber must not be empty");
        }
        var sb = new StringBuilder(64);
        sb.Append(DoubleOut(Convert.ToDouble(Number, CultureInfo.InvariantCulture), 10));
        return sb.ToString();
    }

    /// <inheritdoc/>
    public override bool SupportsObjectStream => false;
}
