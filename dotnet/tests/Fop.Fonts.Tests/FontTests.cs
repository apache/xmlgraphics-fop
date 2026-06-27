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

using System.Drawing;
using Fop.Fonts;
using Xunit;

namespace Fop.Fonts.Tests;

public class FontTests
{
    private static Font NewFont(IFontMetrics metrics, int fontSize = 12000) =>
        new("F1", new FontTriplet("Test", "normal", 400), metrics, fontSize);

    [Fact]
    public void DefaultFont_MatchesConstants()
    {
        Assert.Equal("any", Font.DefaultFont.Name);
        Assert.Equal(Font.StyleNormal, Font.DefaultFont.Style);
        Assert.Equal(Font.WeightNormal, Font.DefaultFont.Weight);
        Assert.Equal(400, Font.WeightNormal);
        Assert.Equal(700, Font.WeightBold);
    }

    [Fact]
    public void MetricAccessors_DivideByThousand()
    {
        // metrics return millipoints; the Font accessors divide by 1000.
        StubMetrics metrics = new();
        Font font = NewFont(metrics, 12000);
        Assert.Equal(metrics.GetAscender(12000) / 1000, font.Ascender);
        Assert.Equal(metrics.GetCapHeight(12000) / 1000, font.CapHeight);
        Assert.Equal(metrics.GetDescender(12000) / 1000, font.Descender);
        Assert.Equal(metrics.GetXHeight(12000) / 1000, font.XHeight);
    }

    [Fact]
    public void GetWidth_DividesByThousand()
    {
        // StubMetrics.GetWidth returns 1000 * size for any char; /1000 -> size.
        Font font = NewFont(new StubMetrics(), 12000);
        Assert.Equal(12000, font.GetWidth('A'));
    }

    [Fact]
    public void GetKernValue_IgnoresSurrogateRangeAndScalesBySize()
    {
        StubMetrics metrics = new()
        {
            Kerning = new Dictionary<int, IReadOnlyDictionary<int, int>>
            {
                ['A'] = new Dictionary<int, int> { ['V'] = 1000 },
            },
        };
        Font font = NewFont(metrics, 12000);

        // 1000 (per-1000) * 12000 / 1000 = 12000
        Assert.Equal(12000, font.GetKernValue('A', 'V'));
        // No kern pair -> 0.
        Assert.Equal(0, font.GetKernValue('A', 'A'));
        // Surrogate-range first char -> 0 regardless.
        Assert.Equal(0, font.GetKernValue(0xD801, 'V'));
    }

    [Fact]
    public void NonTypefaceMetric_UsesWinAnsiCodePointMapping()
    {
        // A plain IFontMetrics (not a Typeface) routes MapChar/HasChar through WinAnsiEncoding.
        Font font = NewFont(new StubMetrics());
        Assert.True(font.HasChar('A'));
        Assert.Equal('A', font.MapChar('A')); // 0x41 -> 0x41
        // A char not in WinAnsi maps to Typeface.NotFound ('#').
        Assert.Equal(Typeface.NotFound, font.MapChar('\u4E00'));
        Assert.False(font.HasChar('\u4E00'));
    }

    [Fact]
    public void TypefaceMetric_DelegatesMapChar()
    {
        // A Typeface metric is asked directly (here it upper-cases as a marker behaviour).
        Font font = NewFont(new MappingTypeface());
        Assert.Equal('Z', font.MapChar('z'));
        Assert.True(font.HasChar('z'));
    }

    [Fact]
    public void GetCharWidth_WhitespaceUsesSpaceWidth()
    {
        Font font = NewFont(new StubMetrics(), 12000);
        int spaceWidth = font.GetCharWidth(' ');
        Assert.Equal(spaceWidth, font.GetCharWidth('\n'));
        Assert.Equal(spaceWidth, font.GetCharWidth('\t'));
        Assert.Equal(spaceWidth, font.GetCharWidth('\u00A0'));
    }

    [Fact]
    public void GetCharWidth_GuessesSpecialSpaces()
    {
        // Use a metric that has no glyph for the special spaces so the guessing table kicks in.
        Font font = NewFont(new EmptyMetrics(), 12000);
        int em = 12000;
        Assert.Equal(em, font.GetCharWidth(' ')); // SPACE -> em
        Assert.Equal(em / 2, font.GetCharWidth('\u2000')); // EN QUAD -> en
        Assert.Equal(0, font.GetCharWidth('\u200B')); // ZERO WIDTH SPACE
        Assert.Equal(0, font.GetCharWidth('\u2060')); // WORD JOINER
        Assert.Equal(0, font.GetCharWidth('\uFEFF')); // ZERO WIDTH NO-BREAK SPACE
    }

