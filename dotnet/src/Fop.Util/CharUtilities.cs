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

namespace Fop.Util;

/// <summary>
/// Utilities to distinguish various kinds of Unicode whitespace and to inspect
/// individual characters / code points.
/// <para>
/// Faithful port of <c>org.apache.fop.util.CharUtilities</c>. Where Java used
/// <c>int</c> character-class constants, this port returns the strongly typed
/// <see cref="CharClass"/> enum.
/// </para>
/// </summary>
public static class CharUtilities
{
    /// <summary>Character code used to signal a character boundary in inline content.</summary>
    public const char CodeEot = '\u0000';

    /// <summary>Null char.</summary>
    public const char NullChar = '\u0000';

    /// <summary>Linefeed character.</summary>
    public const char LineFeedChar = '\n';

    /// <summary>Carriage return.</summary>
    public const char CarriageReturn = '\r';

    /// <summary>Normal tab.</summary>
    public const char Tab = '\t';

    /// <summary>Normal space.</summary>
    public const char Space = '\u0020';

    /// <summary>Non-breaking space.</summary>
    public const char NbSpace = '\u00A0';

    /// <summary>Next line control character.</summary>
    public const char NextLine = '\u0085';

    /// <summary>Zero-width space.</summary>
    public const char ZeroWidthSpace = '\u200B';

    /// <summary>Word joiner.</summary>
    public const char WordJoiner = '\u2060';

    /// <summary>Zero-width joiner.</summary>
    public const char ZeroWidthJoiner = '\u200D';

    /// <summary>Left-to-right mark.</summary>
    public const char Lrm = '\u200E';

    /// <summary>Right-to-left mark. (Matches the original FOP constant, which uses U+202F.)</summary>
    public const char Rlm = '\u202F';

    /// <summary>Left-to-right embedding.</summary>
    public const char Lre = '\u202A';

    /// <summary>Right-to-left embedding.</summary>
    public const char Rle = '\u202B';

    /// <summary>Pop directional formatting.</summary>
    public const char Pdf = '\u202C';

    /// <summary>Left-to-right override.</summary>
    public const char Lro = '\u202D';

    /// <summary>Right-to-left override.</summary>
    public const char Rlo = '\u202E';

    /// <summary>Zero-width no-break space (= byte order mark).</summary>
    public const char ZeroWidthNoBreakSpace = '\uFEFF';

    /// <summary>Soft hyphen.</summary>
    public const char SoftHyphen = '\u00AD';

    /// <summary>Line separator.</summary>
    public const char LineSeparator = '\u2028';

    /// <summary>Paragraph separator.</summary>
    public const char ParagraphSeparator = '\u2029';

    /// <summary>Missing ideograph.</summary>
    public const char MissingIdeograph = '\u25A1';

    /// <summary>Ideographic space.</summary>
    public const char IdeographicSpace = '\u3000';

    /// <summary>Object replacement character.</summary>
    public const char ObjectReplacementCharacter = '\uFFFC';

    /// <summary>Unicode value indicating that the character is "not a character".</summary>
    public const char NotACharacter = '\uFFFF';

    /// <summary>
    /// Returns the appropriate <see cref="CharClass"/> for the given character.
    /// </summary>
    public static CharClass ClassOf(int c) => c switch
    {
        CodeEot => CharClass.Eot,
        LineFeedChar => CharClass.LineFeed,
        Space or CarriageReturn or Tab => CharClass.XmlWhitespace,
        _ => IsAnySpace(c) ? CharClass.UnicodeWhitespace : CharClass.NonWhitespace,
    };

    /// <summary>
    /// Determines whether the character is a space with normal (breaking) behaviour.
    /// </summary>
    public static bool IsBreakableSpace(int c) => c == Space || IsFixedWidthSpace(c);

    /// <summary>Determines whether the character is a zero-width space.</summary>
    public static bool IsZeroWidthSpace(int c) =>
        c == ZeroWidthSpace || c == WordJoiner || c == ZeroWidthNoBreakSpace;

    /// <summary>Determines whether the character is a (breakable) fixed-width space.</summary>
    public static bool IsFixedWidthSpace(int c) =>
        (c >= '\u2000' && c <= ZeroWidthSpace) || c == IdeographicSpace;

    /// <summary>Determines whether the character is a non-breaking space.</summary>
    public static bool IsNonBreakableSpace(int c) =>
        c == NbSpace
        || c == Rlm            // U+202F narrow no-break space
        || c == IdeographicSpace
        || c == WordJoiner
        || c == ZeroWidthNoBreakSpace;

    /// <summary>Determines whether the character is an adjustable space.</summary>
    public static bool IsAdjustableSpace(int c) => c == Space || c == NbSpace;

    /// <summary>Determines whether the character represents any kind of space.</summary>
    public static bool IsAnySpace(int c) => IsBreakableSpace(c) || IsNonBreakableSpace(c);

    /// <summary>
    /// Indicates whether a character is classified as "Alphabetic" by the Unicode standard
    /// (Lu + Ll + Lt + Lm + Lo + Nl).
    /// </summary>
    public static bool IsAlphabetic(int c)
    {
        var category = CharUnicodeInfo.GetUnicodeCategory((char)c);
        return category switch
        {
            UnicodeCategory.UppercaseLetter
            or UnicodeCategory.LowercaseLetter
            or UnicodeCategory.TitlecaseLetter
            or UnicodeCategory.ModifierLetter
            or UnicodeCategory.OtherLetter
            or UnicodeCategory.LetterNumber => true,
            _ => false,
        };
    }

