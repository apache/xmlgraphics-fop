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
/// Layout tests for the document outline built from <c>fo:bookmark-tree</c>: internal-destination
/// resolution to a 0-based page index (reusing the two-pass id map), nesting, the empty-outline case,
/// and the show/hide =&gt; Open mapping.
/// </summary>
public sealed class BookmarkOutlineLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>A page 200pt tall; default font 10pt so a short block is one 12pt line.</summary>
    private static string Document(string body, string bookmarks) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
          </fo:page-sequence>
          {bookmarks}
        </fo:root>
        """;

    [Fact]
    public void NoBookmarkTreeProducesEmptyOutline()
    {
        AreaTree tree = LayOut(Document("<fo:block>Body</fo:block>", string.Empty));
        Assert.Empty(tree.Outline);
    }

    [Fact]
    public void BookmarkToIdOnFirstPageResolvesToPageIndexZero()
    {
        string fo = Document(
            "<fo:block id=\"intro\">Intro</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro">
                <fo:bookmark-title>Introduction</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        OutlineEntry entry = Assert.Single(tree.Outline);
        Assert.Equal("Introduction", entry.Title);
        Assert.Equal(0, entry.TargetPageIndex);
        Assert.Null(entry.Uri);
    }

    [Fact]
    public void BookmarkToIdOnLaterPageResolvesToThatPageIndex()
    {
        // The target block forces a page break before it, so its id lands on page index 1.
        string fo = Document(
            "<fo:block>First page</fo:block>" +
            "<fo:block break-before=\"page\" id=\"chap2\">Second page</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="chap2">
                <fo:bookmark-title>Chapter 2</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        Assert.Equal(2, tree.Pages.Count);
        OutlineEntry entry = Assert.Single(tree.Outline);
        Assert.Equal(1, entry.TargetPageIndex);
    }

    [Fact]
    public void NestedBookmarksProduceNestedOutlineEntries()
    {
        string fo = Document(
            "<fo:block id=\"a\">A</fo:block>" +
            "<fo:block break-before=\"page\" id=\"b\">B</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="a">
                <fo:bookmark-title>Part A</fo:bookmark-title>
                <fo:bookmark internal-destination="b">
                  <fo:bookmark-title>Sub B</fo:bookmark-title>
                </fo:bookmark>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        OutlineEntry top = Assert.Single(tree.Outline);
        Assert.Equal("Part A", top.Title);
        Assert.Equal(0, top.TargetPageIndex);

        OutlineEntry child = Assert.Single(top.Children);
        Assert.Equal("Sub B", child.Title);
        Assert.Equal(1, child.TargetPageIndex);
        Assert.Empty(child.Children);
    }

    [Fact]
    public void StartingStateMapsToOpen()
    {
        string fo = Document(
            "<fo:block id=\"a\">A</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="a" starting-state="show">
                <fo:bookmark-title>Shown</fo:bookmark-title>
              </fo:bookmark>
              <fo:bookmark internal-destination="a" starting-state="hide">
                <fo:bookmark-title>Hidden</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        OutlineEntry[] entries = tree.Outline.ToArray();
        Assert.Equal(2, entries.Length);
        Assert.True(entries[0].Open);
        Assert.False(entries[1].Open);
    }

    [Fact]
    public void UnresolvedInternalDestinationFallsBackToFirstPage()
    {
        string fo = Document(
            "<fo:block id=\"a\">A</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="missing">
                <fo:bookmark-title>Dangling</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        OutlineEntry entry = Assert.Single(tree.Outline);
        // An unknown ref-id is best-effort: it targets the first page so the entry stays navigable.
        Assert.Equal(0, entry.TargetPageIndex);
    }

    [Fact]
    public void ExternalDestinationBookmarkCarriesUri()
    {
        string fo = Document(
            "<fo:block id=\"a\">A</fo:block>",
            """
            <fo:bookmark-tree>
              <fo:bookmark external-destination="https://example.com/">
                <fo:bookmark-title>Site</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        AreaTree tree = LayOut(fo);
        OutlineEntry entry = Assert.Single(tree.Outline);
        Assert.Equal("https://example.com/", entry.Uri);
        Assert.Equal(0, entry.TargetPageIndex);
    }
}