    [Fact]
    public void GetWordWidth_SumsCharWidths()
    {
        Font font = NewFont(new StubMetrics(), 12000);
        Assert.Equal(0, font.GetWordWidth(null));
        // "AB": each maps and returns GetWidth = 12000.
        Assert.Equal(font.GetCharWidth('A') + font.GetCharWidth('B'), font.GetWordWidth("AB"));
    }

    [Fact]
    public void Substitution_DefaultsToFalseForPlainMetric()
    {
        Font font = NewFont(new StubMetrics());
        Assert.False(font.PerformsSubstitution());
        Assert.False(font.PerformsPositioning());
        Assert.Throws<NotSupportedException>(
            () => font.PerformPositioning("x", "latn", "ENG"));
    }

    // A minimal IFontMetrics that is NOT a Typeface, with configurable kerning.
    private class StubMetrics : IFontMetrics
    {
        public IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> Kerning { get; init; } =
            new Dictionary<int, IReadOnlyDictionary<int, int>>();

        public Uri FontUri => new("file:///stub.ttf");

        public string FontName => "Stub";

        public string FullName => "Stub Font";

        public IReadOnlySet<string> FamilyNames => new HashSet<string> { "Stub" };

        public string EmbedFontName => "Stub";

        public FontType FontType => FontType.TrueType;

        public virtual int GetMaxAscent(int size) => GetAscender(size);

        public virtual int GetAscender(int size) => size * 8 / 10;

        public int GetCapHeight(int size) => size * 7 / 10;

        public int GetDescender(int size) => -size * 2 / 10;

        public int GetXHeight(int size) => size * 5 / 10;

        public virtual int GetWidth(int i, int size) => 1000 * size;

        public int[] GetWidths() => [];

        public Rectangle GetBoundingBox(int glyphIndex, int size) => Rectangle.Empty;

        public bool HasKerningInfo => Kerning.Count > 0;

        public IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerningInfo() => Kerning;

        public int GetUnderlinePosition(int size) => -size;

        public int GetUnderlineThickness(int size) => size / 10;

        public int GetStrikeoutPosition(int size) => size / 2;

        public int GetStrikeoutThickness(int size) => size / 10;

        public bool HasFeature(int tableType, string script, string language, string feature) => false;

        public bool IsMultiByte => false;
    }

    // A StubMetrics whose GetWidth is always 0 so the GetCharWidth space-guessing table is used.
    private sealed class EmptyMetrics : StubMetrics
    {
        public override int GetWidth(int i, int size) => 0;
    }

    // A Typeface that maps via char.ToUpperInvariant, to verify the Typeface delegation path.
    private sealed class MappingTypeface : Typeface
    {
        public override string EncodingName => "Test";

        public override Uri FontUri => new("file:///t.ttf");

        public override string FontName => "T";

        public override string FullName => "T Font";

        public override IReadOnlySet<string> FamilyNames => new HashSet<string> { "T" };

        public override string EmbedFontName => "T";

        public override FontType FontType => FontType.TrueType;

        public override int GetAscender(int size) => size;

        public override int GetCapHeight(int size) => size;

        public override int GetDescender(int size) => -size;

        public override int GetXHeight(int size) => size;

        public override int GetWidth(int i, int size) => size;

        public override int[] GetWidths() => [];

        public override Rectangle GetBoundingBox(int glyphIndex, int size) => Rectangle.Empty;

        public override bool HasKerningInfo => false;

        public override IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerningInfo() =>
            new Dictionary<int, IReadOnlyDictionary<int, int>>();

        public override int GetUnderlinePosition(int size) => -size;

        public override int GetUnderlineThickness(int size) => size / 10;

        public override int GetStrikeoutPosition(int size) => size / 2;

        public override int GetStrikeoutThickness(int size) => size / 10;

        public override char MapChar(char c)
        {
            NotifyMapOperation();
            return char.ToUpperInvariant(c);
        }

        public override bool HasChar(char c) => true;
    }
}
