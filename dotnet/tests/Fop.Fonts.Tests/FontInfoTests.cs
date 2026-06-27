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

public class FontInfoTests
{
    private static FontInfo NewFontInfo()
    {
        FontInfo fi = new();
        // Register one metrics object per internal key used below.
        fi.AddMetrics("F1", new NamedMetrics("Helvetica"));
        fi.AddMetrics("F2", new NamedMetrics("Helvetica-Bold"));
        fi.AddMetrics("F3", new NamedMetrics("Times"));
        fi.AddMetrics("Fany", new NamedMetrics("Any"));
        return fi;
    }

    [Fact]
    public void AddFontProperties_RegistersTripletAndKey()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        Assert.True(fi.HasFont("Helvetica", Font.StyleNormal, Font.WeightNormal));
        FontTriplet key = FontInfo.CreateFontKey("Helvetica", Font.StyleNormal, Font.WeightNormal);
        Assert.Equal("F1", fi.GetInternalFontKey(key));
    }

    [Fact]
    public void AddFontProperties_FamiliesArray_RegistersAll()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", ["Helvetica", "Arial"], Font.StyleNormal, Font.WeightNormal);
        Assert.True(fi.HasFont("Helvetica", Font.StyleNormal, Font.WeightNormal));
        Assert.True(fi.HasFont("Arial", Font.StyleNormal, Font.WeightNormal));
    }

    [Fact]
    public void FontLookup_ExactMatch_ReturnsTriplet()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);

        FontTriplet? triplet = fi.FontLookup("Helvetica", Font.StyleNormal, Font.WeightNormal);
        Assert.NotNull(triplet);
        Assert.Equal("Helvetica", triplet!.Name);
        Assert.Equal("F1", fi.GetInternalFontKey(triplet));
    }

    [Fact]
    public void FontLookup_FallsBackToAnyWhenFamilyUnknown()
    {
        FontInfo fi = NewFontInfo();
        // Register the ultimate fallback "any" font (Font.DefaultFont triplet).
        fi.AddFontProperties("Fany", "any", Font.StyleNormal, Font.WeightNormal);

        // Looking up an unknown family in normal/normal should resolve to the "any" fallback.
        FontTriplet? triplet = fi.FontLookup("NoSuchFamily", Font.StyleNormal, Font.WeightNormal);
        Assert.NotNull(triplet);
        Assert.Equal("any", triplet!.Name);
    }

    [Fact]
    public void FontLookup_AdjustsWeightWhenExactWeightMissing()
    {
        FontInfo fi = NewFontInfo();
        // Only bold (700) registered for Helvetica.
        fi.AddFontProperties("F2", "Helvetica", Font.StyleNormal, Font.WeightBold);

        // Asking for weight 800 (>500) should walk up/down and find the 700 face.
        FontTriplet? triplet = fi.FontLookup("Helvetica", Font.StyleNormal, 800);
        Assert.NotNull(triplet);
        Assert.Equal("F2", fi.GetInternalFontKey(triplet!));
        Assert.Equal(Font.WeightBold, triplet!.Weight);
    }

    [Fact]
    public void FindAdjustWeight_NormalRequestPicks400()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, 400);

        FontTriplet? key = fi.FindAdjustWeight("Helvetica", Font.StyleNormal, 500);
        Assert.NotNull(key);
        Assert.Equal(400, key!.Weight);
    }

    [Fact]
    public void AddFontProperties_PriorityKeepsLowerPriorityNumber()
    {
        FontInfo fi = NewFontInfo();
        // priority is the 4th triplet arg; lower number wins (Java: oldPriority < newPriority keeps old).
        FontTriplet pri0 = new("Helvetica", Font.StyleNormal, 400, 0);
        FontTriplet pri5 = new("Helvetica", Font.StyleNormal, 400, 5);

        fi.AddFontProperties("F1", pri0);
        // Equal triplet (priority ignored in equality) with higher priority number must NOT replace.
        fi.AddFontProperties("F2", pri5);
        Assert.Equal("F1", fi.GetInternalFontKey(pri0));

        // A lower priority number than the current (0) does replace.
        FontTriplet priMinus = new("Helvetica", Font.StyleNormal, 400, -1);
        fi.AddFontProperties("F3", priMinus);
        Assert.Equal("F3", fi.GetInternalFontKey(priMinus));
    }

    [Fact]
    public void GetFontInstance_IsCachedPerSize()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        FontTriplet triplet = fi.FontLookup("Helvetica", Font.StyleNormal, Font.WeightNormal)!;

        Font a = fi.GetFontInstance(triplet, 12000);
        Font b = fi.GetFontInstance(triplet, 12000);
        Font c = fi.GetFontInstance(triplet, 14000);
        Assert.Same(a, b);
        Assert.NotSame(a, c);
        Assert.Equal(12000, a.FontSize);
    }

    [Fact]
    public void UseFont_AndGetMetricsFor_TrackUsedFonts()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        Assert.Empty(fi.GetUsedFonts());

        IFontMetrics metrics = fi.GetMetricsFor("F1");
        Assert.Equal("Helvetica", metrics.FontName);
        Assert.True(fi.GetUsedFonts().ContainsKey("F1"));
    }

    [Fact]
    public void IsSetupValid_RequiresDefaultFont()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        Assert.False(fi.IsSetupValid());

        FontInfo fi2 = NewFontInfo();
        fi2.AddFontProperties("Fany", "any", Font.StyleNormal, Font.WeightNormal);
        Assert.True(fi2.IsSetupValid());
    }

    [Fact]
    public void GetTripletsFor_AndTripletFor_AreSorted()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        fi.AddFontProperties("F1", "Helvetica", Font.StyleItalic, Font.WeightNormal);

        List<FontTriplet> triplets = fi.GetTripletsFor("F1");
        Assert.Equal(2, triplets.Count);

        // GetTripletFor sorts and returns the first; "italic" < "normal" ordinally.
        FontTriplet? first = fi.GetTripletFor("F1");
        Assert.NotNull(first);
        Assert.Equal(Font.StyleItalic, first!.Style);
        Assert.Equal(Font.StyleItalic, fi.GetFontStyleFor("F1"));
        Assert.Equal(Font.WeightNormal, fi.GetFontWeightFor("F1"));
    }

    [Fact]
    public void GetFontStyleFor_UnknownReturnsEmptyAndZero()
    {
        FontInfo fi = NewFontInfo();
        Assert.Equal(string.Empty, fi.GetFontStyleFor("F99"));
        Assert.Equal(0, fi.GetFontWeightFor("F99"));
    }

    [Fact]
    public void FontLookupArray_EmptyFamiliesThrows()
    {
        FontInfo fi = NewFontInfo();
        Assert.Throws<ArgumentException>(() => fi.FontLookup([], Font.StyleNormal, Font.WeightNormal));
    }

    [Fact]
    public void FontLookupArray_ReturnsMatchesAcrossFamilies()
    {
        FontInfo fi = NewFontInfo();
        fi.AddFontProperties("F1", "Helvetica", Font.StyleNormal, Font.WeightNormal);
        fi.AddFontProperties("F3", "Times", Font.StyleNormal, Font.WeightNormal);

        FontTriplet[] result =
            fi.FontLookup(["Helvetica", "Times"], Font.StyleNormal, Font.WeightNormal);
        Assert.Equal(2, result.Length);
    }

    // A simple non-Typeface IFontMetrics carrying a font name for registry tests.
    private sealed class NamedMetrics(string name) : IFontMetrics
    {
        public Uri FontUri => new("file:///named.ttf");

        public string FontName => name;

        public string FullName => name;

        public IReadOnlySet<string> FamilyNames => new HashSet<string> { name };

        public string EmbedFontName => name;

        public FontType FontType => FontType.TrueType;

        public int GetMaxAscent(int size) => size;

        public int GetAscender(int size) => size;

        public int GetCapHeight(int size) => size;

        public int GetDescender(int size) => -size;

        public int GetXHeight(int size) => size;

        public int GetWidth(int i, int size) => size;

        public int[] GetWidths() => [];

        public Rectangle GetBoundingBox(int glyphIndex, int size) => Rectangle.Empty;

        public bool HasKerningInfo => false;

        public IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerningInfo() =>
            new Dictionary<int, IReadOnlyDictionary<int, int>>();

        public int GetUnderlinePosition(int size) => -size;

        public int GetUnderlineThickness(int size) => size / 10;

        public int GetStrikeoutPosition(int size) => size / 2;

        public int GetStrikeoutThickness(int size) => size / 10;

        public bool HasFeature(int tableType, string script, string language, string feature) => false;

        public bool IsMultiByte => false;
    }
}
