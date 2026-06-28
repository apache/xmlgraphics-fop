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
/// Layout tests for <c>widows</c>/<c>orphans</c> control across page breaks. The deterministic
/// <see cref="FakeFontMeasurer"/> at 10pt gives a 12000mpt line advance, so a 36pt-tall content area
/// (zero margins) holds exactly three lines, and a 12pt-wide area puts each two-letter word on its
/// own line.
/// </summary>
public sealed class WidowsOrphansLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string body) => new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString($"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="12pt" page-height="36pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
          </fo:page-sequence>
        </fo:root>
        """));

    private static int LineCount(PageArea page) => page.TextRuns.Count;

    [Fact]
    public void WidowsPullsLinesToAvoidASingleTrailingLine()
    {
        // A 4-line block on a 3-line page. Greedy would leave a single widow (3 + 1); widows=2 (default)
        // pulls a line back so the last page keeps two.
        AreaTree tree = LayOut("<fo:block>w0 w1 w2 w3</fo:block>");
        Assert.Equal(2, tree.Pages.Count);
        Assert.Equal(2, LineCount(tree.Pages[0]));
        Assert.Equal(2, LineCount(tree.Pages[1]));
    }

    [Fact]
    public void WidowsOneAllowsTheGreedySplit()
    {
        // widows=1 permits a single trailing line: the greedy 3 + 1 split.
        AreaTree tree = LayOut("<fo:block widows=\"1\" orphans=\"1\">w0 w1 w2 w3</fo:block>");
        Assert.Equal(2, tree.Pages.Count);
        Assert.Equal(3, LineCount(tree.Pages[0]));
        Assert.Equal(1, LineCount(tree.Pages[1]));
    }

    [Fact]
    public void OrphansPushesAWholeBlockWhenTooFewLinesFitAtTheBottom()
    {
        // Block A takes two lines, leaving room for only one more line on page 1. Block B has three
        // lines; orphans=2 (default) forbids leaving a single line at the bottom, so B moves entirely
        // to page 2.
        AreaTree tree = LayOut("""
            <fo:block>a0 a1</fo:block>
            <fo:block>b0 b1 b2</fo:block>
            """);
        Assert.Equal(2, tree.Pages.Count);
        Assert.DoesNotContain(tree.Pages[0].TextRuns, r => r.Text.StartsWith('b'));
        Assert.Equal(3, tree.Pages[1].TextRuns.Count(r => r.Text.StartsWith('b')));
    }

    [Fact]
    public void OrphansOneAllowsTheBlockToStartAtTheBottom()
    {
        // With orphans=1 the block may start with a single line at the page bottom.
        AreaTree tree = LayOut("""
            <fo:block>a0 a1</fo:block>
            <fo:block orphans="1" widows="1">b0 b1 b2</fo:block>
            """);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text.StartsWith('b'));
    }

    [Fact]
    public void ShortBlockThatFitsIsNotAffected()
    {
        // A block that fits entirely on the page is laid out unchanged regardless of widows/orphans.
        AreaTree tree = LayOut("<fo:block>w0 w1</fo:block>");
        Assert.Single(tree.Pages);
        Assert.Equal(2, LineCount(tree.Pages[0]));
    }
}
