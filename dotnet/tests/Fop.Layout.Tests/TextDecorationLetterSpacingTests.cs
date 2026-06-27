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

using System.Linq;
using Fop.Fo;
using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Layout tests for the <c>text-decoration</c> (underline/overline/line-through rules) and
/// <c>letter-spacing</c> (per-glyph tracking widening line advance) polish, over the deterministic
/// <see cref="FakeFontMeasurer"/> (500 units/glyph at 1000mpt).
/// </summary>
public sealed class TextDecorationLetterSpacingTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string blockAttrs, string content) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="400pt" page-height="200pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              <fo:block font-size="10pt" {blockAttrs}>{content}</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void UnderlineEmitsOneRuleUnderText()
    {
        AreaTree tree = LayOut(Document("text-decoration=\"underline\"", "Hello"));
        PageArea page = tree.Pages[0];
        TextRun run = Assert.Single(page.TextRuns);
        // One decoration rule spanning the run, positioned below the baseline.
        RectFill rule = Assert.Single(page.RectFills);
        Assert.True(rule.YMpt > run.BaselineYMpt, "underline should sit below the baseline");
        Assert.Equal(run.XMpt, rule.XMpt, 1);
    }

    [Fact]
    public void OverlineSitsAboveBaseline()
    {
        AreaTree tree = LayOut(Document("text-decoration=\"overline\"", "Hello"));
        PageArea page = tree.Pages[0];
        TextRun run = Assert.Single(page.TextRuns);
        RectFill rule = Assert.Single(page.RectFills);
        Assert.True(rule.YMpt < run.BaselineYMpt, "overline should sit above the baseline");
    }

    [Fact]
    public void CombinedDecorationsEmitTwoRules()
    {
        AreaTree tree = LayOut(Document("text-decoration=\"underline overline\"", "Hi"));
        Assert.Equal(2, tree.Pages[0].RectFills.Count);
    }

    [Fact]
    public void NoDecorationEmitsNoRules()
    {
        AreaTree tree = LayOut(Document(string.Empty, "Hi"));
        Assert.Empty(tree.Pages[0].RectFills);
    }

    [Fact]
    public void InlineDecorationAppliesToInlineOnly()
    {
        // Only the inner inline is underlined; the surrounding text is not.
        string content = "plain <fo:inline text-decoration=\"underline\">marked</fo:inline>";
        AreaTree tree = LayOut(Document(string.Empty, content));
        // Exactly one underline rule (for the inline run).
        RectFill rule = Assert.Single(tree.Pages[0].RectFills);
        // Its width matches "marked" (6 glyphs * 500 units * 10pt/1000 = 30000mpt).
        Assert.Equal(30_000, rule.WidthMpt, 1);
    }

    [Fact]
    public void LetterSpacingWidensRunAdvance()
    {
        // "AAAA" at 10pt: glyphs 4*5000 = 20000mpt; with 2pt letter-spacing add 4*2000 = 8000 -> the run
        // is emitted as a single TextRun carrying the spacing, positioned at the content origin.
        AreaTree tree = LayOut(Document("letter-spacing=\"2pt\"", "AAAA"));
        TextRun run = Assert.Single(tree.Pages[0].TextRuns);
        Assert.Equal("AAAA", run.Text);
        Assert.Equal(2_000, run.LetterSpacingMpt, 1);
    }

    [Fact]
    public void LetterSpacingPushesSecondWordFurther()
    {
        // Two words; with letter-spacing the words do not coalesce, and the second word's x reflects
        // the first word's widened advance.
        AreaTree spaced = LayOut(Document("letter-spacing=\"3pt\" text-align=\"start\"", "AA BB"));
        AreaTree plain = LayOut(Document("text-align=\"start\"", "AA BB"));

        double spacedSecondX = spaced.Pages[0].TextRuns.OrderBy(r => r.XMpt).Last().XMpt;
        double plainSecondX = plain.Pages[0].TextRuns.OrderBy(r => r.XMpt).Last().XMpt;
        Assert.True(spacedSecondX > plainSecondX,
            $"letter-spacing should push the second word right ({spacedSecondX} vs {plainSecondX})");
    }

    [Fact]
    public void TextDecorationParsingHandlesNoUnderline()
    {
        // no-underline clears an inherited underline.
        Assert.Equal(TextDecoration.None,
            FoEnumParsing.ParseTextDecoration("no-underline", TextDecoration.Underline));
        Assert.Equal(TextDecoration.Underline | TextDecoration.Overline,
            FoEnumParsing.ParseTextDecoration("overline", TextDecoration.Underline));
        Assert.Equal(TextDecoration.None,
            FoEnumParsing.ParseTextDecoration("none", TextDecoration.Underline | TextDecoration.Overline));
    }
}
