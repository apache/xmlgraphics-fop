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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

// Character constants are referenced by code point to keep this file pure ASCII and unambiguous.
public class CharUtilitiesTests
{
    [Theory]
    [InlineData(0x0000, CharClass.Eot)]            // CODE_EOT
    [InlineData(0x000A, CharClass.LineFeed)]       // \n
    [InlineData(0x0020, CharClass.XmlWhitespace)]  // space
    [InlineData(0x000D, CharClass.XmlWhitespace)]  // \r
    [InlineData(0x0009, CharClass.XmlWhitespace)]  // \t
    [InlineData(0x0041, CharClass.NonWhitespace)]  // 'A'
    [InlineData(0x00A0, CharClass.UnicodeWhitespace)] // non-breaking space
    public void ClassOf_ClassifiesCharacters(int c, CharClass expected)
        => Assert.Equal(expected, CharUtilities.ClassOf(c));

    [Theory]
    [InlineData(0x0020, true)]   // normal space
    [InlineData(0x00A0, false)]  // non-breaking space is not breakable
    [InlineData(0x2003, true)]   // em space is a fixed-width (breakable) space
    [InlineData(0x0041, false)]  // 'A'
    public void IsBreakableSpace_Works(int c, bool expected)
        => Assert.Equal(expected, CharUtilities.IsBreakableSpace(c));

    [Theory]
    [InlineData(0x00A0, true)]   // no-break space
    [InlineData(0x3000, true)]   // ideographic space
    [InlineData(0x2060, true)]   // word joiner
    [InlineData(0x0020, false)]  // normal space
    public void IsNonBreakableSpace_Works(int c, bool expected)
        => Assert.Equal(expected, CharUtilities.IsNonBreakableSpace(c));

    [Theory]
    [InlineData(0x200B, true)]   // zero-width space
    [InlineData(0x2060, true)]   // word joiner
    [InlineData(0xFEFF, true)]   // zero-width no-break space
    [InlineData(0x0041, false)]  // 'A'
    public void IsZeroWidthSpace_Works(int c, bool expected)
        => Assert.Equal(expected, CharUtilities.IsZeroWidthSpace(c));

    [Theory]
    [InlineData(0x0041, true)]   // 'A'
    [InlineData(0x007A, true)]   // 'z'
    [InlineData(0x0035, false)]  // '5'
    [InlineData(0x0020, false)]  // space
    public void IsAlphabetic_Works(int c, bool expected)
        => Assert.Equal(expected, CharUtilities.IsAlphabetic(c));

    [Theory]
    [InlineData(0x000A, true)]   // \n
    [InlineData(0x000D, true)]   // \r
    [InlineData(0x0085, true)]   // next line
    [InlineData(0x2028, true)]   // line separator
    [InlineData(0x0041, false)]  // 'A'
    public void IsExplicitBreak_Works(int c, bool expected)
        => Assert.Equal(expected, CharUtilities.IsExplicitBreak(c));

    [Theory]
    [InlineData(0x0041, "&#x0041;")]
    [InlineData(0x003C, "&#x003C;")]
    public void CharToNCRef_BmpUsesFourDigits(int c, string expected)
        => Assert.Equal(expected, CharUtilities.CharToNCRef(c));

    [Fact]
    public void CharToNCRef_NonBmpUsesSixDigits()
        => Assert.Equal("&#x01F600;", CharUtilities.CharToNCRef(0x1F600));

    [Fact]
    public void ToNCRefs_EscapesXmlAndEncodesNonAscii()
    {
        Assert.Equal("a&lt;b&gt;c&amp;d", CharUtilities.ToNCRefs("a<b>c&d"));
        Assert.Equal("&#x00E9;", CharUtilities.ToNCRefs("é")); // 'e' with acute accent
    }

    [Theory]
    [InlineData(0x41, "0x0041")]
    [InlineData(0x1F600, "0x01f600")] // lowercase hex, matching Java's Integer.toString(c, 16)
    public void Format_PadsToBmpWidth(int c, string expected)
        => Assert.Equal(expected, CharUtilities.Format(c));

    [Fact]
    public void Format_RejectsOutOfRange()
        => Assert.Equal("!NOT A CHARACTER!", CharUtilities.Format(1114112));

    [Theory]
    [InlineData(0x0041, true)]
    [InlineData(0x1F600, false)]
    public void IsBmpCodePoint_Works(int codePoint, bool expected)
        => Assert.Equal(expected, CharUtilities.IsBmpCodePoint(codePoint));

    [Fact]
    public void CodePoints_HandlesSurrogatePairs()
    {
        // "A" + U+1F600 (grinning face, a surrogate pair) + "B"
        string s = "A\U0001F600B";
        Assert.Equal(new[] { 0x41, 0x1F600, 0x42 }, CharUtilities.CodePoints(s));
    }

    [Fact]
    public void ContainsSurrogatePairAt_DetectsWellFormedPair()
        => Assert.True(CharUtilities.ContainsSurrogatePairAt("\U0001F600", 0));

    [Fact]
    public void ContainsSurrogatePairAt_ThrowsOnIsolatedLowSurrogate()
        => Assert.Throws<ArgumentException>(() => CharUtilities.ContainsSurrogatePairAt("\uDE00", 0));
}