    /// <summary>Indicates whether the given character is an explicit break character.</summary>
    public static bool IsExplicitBreak(int c) =>
        c == LineFeedChar
        || c == CarriageReturn
        || c == NextLine
        || c == LineSeparator
        || c == ParagraphSeparator;

    /// <summary>
    /// Converts a single Unicode scalar value to an XML numeric character reference.
    /// Four hex digits are used inside the BMP, six otherwise.
    /// </summary>
    public static string CharToNCRef(int c)
    {
        var sb = new StringBuilder();
        for (int i = 0, nDigits = (c > 0xFFFF) ? 6 : 4; i < nDigits; i++, c >>= 4)
        {
            int d = c & 0xF;
            char hd = d < 10 ? (char)('0' + d) : (char)('A' + (d - 10));
            sb.Append(hd);
        }

        // Java appends the reversed buffer; reverse to match exactly.
        char[] digits = sb.ToString().ToCharArray();
        Array.Reverse(digits);
        return "&#x" + new string(digits) + ";";
    }

    /// <summary>
    /// Converts a string to a sequence of ASCII or XML numeric character references.
    /// </summary>
    public static string ToNCRefs(string? s)
    {
        var sb = new StringBuilder();
        if (s != null)
        {
            foreach (char c in s)
            {
                if (c is >= (char)32 and < (char)127)
                {
                    sb.Append(c switch
                    {
                        '<' => "&lt;",
                        '>' => "&gt;",
                        '&' => "&amp;",
                        _ => c.ToString(),
                    });
                }
                else
                {
                    sb.Append(CharToNCRef(c));
                }
            }
        }

        return sb.ToString();
    }

    /// <summary>Pads a string on the left out to <paramref name="width"/> using <paramref name="pad"/>.</summary>
    public static string PadLeft(string s, int width, char pad) => s.PadLeft(width, pad);

    /// <summary>
    /// Formats a character for debugging output: prefixed with "0x", padded left with '0',
    /// and either 4 or 6 hex characters wide according to whether it is in the BMP or not.
    /// </summary>
    public static string Format(int c)
    {
        if (c < 1114112)
        {
            string hex = Convert.ToString(c, 16);
            return "0x" + PadLeft(hex, c < 65536 ? 4 : 6, '0');
        }

        return "!NOT A CHARACTER!";
    }

    /// <summary>Determines whether two character sequences contain the same characters.</summary>
    public static bool IsSameSequence(string cs1, string cs2)
    {
        ArgumentNullException.ThrowIfNull(cs1);
        ArgumentNullException.ThrowIfNull(cs2);
        return string.Equals(cs1, cs2, StringComparison.Ordinal);
    }

    /// <summary>
    /// Determines whether the specified code point is in the Basic Multilingual Plane (BMP).
    /// </summary>
    public static bool IsBmpCodePoint(int codePoint) => (uint)codePoint >> 16 == 0;

    /// <summary>Returns 1 if <paramref name="codePoint"/> is not in the BMP, otherwise 0.</summary>
    public static int IncrementIfNonBmp(int codePoint) => IsBmpCodePoint(codePoint) ? 0 : 1;

    /// <summary>Determines whether the given character is part of a surrogate pair.</summary>
    public static bool IsSurrogatePair(char ch) => char.IsHighSurrogate(ch) || char.IsLowSurrogate(ch);

    /// <summary>
    /// Tells whether there is a well-formed surrogate pair starting at the given index.
    /// </summary>
    /// <exception cref="ArgumentException">If the surrogate pair is ill-formed.</exception>
    public static bool ContainsSurrogatePairAt(string chars, int index)
    {
        ArgumentNullException.ThrowIfNull(chars);
        char ch = chars[index];

        if (char.IsHighSurrogate(ch))
        {
            if (index + 1 >= chars.Length)
            {
                throw new ArgumentException(
                    "ill-formed UTF-16 sequence, contains isolated high surrogate at end of sequence");
            }

            if (char.IsLowSurrogate(chars[index + 1]))
            {
                return true;
            }

            throw new ArgumentException(
                $"ill-formed UTF-16 sequence, contains isolated high surrogate at index {index}");
        }

        if (char.IsLowSurrogate(ch))
        {
            throw new ArgumentException(
                $"ill-formed UTF-16 sequence, contains isolated low surrogate at index {index}");
        }

        return false;
    }

    /// <summary>Enumerates the Unicode code points of the given string.</summary>
    public static IEnumerable<int> CodePoints(string s)
    {
        ArgumentNullException.ThrowIfNull(s);
        return CodePoints(s, 0, s.Length);
    }

    /// <summary>Enumerates the Unicode code points of the given sub-range of a string.</summary>
    public static IEnumerable<int> CodePoints(string s, int beginIndex, int endIndex)
    {
        ArgumentNullException.ThrowIfNull(s);
        ArgumentOutOfRangeException.ThrowIfNegative(beginIndex);
        ArgumentOutOfRangeException.ThrowIfGreaterThan(endIndex, s.Length);
        ArgumentOutOfRangeException.ThrowIfGreaterThan(beginIndex, endIndex);

        return Iterate(s, beginIndex, endIndex);

        static IEnumerable<int> Iterate(string s, int beginIndex, int endIndex)
        {
            int nextIndex = beginIndex;
            while (nextIndex < endIndex)
            {
                int result = char.ConvertToUtf32(s, nextIndex);
                nextIndex += char.IsHighSurrogate(s[nextIndex]) ? 2 : 1;
                yield return result;
            }
        }
    }
}
