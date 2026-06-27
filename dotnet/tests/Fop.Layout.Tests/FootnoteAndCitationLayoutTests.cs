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
using Fop.Layout;

using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Layout tests for page-number citations (two-pass resolution) and footnotes (page-bottom
/// placement + content-height reserve), over the deterministic <see cref="FakeFontMeasurer"/>
/// (500 units/glyph at 1000mpt, ascender 800, descender 200, scaled by font size).
/// </summary>
public sealed class FootnoteAndCitationLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>
    /// A page 200pt tall with a region-after band of 20pt at the bottom (180..200pt), the body inset
    /// by matching region-body margins (content rectangle 20..180pt). Default font 10pt: each short
    /// block is one 12pt line.
    /// </summary>
    private static string Document(string body) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
              <fo:region-body margin-top="20pt" margin-bottom="20pt"/>
              <fo:region-before extent="20pt"/>
              <fo:region-after extent="20pt"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    /// <summary>N blocks each holding a single short word (one 10pt line, 12pt high).</summary>
    private static string Blocks(int n) =>
        string.Concat(Enumerable.Range(0, n).Select(i => $"<fo:block>B{i}</fo:block>"));

    // ----- Page-number citation ----------------------------------------------------------------

    [Fact]
    public void CitationResolvesToTheReferencedIdsPage()
    {
        // The "target" block carries id="t". A citation in a later block references it; both land on the
        // same (only) page, so the citation reads "1".
        string fo = Document(
            "<fo:block id=\"t\">Target</fo:block>" +
            "<fo:block>See <fo:page-number-citation ref-id=\"t\"/></fo:block>");

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "See 1");
    }

    [Fact]
    public void ForwardCitationResolvesViaTwoPass()
    {
        // A citation on page 1 references an id whose block lands on page 2 (a forward reference). Each
        // block is one 12pt line; the content rectangle is 160pt tall (~13 lines), so 20 blocks before
        // the target push it onto page 2. The two-pass layout resolves the forward reference to "2".
        string fo = Document(
            "<fo:block>See <fo:page-number-citation ref-id=\"later\"/></fo:block>" +
            Blocks(20) +
            "<fo:block id=\"later\">Later</fo:block>");

        AreaTree tree = LayOut(fo);
        Assert.True(tree.Pages.Count >= 2, $"expected the target on a later page, got {tree.Pages.Count} pages");

        // The citation sits on page 1 and resolves to the target's page (2).
        int targetPage = -1;
        for (int i = 0; i < tree.Pages.Count; i++)
        {
            if (tree.Pages[i].TextRuns.Any(r => r.Text == "Later"))
            {
                targetPage = i + 1;
            }
        }

        Assert.True(targetPage >= 2, $"target landed on page {targetPage}");
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text == $"See {targetPage}");
    }

    [Fact]
    public void UnknownRefIdResolvesToQuestionMark()
    {
        string fo = Document("<fo:block>See <fo:page-number-citation ref-id=\"nope\"/></fo:block>");

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "See ?");
    }

    [Fact]
    public void CitationLastResolvesToReferencedPage()
    {
        // With the flat model, page-number-citation-last reads the same page recorded for the id.
        string fo = Document(
            "<fo:block id=\"t\">Target</fo:block>" +
            "<fo:block>Last <fo:page-number-citation-last ref-id=\"t\"/></fo:block>");

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "Last 1");
    }

    [Fact]
    public void InlineLevelIdIsResolvable()
    {
        // An id on an inline (not a block) is also recorded against the page its containing block lands on.
        string fo = Document(
            "<fo:block>Here is <fo:inline id=\"mark\">a mark</fo:inline></fo:block>" +
            "<fo:block>Ref <fo:page-number-citation ref-id=\"mark\"/></fo:block>");

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "Ref 1");
    }

    // ----- Footnotes ---------------------------------------------------------------------------

    private const string OneFootnote = """
        <fo:block>Anchor<fo:footnote>
          <fo:inline>1</fo:inline>
          <fo:footnote-body><fo:block>NOTEONE</fo:block></fo:footnote-body>
        </fo:footnote></fo:block>
        """;

    [Fact]
    public void FootnoteAnchorFlowsInlineInBody()
    {
        AreaTree tree = LayOut(Document(OneFootnote));
        PageArea page = Assert.Single(tree.Pages);

        // The anchor content (the fo:inline "1") flows inline with the body word "Anchor".
        Assert.Contains(page.TextRuns, r => r.Text.Contains("Anchor"));
        Assert.Contains(page.TextRuns, r => r.Text.Contains("1"));
    }

    [Fact]
    public void FootnoteBodyAppearsNearPageBottomBelowBodyContent()
    {
        AreaTree tree = LayOut(Document(OneFootnote));
        PageArea page = Assert.Single(tree.Pages);

        TextRun anchor = page.TextRuns.Single(r => r.Text.Contains("Anchor"));
        TextRun note = page.TextRuns.Single(r => r.Text == "NOTEONE");

        // The footnote body sits below the body content (anchor near the top, note near the bottom)...
        Assert.True(note.BaselineYMpt > anchor.BaselineYMpt,
            $"note {note.BaselineYMpt} should be below anchor {anchor.BaselineYMpt}");

        // ...within the body content rectangle's lower part (above the region-after band at 180pt).
        Assert.True(note.BaselineYMpt > 140_000 && note.BaselineYMpt <= 180_000,
            $"note baseline {note.BaselineYMpt} not near the page bottom (above the region-after band)");
    }

    [Fact]
    public void FootnoteAreaHasSeparatorRuleAboveIt()
    {
        AreaTree tree = LayOut(Document(OneFootnote));
        PageArea page = Assert.Single(tree.Pages);

        TextRun note = page.TextRuns.Single(r => r.Text == "NOTEONE");

        // A thin separator rule (RectFill) sits above the footnote body, within the body content width.
        Assert.Contains(page.RectFills,
            f => f.YMpt < note.BaselineYMpt && f.YMpt > 120_000 && f.HeightMpt < 2_000);
    }

    [Fact]
    public void FootnoteReserveReducesBodyContentHeight()
    {
        // Without a footnote, N short blocks fit on one page; with a footnote reserving bottom space,
        // the same N blocks paginate earlier (the last block spills to a new page).
        // The content rectangle is 160pt (~13 lines of 12pt). Use 12 blocks plus a footnoted block.
        string noFootnote = Document(Blocks(13));
        AreaTree without = LayOut(noFootnote);

        string withFootnote = Document(Blocks(12) +
            "<fo:block>Anchor<fo:footnote>" +
            "<fo:inline>1</fo:inline>" +
            "<fo:footnote-body><fo:block>NOTE</fo:block><fo:block>NOTE2</fo:block></fo:footnote-body>" +
            "</fo:footnote></fo:block>");
        AreaTree with = LayOut(withFootnote);

        Assert.Single(without.Pages);
        Assert.True(with.Pages.Count > without.Pages.Count,
            $"footnote reserve should force pagination: without={without.Pages.Count} with={with.Pages.Count}");
    }

    [Fact]
    public void TwoFootnotesOnAPageStack()
    {
        string body =
            "<fo:block>A<fo:footnote><fo:inline>1</fo:inline>" +
            "<fo:footnote-body><fo:block>FIRSTNOTE</fo:block></fo:footnote-body></fo:footnote></fo:block>" +
            "<fo:block>B<fo:footnote><fo:inline>2</fo:inline>" +
            "<fo:footnote-body><fo:block>SECONDNOTE</fo:block></fo:footnote-body></fo:footnote></fo:block>";

        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun first = page.TextRuns.Single(r => r.Text == "FIRSTNOTE");
        TextRun second = page.TextRuns.Single(r => r.Text == "SECONDNOTE");

        // Both bodies are on the same page, stacked in source order (first above second).
        Assert.True(second.BaselineYMpt > first.BaselineYMpt,
            $"second note {second.BaselineYMpt} should stack below first {first.BaselineYMpt}");

        // Both lie in the lower part of the body content rectangle.
        Assert.True(first.BaselineYMpt > 120_000 && second.BaselineYMpt <= 180_000);
    }
}
