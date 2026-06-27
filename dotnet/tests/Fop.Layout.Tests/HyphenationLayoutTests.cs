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

using Fop.Fo;
using Fop.Layout;

using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Tests that hyphenation hooks into greedy line breaking when a block enables it, and is a no-op when
/// off (the default). Uses the deterministic <see cref="FakeFontMeasurer"/> (500 units/glyph at
/// 1000mpt, so a 10pt glyph and the inter-word space are each 5000mpt).
/// </summary>
public sealed class HyphenationLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    /// <summary>A deterministic stub: splits any word at the given fixed offsets, ignoring counts.</summary>
    private sealed class StubHyphenator(params int[] points) : ILineHyphenator
    {
        public int[]? Hyphenate(string? language, string? country, string word, int remain, int push)
        {
            // Only return points strictly inside the word.
            var valid = points.Where(p => p > 0 && p < word.Length).ToArray();
            return valid.Length > 0 ? valid : null;
        }
    }

    private static AreaTree LayOut(string fo, ILineHyphenator? hyphenator)
    {
        var engine = new LayoutEngine(Measurer) { Hyphenator = hyphenator };
        return engine.LayOut(FoTreeBuilder.ParseString(fo));
    }

    private static string Document(string body, double pageWidthPt, double pageHeightPt = 1000) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="{pageWidthPt}pt" page-height="{pageHeightPt}pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              {body}
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void HyphenateOn_SplitsLongWordWithHyphenAcrossTwoLines()
    {
        // "ab" (10000) fits; the space + "longword" (5000 + 40000) overflows width 30000.
        // The stub splits "longword" at offset 4 ("long" | "word"). On the line after "ab" the remaining
        // width is 30000 - 10000 - 5000(space) = 15000; fragment "long"(20000)+"-"(5000) does NOT fit,
        // but offset 4 is the only point; so this checks a point that fits: use offset 2 ("lo").
        // "lo"(10000) + "-"(5000) = 15000 <= 15000 fits.
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30), new StubHyphenator(2, 4));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal(2, page.TextRuns.Count);
        Assert.Equal("ab lo-", page.TextRuns[0].Text);
        Assert.Equal("ngword", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOff_ByDefault_DoesNotSplit()
    {
        // Same content, hyphenate not set (default off): the long word wraps whole to the next line.
        string body = "<fo:block font-size=\"10pt\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30), new StubHyphenator(2, 4));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal(2, page.TextRuns.Count);
        Assert.Equal("ab", page.TextRuns[0].Text);
        Assert.Equal("longword", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_NoHyphenatorConfigured_DoesNotSplit()
    {
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30), hyphenator: null);
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal(2, page.TextRuns.Count);
        Assert.Equal("ab", page.TextRuns[0].Text);
        Assert.Equal("longword", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_ChoosesLastFittingPoint()
    {
        // Width 50000. Full "ab longword" = 10000+5000+40000 = 55000 overflows. Remaining after "ab" is
        // 50000-10000-5000 = 35000. Points at 2 ("lo": 10000+5000=15000) and 6 ("longwo": 30000+5000=35000)
        // both fit; the later point (6) is preferred for a tighter line.
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 50), new StubHyphenator(2, 6));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal("ab longwo-", page.TextRuns[0].Text);
        Assert.Equal("rd", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_NoFittingPoint_FallsBackToWrap()
    {
        // Remaining after "ab" is 15000; the only point (8 -> "longword"[..8] = 40000) does not fit.
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30), new StubHyphenator(8));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal("ab", page.TextRuns[0].Text);
        Assert.Equal("longword", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_UnknownLanguageWithRealPatterns_DoesNotSplit()
    {
        // The default (real) hyphenator has no patterns for "zz", so the word wraps whole.
        var engine = new LayoutEngine(Measurer);   // default DefaultLineHyphenator
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"zz\">ab hyphenation</fo:block>";
        AreaTree tree = engine.LayOut(FoTreeBuilder.ParseString(Document(body, pageWidthPt: 40)));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal("ab", page.TextRuns[0].Text);
        Assert.Equal("hyphenation", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_RealEnglishPatterns_SplitsHyphenation()
    {
        // Real bundled en patterns: "hyphenation" hyphenates hy-phen-ation (points 2 and 6).
        // Width 60pt: "ab"(10000)+space(5000) leaves 45000. "hyphen"(30000)+"-"(5000)=35000 fits
        // (point 6); "hyphenation"[..6] = "hyphen".
        var engine = new LayoutEngine(Measurer);
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab hyphenation</fo:block>";
        AreaTree tree = engine.LayOut(FoTreeBuilder.ParseString(Document(body, pageWidthPt: 60)));
        PageArea page = Assert.Single(tree.Pages);

        Assert.Equal("ab hyphen-", page.TextRuns[0].Text);
        Assert.Equal("ation", page.TextRuns[1].Text);
    }

    [Fact]
    public void HyphenateOn_LoneOverWideFirstWord_IsSplit()
    {
        // A single word wider than the whole line is split from scratch (no preceding word).
        // Width 30000; "longword"(40000) overflows. Point 2 ("lo": 10000+5000=15000 <= 30000) fits.
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">longword</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30), new StubHyphenator(2, 6));
        PageArea page = Assert.Single(tree.Pages);

        // Point 6 ("longwo": 30000+5000=35000) does not fit in 30000; point 2 does.
        Assert.Equal("lo-", page.TextRuns[0].Text);
        Assert.Equal("ngword", page.TextRuns[1].Text);
    }
}
