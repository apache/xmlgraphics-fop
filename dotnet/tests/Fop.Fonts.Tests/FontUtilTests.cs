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

using Fop.Fonts;
using Xunit;

namespace Fop.Fonts.Tests;

public class FontUtilTests
{
    [Theory]
    [InlineData("normal", 400)]
    [InlineData("bold", 700)]
    [InlineData("100", 100)]
    [InlineData("400", 400)]
    [InlineData("900", 900)]
    public void ParseCss2FontWeight_KnownValues(string text, int expected)
    {
        Assert.Equal(expected, FontUtil.ParseCss2FontWeight(text));
    }

    [Theory]
    [InlineData("150", 100)] // (150/100)*100 = 100
    [InlineData("250", 200)]
    [InlineData("999", 900)] // (999/100)*100 = 900
    public void ParseCss2FontWeight_RoundsDownToHundreds(string text, int expected)
    {
        Assert.Equal(expected, FontUtil.ParseCss2FontWeight(text));
    }

    [Theory]
    [InlineData("0", 100)]    // clamped up to 100
    [InlineData("50", 100)]   // clamped up to 100
    [InlineData("1000", 900)] // clamped down to 900
    [InlineData("5000", 900)]
    public void ParseCss2FontWeight_Clamps(string text, int expected)
    {
        Assert.Equal(expected, FontUtil.ParseCss2FontWeight(text));
    }

    [Theory]
    [InlineData("lighter")]
    [InlineData("bolder")]
    [InlineData("400.5")]
    [InlineData("")]
    [InlineData("heavy")]
    public void ParseCss2FontWeight_RejectsIllegalValues(string text)
    {
        Assert.Throws<ArgumentException>(() => FontUtil.ParseCss2FontWeight(text));
    }

    [Theory]
    [InlineData("Helvetica Bold Oblique", "HelveticaBoldOblique")]
    [InlineData("a b\tc\r\nd", "abcd")]
    [InlineData("nospace", "nospace")]
    public void StripWhiteSpace_RemovesAsciiWhitespace(string input, string expected)
    {
        Assert.Equal(expected, FontUtil.StripWhiteSpace(input));
    }

    [Fact]
    public void StripWhiteSpace_NullReturnsNull()
    {
        Assert.Null(FontUtil.StripWhiteSpace(null));
    }

    [Theory]
    [InlineData("Helvetica-italic", "italic")]
    [InlineData("SomeFont-oblique", "italic")]
    [InlineData("SomeFont-inclined", "italic")]
    [InlineData("Helvetica", "normal")]
    [InlineData("Times-bold", "normal")]
    public void GuessStyle_DetectsItalicWords(string fontName, string expected)
    {
        Assert.Equal(expected, FontUtil.GuessStyle(fontName));
    }

    [Fact]
    public void GuessStyle_IsCaseSensitive()
    {
        // Java uses case-sensitive substring search, so "Italic" (capital) is NOT detected.
        Assert.Equal("normal", FontUtil.GuessStyle("Helvetica-Italic"));
    }

    [Fact]
    public void GuessStyle_NullReturnsNormal()
    {
        Assert.Equal("normal", FontUtil.GuessStyle(null));
    }

    [Theory]
    [InlineData("Helvetica", 400)]            // no qualifier -> normal
    [InlineData("Helvetica-bold", 700)]       // bold
    [InlineData("SomeFont-medium", 500)]      // medium = 400 + 100
    [InlineData("SomeFont-light", 200)]       // light
    public void GuessWeight_SimpleQualifiers(string fontName, int expected)
    {
        Assert.Equal(expected, FontUtil.GuessWeight(fontName));
    }

    [Theory]
    [InlineData("SomeFont-extrabold", 800)]
    [InlineData("SomeFont-black", 800)]
    [InlineData("SomeFont-heavy", 800)]
    [InlineData("SomeFont-ultra", 800)]
    [InlineData("SomeFont-super", 800)]
    public void GuessWeight_ExtraBoldWords(string fontName, int expected)
    {
        Assert.Equal(expected, FontUtil.GuessWeight(fontName));
    }

    [Theory]
    [InlineData("SomeFont-demibold", 600)] // contains both "demi" and "bold"; last applicable wins by Java order
    [InlineData("SomeFont-semibold", 600)]
    public void GuessWeight_DemiSemiResolveTo600(string fontName, int expected)
    {
        // Java order: BOLD (700) then DEMI (overrides to 600); neither EXTRA_BOLD nor LIGHT matches.
        Assert.Equal(expected, FontUtil.GuessWeight(fontName));
    }

    [Fact]
    public void GuessWeight_LightWordWinsOverEarlierMatches()
    {
        // "light" is checked last in Java, so it overrides any earlier weight when present.
        Assert.Equal(200, FontUtil.GuessWeight("SomeFont-light"));
    }
}
