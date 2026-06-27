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

public class TypefaceTests
{
    [Fact]
    public void NotFound_IsHash()
    {
        Assert.Equal('#', Typeface.NotFound);
    }

    [Fact]
    public void HadMappingOperations_StartsFalse_AndFlipsAfterMapping()
    {
        TestTypeface tf = new();
        Assert.False(tf.HadMappingOperations);
        tf.Map('A');
        Assert.True(tf.HadMappingOperations);
    }

    [Fact]
    public void IsMultiByte_DefaultsFalse()
    {
        Assert.False(new TestTypeface().IsMultiByte);
    }

    [Fact]
    public void GetMaxAscent_DefaultsToAscender()
    {
        TestTypeface tf = new();
        Assert.Equal(tf.GetAscender(12), tf.GetMaxAscent(12));
    }

    [Fact]
    public void HasFeature_DefaultsFalse()
    {
        Assert.False(new TestTypeface().HasFeature(0, "latn", "ENG", "liga"));
    }

    [Theory]
    [InlineData(FontType.Type1C, true)]
    [InlineData(FontType.TrueType, false)]
    public void IsCid_DependsOnFontType(FontType type, bool expected)
    {
        TestTypeface tf = new() { TypeOfFont = type };
        Assert.Equal(expected, tf.IsCid);
    }

    [Fact]
    public void ToString_WrapsFullNameInBraces()
    {
        TestTypeface tf = new();
        Assert.EndsWith("{Test Full Name}", tf.ToString());
    }

    [Fact]
    public void WarnMissingGlyph_DeduplicatesAndCapsAtEight()
    {
        TestTypeface tf = new();
        // De-duplication: warning the same char repeatedly counts once.
        tf.Warn('x');
        tf.Warn('x');
        Assert.Equal(1, tf.WarnedCount);

        // Cap at 8 distinct characters.
        for (int i = 0; i < 20; i++)
        {
            tf.Warn((char)('A' + i));
        }

        Assert.Equal(8, tf.WarnedCount);
    }

    private sealed class TestTypeface : Typeface
    {
        public FontType TypeOfFont { get; init; } = FontType.TrueType;

        public int WarnedCount => WarnedGlyphCount;

        public void Map(char c) => NotifyMapOperation();

        public void Warn(char c) => WarnMissingGlyph(c);

        public override string EncodingName => "TestEncoding";

        public override Uri FontUri => new("file:///test.ttf");

        public override string FontName => "TestFont";

        public override string FullName => "Test Full Name";

        public override IReadOnlySet<string> FamilyNames => new HashSet<string> { "Test" };

        public override string EmbedFontName => "TestFont";

        public override FontType FontType => TypeOfFont;

        public override int GetAscender(int size) => size * 8;

        public override int GetCapHeight(int size) => size * 7;

        public override int GetDescender(int size) => -size * 2;

        public override int GetXHeight(int size) => size * 5;

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
            return c;
        }

        public override bool HasChar(char c) => true;
    }
}
