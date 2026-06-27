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

namespace Fop.Fonts;

/// <summary>
/// Font utilities.
/// <para>Port of <c>org.apache.fop.fonts.FontUtil</c>.</para>
/// </summary>
public static class FontUtil
{
    // font constituent names which identify a font as being of "italic" style
    private static readonly string[] ItalicWords =
        [FontConstants.StyleItalic, FontConstants.StyleOblique, FontConstants.StyleInclined];

    // font constituent names which identify a font as being of "light" weight
    private static readonly string[] LightWords = ["light"];

    // font constituent names which identify a font as being of "medium" weight
    private static readonly string[] MediumWords = ["medium"];

    // font constituent names which identify a font as being of "demi/semi" weight
    private static readonly string[] DemiWords = ["demi", "semi"];

    // font constituent names which identify a font as being of "bold" weight
    private static readonly string[] BoldWords = ["bold"];

    // font constituent names which identify a font as being of "extra bold" weight
    private static readonly string[] ExtraBoldWords =
        ["extrabold", "extra bold", "black", "heavy", "ultra", "super"];

    /// <summary>
    /// Parses a CSS2 (SVG and XSL-FO) font weight ("normal", "bold", 100-900) to an integer.
    /// See http://www.w3.org/TR/REC-CSS2/fonts.html#propdef-font-weight.
    /// TODO: Implement "lighter" and "bolder".
    /// </summary>
    /// <param name="text">the font weight to parse.</param>
    /// <returns>an integer between 100 and 900 (100, 200, 300...).</returns>
    /// <exception cref="ArgumentException">if the value is not a legal font weight.</exception>
    public static int ParseCss2FontWeight(string text)
    {
        // Mirrors Java Integer.parseInt: a strict integer parse (no decimal point, no leading "+"
        // sign tolerance beyond what Java allows). On failure we fall through to the symbolic names.
        if (int.TryParse(text, NumberStyles.AllowLeadingSign, CultureInfo.InvariantCulture, out int weight))
        {
            weight = (weight / 100) * 100;
            weight = Math.Max(weight, 100);
            weight = Math.Min(weight, 900);
            return weight;
        }

        // weight is no number, so convert symbolic name to number
        return text switch
        {
            "normal" => 400,
            "bold" => 700,
            _ => throw new ArgumentException(
                "Illegal value for font weight: '"
                + text
                + "'. Use one of: 100, 200, 300, "
                + "400, 500, 600, 700, 800, 900, "
                + "normal (=400), bold (=700)"),
        };
    }

    /// <summary>
    /// Removes all white space from a string (used primarily for font names).
    /// </summary>
    /// <param name="str">the string.</param>
    /// <returns>the processed result (or <c>null</c> if <paramref name="str"/> is <c>null</c>).</returns>
    public static string? StripWhiteSpace(string? str)
    {
        if (str is not null)
        {
            // Matches Java: strips only space, CR, LF and tab (not the full Unicode whitespace set).
            StringBuilder stringBuilder = new(str.Length);
            foreach (char ch in str)
            {
                if (ch != ' ' && ch != '\r' && ch != '\n' && ch != '\t')
                {
                    stringBuilder.Append(ch);
                }
            }

            return stringBuilder.ToString();
        }

        return str;
    }

    /// <summary>
    /// Guesses the font style of a font using its name.
    /// </summary>
    /// <param name="fontName">the font name.</param>
    /// <returns>"normal" or "italic".</returns>
    public static string GuessStyle(string? fontName)
    {
        if (fontName is not null)
        {
            // Java uses case-sensitive substring search (String.indexOf) against the style words
            // "italic", "oblique" and "inclined".
            foreach (string word in ItalicWords)
            {
                if (fontName.Contains(word, StringComparison.Ordinal))
                {
                    return FontConstants.StyleItalic;
                }
            }
        }

        return FontConstants.StyleNormal;
    }

    /// <summary>
    /// Guesses the font weight of a font using its name.
    /// </summary>
    /// <param name="fontName">the font name.</param>
    /// <returns>an integer between 100 and 900.</returns>
    public static int GuessWeight(string fontName)
    {
        // weight
        int weight = FontConstants.WeightNormal;

        foreach (string word in BoldWords)
        {
            if (fontName.Contains(word, StringComparison.Ordinal))
            {
                weight = FontConstants.WeightBold;
                break;
            }
        }

        foreach (string word in MediumWords)
        {
            if (fontName.Contains(word, StringComparison.Ordinal))
            {
                weight = FontConstants.WeightNormal + 100; // 500
                break;
            }
        }

        // Search for "semi/demi" before "light", but after "bold"
        // (normally semi/demi-bold is meant, but it can also be semi/demi-light)
        foreach (string word in DemiWords)
        {
            if (fontName.Contains(word, StringComparison.Ordinal))
            {
                weight = FontConstants.WeightBold - 100; // 600
                break;
            }
        }

        foreach (string word in ExtraBoldWords)
        {
            if (fontName.Contains(word, StringComparison.Ordinal))
            {
                weight = FontConstants.WeightExtraBold;
                break;
            }
        }

        foreach (string word in LightWords)
        {
            if (fontName.Contains(word, StringComparison.Ordinal))
            {
                weight = FontConstants.WeightLight;
                break;
            }
        }

        return weight;
    }
}
