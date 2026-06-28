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
/// Tests that <c>fo:page-number-citation</c> resolves ids that live inside relocatable buffers --
/// table cells, list items and footnote bodies -- which are recorded against the page their buffer is
/// placed on (previously such references rendered the "?" placeholder).
/// </summary>
public sealed class BufferedIdCitationTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string body) => new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString($"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="300pt" page-height="300pt">
              <fo:region-body margin-bottom="20pt"/>
              <fo:region-after extent="20pt"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
          </fo:page-sequence>
        </fo:root>
        """));

    // Citations coalesce with adjacent text into one run (e.g. "see 1"), so match on a substring.
    private static bool HasRun(AreaTree tree, string text) =>
        tree.Pages.SelectMany(p => p.TextRuns).Any(r => r.Text.Contains(text));

    [Fact]
    public void CitationResolvesIdInsideTableCell()
    {
        AreaTree tree = LayOut("""
            <fo:block>see <fo:page-number-citation ref-id="t"/></fo:block>
            <fo:table>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row><fo:table-cell><fo:block id="t">target</fo:block></fo:table-cell></fo:table-row>
              </fo:table-body>
            </fo:table>
            """);

        Assert.True(HasRun(tree, "1"), "citation to a table-cell id should resolve to page 1");
        Assert.False(HasRun(tree, "?"), "citation should not render the unresolved placeholder");
    }

    [Fact]
    public void CitationResolvesIdInsideListItem()
    {
        AreaTree tree = LayOut("""
            <fo:block>see <fo:page-number-citation ref-id="t"/></fo:block>
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block id="t">target</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        Assert.True(HasRun(tree, "1"), "citation to a list-item id should resolve");
        Assert.False(HasRun(tree, "?"));
    }

    [Fact]
    public void CitationResolvesIdInsideFootnoteBody()
    {
        AreaTree tree = LayOut("""
            <fo:block>see <fo:page-number-citation ref-id="t"/></fo:block>
            <fo:block>anchor<fo:footnote>
              <fo:inline>1</fo:inline>
              <fo:footnote-body><fo:block id="t">note</fo:block></fo:footnote-body>
            </fo:footnote></fo:block>
            """);

        Assert.True(HasRun(tree, "1"), "citation to a footnote-body id should resolve");
        Assert.False(HasRun(tree, "?"));
    }

    [Fact]
    public void UnknownRefIdStillRendersPlaceholder()
    {
        AreaTree tree = LayOut("<fo:block><fo:page-number-citation ref-id=\"missing\"/></fo:block>");
        Assert.True(HasRun(tree, "?"));
    }
}
