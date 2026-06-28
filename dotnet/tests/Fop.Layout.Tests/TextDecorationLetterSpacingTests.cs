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
using Fop.Colors;
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
    public void UnderlineIsCarriedAsRunTrait()
    {
        // Decoration is a trait on the run (the renderer paints the lines over the glyphs), not a
        // RectFill in the area tree.
        AreaTree tree = LayOut(Document("text-decoration=\"underline\"", "Hello"));
        PageArea page = tree.Pages[0];
        TextRun run = Assert.Single(page.TextRuns);
        Assert.True(run.Decoration.HasUnderline);
        Assert.False(run.Decoration.HasOverline);
        Assert.False(run.Decoration.HasLineThrough);
        Assert.Empty(page.RectFills);
    }

    [Fact]
    public void CombinedDecorationsSetBothFlags()
    {
        AreaTree tree = LayOut(Document("text-decoration=\"underline overline\"", "Hi"));
        TextRun run = Assert.Single(tree.Pages[0].TextRuns);
        Assert.True(run.Decoration.HasUnderline);
        Assert.True(run.Decoration.HasOverline);
        Assert.False(run.Decoration.HasLineThrough);
    }

    [Fact]
    public void NoDecorationLeavesRunUndecorated()
    {
        AreaTree tree = LayOut(Document(string.Empty, "Hi"));
        TextRun run = Assert.Single(tree.Pages[0].TextRuns);
        Assert.True(run.Decoration.IsNone);
        Assert.Empty(tree.Pages[0].RectFills);
    }

    [Fact]
    public void InlineDecorationAppliesToInlineRunOnly()
    {
        // Only the inner inline is underlined; the surrounding text is a separate, undecorated run.
        string content = "plain <fo:inline text-decoration=\"underline\">marked</fo:inline>";
        AreaTree tree = LayOut(Document(string.Empty, content));
        TextRun plain = Assert.Single(tree.Pages[0].TextRuns, r => r.Text == "plain");
        TextRun marked = Assert.Single(tree.Pages[0].TextRuns, r => r.Text == "marked");
        Assert.True(plain.Decoration.IsNone);
        Assert.True(marked.Decoration.HasUnderline);
    }

    [Fact]
    public void DecorationColorComesFromDeclaringElementNotCoveredText()
    {
        // FOP: the underline keeps the colour of the FO that declared it (red on the block), even
        // though the covered inline text is blue.
        string content = "<fo:inline color=\"blue\">word</fo:inline>";
        AreaTree tree = LayOut(Document("text-decoration=\"underline\" color=\"red\"", content));
        TextRun run = Assert.Single(tree.Pages[0].TextRuns, r => r.Text == "word");
        Assert.Equal(0, run.Color.Red);       // text is blue
        Assert.Equal(255, run.Color.Blue);
        Assert.NotNull(run.Decoration.UnderlineColor);
        Assert.Equal(255, run.Decoration.UnderlineColor!.Red);   // underline is red
        Assert.Equal(0, run.Decoration.UnderlineColor!.Blue);
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
    public void TextDecorationResolutionHandlesNoUnderlineAndNone()
    {
        FopColor red = FopColor.FromRgb(255, 0, 0);
        var underlined = new TextDecorationTraits(red, null, null);

        // no-underline clears an inherited underline.
        Assert.True(FoEnumParsing.ResolveTextDecoration("no-underline", underlined, red).IsNone);

        // overline adds to the inherited underline (both active).
        TextDecorationTraits both = FoEnumParsing.ResolveTextDecoration("overline", underlined, red);
        Assert.True(both.HasUnderline);
        Assert.True(both.HasOverline);

        // none clears everything.
        var all = new TextDecorationTraits(red, red, red);
        Assert.True(FoEnumParsing.ResolveTextDecoration("none", all, red).IsNone);
    }
}
