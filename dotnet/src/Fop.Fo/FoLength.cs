// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Globalization;

namespace Fop.Fo;

/// <summary>
/// An absolute length, stored in millipoints (1pt = 1000mpt) — the internal unit FOP uses
/// throughout the layout engine. Provides a parser for CSS/XSL-FO length expressions.
/// </summary>
/// <param name="Millipoints">The length in millipoints.</param>
public readonly record struct FoLength(double Millipoints)
{
    /// <summary>A zero length.</summary>
    public static readonly FoLength Zero = new(0);

    private const double MillipointsPerPoint = 1000.0;

    /// <summary>The length in points.</summary>
    public double Points => Millipoints / MillipointsPerPoint;

    /// <summary>Creates a length from a value in points.</summary>
    public static FoLength FromPoints(double points) => new(points * MillipointsPerPoint);

    /// <summary>
    /// Parses an XSL-FO length expression such as <c>"12pt"</c>, <c>"2.5mm"</c>, <c>"1in"</c>,
    /// <c>"1.2em"</c>, or a percentage like <c>"80%"</c>.
    /// </summary>
    /// <param name="value">The expression to parse.</param>
    /// <param name="fontSizeMpt">The current font size in millipoints, used to resolve <c>em</c>.</param>
    /// <param name="percentBaseMpt">The base length in millipoints, used to resolve percentages.</param>
    /// <returns>The resolved absolute length, or <c>null</c> if the value cannot be parsed.</returns>
    public static FoLength? TryParse(string? value, double fontSizeMpt = 12_000, double percentBaseMpt = 0)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        string text = value.Trim();

        // Percentage.
        if (text.EndsWith('%'))
        {
            if (double.TryParse(text[..^1], NumberStyles.Float, CultureInfo.InvariantCulture, out double pct))
            {
                return new FoLength(percentBaseMpt * pct / 100.0);
            }

            return null;
        }

        // Split numeric part from the unit suffix.
        int splitIndex = text.Length;
        for (int i = 0; i < text.Length; i++)
        {
            char c = text[i];
            if (!char.IsDigit(c) && c != '.' && c != '-' && c != '+' && c != 'e' && c != 'E')
            {
                splitIndex = i;
                break;
            }
        }

        string numberPart = text[..splitIndex];
        string unit = text[splitIndex..].Trim().ToLowerInvariant();

        if (!double.TryParse(numberPart, NumberStyles.Float, CultureInfo.InvariantCulture, out double number))
        {
            return null;
        }

        // Unitless numbers are treated as points (lenient; strict XSL-FO requires a unit).
        double mpt = unit switch
        {
            "" or "pt" => number * MillipointsPerPoint,
            "px" => number * MillipointsPerPoint,                 // assume 1px == 1pt at 72dpi
            "pc" => number * 12 * MillipointsPerPoint,            // 1pica == 12pt
            "in" => number * 72 * MillipointsPerPoint,
            "cm" => number * 72 / 2.54 * MillipointsPerPoint,
            "mm" => number * 72 / 25.4 * MillipointsPerPoint,
            "em" => number * fontSizeMpt,
            _ => double.NaN,
        };

        return double.IsNaN(mpt) ? null : new FoLength(mpt);
    }

    /// <summary>Parses a length, returning <see cref="Zero"/> (or a supplied default) when invalid.</summary>
    public static FoLength ParseOrDefault(string? value, FoLength @default, double fontSizeMpt = 12_000,
        double percentBaseMpt = 0)
        => TryParse(value, fontSizeMpt, percentBaseMpt) ?? @default;

    /// <inheritdoc/>
    public override string ToString() => $"{Points.ToString(CultureInfo.InvariantCulture)}pt";

    /// <summary>Adds two lengths.</summary>
    public static FoLength operator +(FoLength a, FoLength b) => new(a.Millipoints + b.Millipoints);

    /// <summary>Subtracts one length from another.</summary>
    public static FoLength operator -(FoLength a, FoLength b) => new(a.Millipoints - b.Millipoints);
}
