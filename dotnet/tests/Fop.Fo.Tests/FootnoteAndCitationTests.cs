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

namespace Fop.Fo.Tests;

/// <summary>
/// Parsing + property tests for the page-number-citation and footnote formatting objects:
/// element creation, ref-id/id attributes (keyed by local name), and footnote structure
/// (inline anchor + footnote-body).
/// </summary>
public sealed class FootnoteAndCitationTests
{
    private static FoBlock FirstBlock(string blockMarkup)
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
                  {blockMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().First();
    }

    [Fact]
    public void ParsesPageNumberCitationWithRefId()
    {
        FoBlock block = FirstBlock("<fo:block>See <fo:page-number-citation ref-id=\"intro\"/></fo:block>");
        FoPageNumberCitation citation =
            block.ChildObjects.OfType<FoPageNumberCitation>().Single();

        Assert.Equal("page-number-citation", citation.LocalName);
        Assert.Equal("intro", citation.RefId);
    }

    [Fact]
    public void ParsesPageNumberCitationLastWithRefId()
    {
        FoBlock block =
            FirstBlock("<fo:block>End <fo:page-number-citation-last ref-id=\"chap\"/></fo:block>");
        FoPageNumberCitationLast citation =
            block.ChildObjects.OfType<FoPageNumberCitationLast>().Single();

        Assert.Equal("page-number-citation-last", citation.LocalName);
        Assert.Equal("chap", citation.RefId);
    }

    [Fact]
    public void CitationWithoutRefIdIsEmptyString()
    {
        FoBlock block = FirstBlock("<fo:block><fo:page-number-citation/></fo:block>");
        FoPageNumberCitation citation = block.ChildObjects.OfType<FoPageNumberCitation>().Single();

        Assert.Equal(string.Empty, citation.RefId);
    }

    [Fact]
    public void IdAttributeIsAvailableByLocalName()
    {
        // The id attribute (keyed by local name) is the target a citation's ref-id references.
        FoBlock block = FirstBlock("<fo:block id=\"intro\">Hello</fo:block>");
        Assert.Equal("intro", block.Properties.GetString("id", string.Empty));
    }

    [Fact]
    public void ParsesFootnoteWithAnchorAndBody()
    {
        FoBlock block = FirstBlock("""
            <fo:block>Body text<fo:footnote>
              <fo:inline>1</fo:inline>
              <fo:footnote-body><fo:block>The note.</fo:block></fo:footnote-body>
            </fo:footnote></fo:block>
            """);

        FoFootnote footnote = block.ChildObjects.OfType<FoFootnote>().Single();
        Assert.Equal("footnote", footnote.LocalName);

        // The body is the footnote-body child.
        Assert.NotNull(footnote.Body);
        Assert.Equal("footnote-body", footnote.Body!.LocalName);

        // The anchor children are everything except the footnote-body (here, the fo:inline).
        FObj anchor = Assert.Single(footnote.AnchorChildren);
        Assert.IsType<FoInline>(anchor);
    }

    [Fact]
    public void FootnoteBodyExposesBlockLevelChildren()
    {
        FoBlock block = FirstBlock("""
            <fo:block><fo:footnote>
              <fo:inline>*</fo:inline>
              <fo:footnote-body>
                <fo:block>One</fo:block>
                <fo:block>Two</fo:block>
              </fo:footnote-body>
            </fo:footnote></fo:block>
            """);

        FoFootnoteBody body = block.ChildObjects.OfType<FoFootnote>().Single().Body!;
        Assert.Equal(2, body.BlockLevelChildren.OfType<FoBlock>().Count());
    }
}
