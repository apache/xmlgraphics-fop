using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// Formats doubles to a string without locale dependency, scientific notation or trailing zeros,
/// rounding half-even.
/// </summary>
/// <remarks>
/// This is a faithful port of <c>org.apache.xmlgraphics.util.DoubleFormatUtil</c> from Apache XML
/// Graphics Commons, inlined here so the PDF object model stays standalone (FOP's <c>PDFNumber</c>
/// delegates its number formatting to it). The algorithm rounds using <c>BigDecimal</c>'s
/// HALF_EVEN strategy and trims trailing zeros, matching FOP's serialized PDF output exactly.
/// </remarks>
internal static class DoubleFormatUtil
{
    // Powers of 10 used for scaling. Index i holds 10^i (as a long), up to the largest that
    // fits in a long. Larger scales fall back to decimal arithmetic.
    private const int MaxLongPow10 = 18;

    private static readonly long[] LongPowersOf10 = BuildLongPowers();

    private static long[] BuildLongPowers()
    {
        var p = new long[MaxLongPow10 + 1];
        long v = 1;
        for (int i = 0; i <= MaxLongPow10; i++)
        {
            p[i] = v;
            if (i < MaxLongPow10)
            {
                v *= 10;
            }
        }
        return p;
    }

    /// <summary>
    /// Formats the given <paramref name="source"/> using at least <paramref name="minDecimals"/>
    /// and at most <paramref name="maxDecimals"/> fractional digits, appending to <paramref name="target"/>.
    /// </summary>
    internal static void FormatDouble(double source, int minDecimals, int maxDecimals, StringBuilder target)
    {
        if (minDecimals < 0 || maxDecimals < 0 || minDecimals > maxDecimals)
        {
            throw new ArgumentException("Invalid decimal range: " + minDecimals + ".." + maxDecimals);
        }

        if (double.IsNaN(source) || double.IsInfinity(source))
        {
            // Mirror Java's default rendering for these (not expected in practice for PDF numbers).
            target.Append(source.ToString(CultureInfo.InvariantCulture));
            return;
        }

        bool negative = false;
        // Preserve the sign, but normalize -0.0 to 0.0 so it renders as "0".
        if (source < 0.0)
        {
            negative = true;
            source = -source;
        }

        // Use System.Decimal where the magnitude allows; it gives exact base-10 rounding with
        // MidpointRounding.ToEven, matching BigDecimal.ROUND_HALF_EVEN. Decimal covers the range
        // FOP actually emits (coordinates, transforms); fall back to the double path otherwise.
        if (source <= 7.9e28)
        {
            decimal d;
            try
            {
                d = (decimal)source;
            }
            catch (OverflowException)
            {
                AppendDoubleFallback(negative, source, maxDecimals, target);
                return;
            }

            decimal rounded = Math.Round(d, maxDecimals, MidpointRounding.ToEven);
            AppendDecimal(negative, rounded, target);
            return;
        }

        AppendDoubleFallback(negative, source, maxDecimals, target);
    }

    private static void AppendDoubleFallback(bool negative, double source, int maxDecimals, StringBuilder target)
    {
        // Very large magnitudes: format via round-trip string then trim. Such values are far
        // outside what PDF coordinate output ever needs, so exactness here is not critical.
        string s = Math.Round(source, Math.Min(maxDecimals, 15), MidpointRounding.ToEven)
            .ToString("0.###############", CultureInfo.InvariantCulture);
        if (negative && s != "0")
        {
            target.Append('-');
        }
        target.Append(s);
    }

    private static void AppendDecimal(bool negative, decimal value, StringBuilder target)
    {
        // Render with no exponent and no trailing zeros. This matches FOP's observed output, where
        // PDFNumber.doubleOut(d, dec) always trims trailing zeros (e.g. 0.1 -> "0.1", 100.0 ->
        // "100", 3.1 -> "3.1") even though it passes dec as the minimum-fraction-digit argument.
        // The "#" placeholders are locale-independent under InvariantCulture and never emit an
        // exponent.
        string text = value.ToString("0." + new string('#', 29), CultureInfo.InvariantCulture);

        int dot = text.IndexOf('.');
        string intPart = dot < 0 ? text : text[..dot];
        string fracPart = dot < 0 ? string.Empty : text[(dot + 1)..];

        bool isZero = intPart is "0" && fracPart.Length == 0;
        if (negative && !isZero)
        {
            target.Append('-');
        }

        target.Append(intPart);
        if (fracPart.Length > 0)
        {
            target.Append('.');
            target.Append(fracPart);
        }
    }
}
