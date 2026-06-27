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
using System.Text;

namespace Fop.Util.Text;

/// <summary>
/// A minimal port of <c>java.text.ChoiceFormat</c> covering the pattern features used by
/// <see cref="ChoiceFieldPart"/>: limits separated by <c>#</c> (inclusive), <c>&lt;</c>
/// (exclusive, the limit becomes the next representable double) or U+2264 (inclusive),
/// with choices separated by <c>|</c>.
/// <para>
/// FOP only ever uses <c>ChoiceFormat</c> for selecting a string by a numeric value, so only that
/// behaviour is reproduced here (no formatting of the number itself and no parsing).
/// </para>
/// </summary>
public sealed class ChoiceFormat
{
    private readonly double[] choiceLimits;
    private readonly string[] choiceFormats;

    /// <summary>Constructs the format from a Java <c>ChoiceFormat</c> pattern string.</summary>
    public ChoiceFormat(string newPattern)
    {
        ArgumentNullException.ThrowIfNull(newPattern);
        ApplyPattern(newPattern, out choiceLimits, out choiceFormats);
    }

    // Port of java.text.ChoiceFormat.applyPattern.
    private static void ApplyPattern(string newPattern, out double[] limits, out string[] formats)
    {
        var segments = new StringBuilder[2];
        segments[0] = new StringBuilder();
        segments[1] = new StringBuilder();
        var newChoiceLimits = new List<double>();
        var newChoiceFormats = new List<string>();
        int part = 0;
        double startValue = 0;
        double oldStartValue = double.NaN;
        bool inQuote = false;
        for (int i = 0; i < newPattern.Length; ++i)
        {
            char ch = newPattern[i];
            if (ch == '\'')
            {
                // Check for "''" indicating a literal quote.
                if (i + 1 < newPattern.Length && newPattern[i + 1] == ch)
                {
                    segments[part].Append(ch);
                    ++i;
                }
                else
                {
                    inQuote = !inQuote;
                }
            }
            else if (inQuote)
            {
                segments[part].Append(ch);
            }
            else if (ch == '<' || ch == '#' || ch == '\u2264')
            {
                if (segments[0].Length == 0)
                {
                    throw new ArgumentException("Each interval must contain a number before a format");
                }

                double tempStart = double.Parse(
                    segments[0].ToString(), NumberStyles.Float, CultureInfo.InvariantCulture);

                if (ch == '<' && tempStart != double.PositiveInfinity
                    && tempStart != double.NegativeInfinity)
                {
                    tempStart = NextDouble(tempStart);
                }

                if (tempStart <= oldStartValue)
                {
                    throw new ArgumentException(
                        "Incorrect order of intervals, must be in ascending order");
                }

                segments[0].Length = 0;
                startValue = tempStart;
                part = 1;
            }
            else if (ch == '|')
            {
                newChoiceLimits.Add(startValue);
                newChoiceFormats.Add(segments[1].ToString());
                oldStartValue = startValue;
                segments[1].Length = 0;
                part = 0;
            }
            else
            {
                segments[part].Append(ch);
            }
        }

        // Clean up last one.
        if (part == 1)
        {
            newChoiceLimits.Add(startValue);
            newChoiceFormats.Add(segments[1].ToString());
        }

        limits = newChoiceLimits.ToArray();
        formats = newChoiceFormats.ToArray();
    }

    /// <summary>
    /// Returns the choice that applies to the given number, mirroring
    /// <c>java.text.ChoiceFormat.format(double)</c>.
    /// </summary>
    public string Format(double number)
    {
        // Find the number.
        int i;
        for (i = 0; i < choiceLimits.Length; ++i)
        {
            if (!(number >= choiceLimits[i]))
            {
                // Same as number < choiceLimits, except catches NaN.
                break;
            }
        }
        --i;
        if (i < 0)
        {
            i = 0;
        }
        // Return either a formatted number, or a string.
        return choiceFormats[i];
    }

    // Port of java.text.ChoiceFormat.nextDouble(double) for the increasing direction:
    // returns the next representable double greater than d.
    private static double NextDouble(double d) => Math.BitIncrement(d);
}
