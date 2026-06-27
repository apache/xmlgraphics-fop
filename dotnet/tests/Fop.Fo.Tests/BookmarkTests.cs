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

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>
/// Parsing + property tests for <c>fo:bookmark-tree</c>, <c>fo:bookmark</c> (internal/external
/// destination, starting-state, nesting) and <c>fo:bookmark-title</c> (text retention).
/// </summary>
public sealed class BookmarkTests
{
    private static FoRoot ParseWithBookmarks(string bookmarkTreeMarkup)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="400pt" page-height="400pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block id="intro">Intro</fo:block>
                </fo:flow>
              </fo:page-sequence>
              {bookmarkTreeMarkup}
            </fo:root>
            """;
        return FoTreeBuilder.ParseString(fo);
    }

    [Fact]
    public void RootExposesBookmarkTreeAfterPageSequences()
    {
        FoRoot root = ParseWithBookmarks(
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro">
                <fo:bookmark-title>Introduction</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        Assert.NotNull(root.BookmarkTree);
        Assert.Equal("bookmark-tree", root.BookmarkTree!.LocalName);
        FoBookmark bookmark = Assert.Single(root.BookmarkTree.Bookmarks);
        Assert.Equal("bookmark", bookmark.LocalName);
        Assert.Equal("intro", bookmark.InternalDestination);
    }

    [Fact]
    public void BookmarkTreeIsNullWhenAbsent()
    {
        FoRoot root = ParseWithBookmarks(string.Empty);
        Assert.Null(root.BookmarkTree);
    }

    [Fact]
    public void BookmarkTitleRetainsText()
    {
        FoRoot root = ParseWithBookmarks(
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro">
                <fo:bookmark-title>Chapter One</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        FoBookmark bookmark = root.BookmarkTree!.Bookmarks.Single();
        Assert.NotNull(bookmark.Title);
        Assert.Equal("bookmark-title", bookmark.Title!.LocalName);
        Assert.Equal("Chapter One", bookmark.Title.Text);
    }

    [Fact]
    public void ParsesExternalDestinationWithUrlWrapper()
    {
        FoRoot root = ParseWithBookmarks(
            """
            <fo:bookmark-tree>
              <fo:bookmark external-destination="url('https://example.com/')">
                <fo:bookmark-title>Site</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        FoBookmark bookmark = root.BookmarkTree!.Bookmarks.Single();
        Assert.Equal("https://example.com/", bookmark.ExternalDestination);
        Assert.Equal(string.Empty, bookmark.InternalDestination);
    }

    [Theory]
    [InlineData("show", StartingState.Show)]
    [InlineData("hide", StartingState.Hide)]
    public void ParsesStartingState(string raw, StartingState expected)
    {
        FoRoot root = ParseWithBookmarks(
            $"""
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro" starting-state="{raw}">
                <fo:bookmark-title>X</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        Assert.Equal(expected, root.BookmarkTree!.Bookmarks.Single().StartingState);
    }

    [Fact]
    public void StartingStateDefaultsToShow()
    {
        FoRoot root = ParseWithBookmarks(
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro">
                <fo:bookmark-title>X</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        Assert.Equal(StartingState.Show, root.BookmarkTree!.Bookmarks.Single().StartingState);
    }

    [Fact]
    public void ParsesNestedBookmarks()
    {
        FoRoot root = ParseWithBookmarks(
            """
            <fo:bookmark-tree>
              <fo:bookmark internal-destination="intro">
                <fo:bookmark-title>Part 1</fo:bookmark-title>
                <fo:bookmark internal-destination="intro">
                  <fo:bookmark-title>Section 1.1</fo:bookmark-title>
                </fo:bookmark>
                <fo:bookmark internal-destination="intro">
                  <fo:bookmark-title>Section 1.2</fo:bookmark-title>
                </fo:bookmark>
              </fo:bookmark>
            </fo:bookmark-tree>
            """);

        FoBookmark top = root.BookmarkTree!.Bookmarks.Single();
        Assert.Equal("Part 1", top.Title!.Text);

        FoBookmark[] children = top.Children.ToArray();
        Assert.Equal(2, children.Length);
        Assert.Equal("Section 1.1", children[0].Title!.Text);
        Assert.Equal("Section 1.2", children[1].Title!.Text);
        Assert.Empty(children[0].Children);
    }
}
