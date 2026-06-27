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
using Xunit;

namespace Fop.Fo.Tests;

/// <summary>
/// FO-tree tests for markers (fo:marker/fo:retrieve-marker) and the side regions
/// (fo:region-start/fo:region-end): parsing, the block exposing its markers without flowing them as
/// normal content, and the retrieve-position keyword mapping.
/// </summary>
public class MarkerAndSideRegionTests
{
    private const string Fo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
              <fo:region-body margin-left="20pt" margin-right="20pt"/>
              <fo:region-start extent="20pt"/>
              <fo:region-end extent="18pt"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:static-content flow-name="xsl-region-start">
              <fo:block><fo:retrieve-marker retrieve-class-name="chapter"/></fo:block>
            </fo:static-content>
            <fo:flow flow-name="xsl-region-body">
              <fo:block>Before<fo:marker marker-class-name="chapter">Chapter One</fo:marker>After</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void ParsesRegionStartAndEndExtents()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoSimplePageMaster master = root.LayoutMasterSet!.GetSimplePageMaster("p")!;

        Assert.NotNull(master.RegionStart);
        Assert.NotNull(master.RegionEnd);
        Assert.Equal(20_000, master.RegionStart!.Extent.Millipoints);
        Assert.Equal(18_000, master.RegionEnd!.Extent.Millipoints);
    }

    [Fact]
    public void SideRegionExtentDefaultsToZeroWhenAbsent()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                  <fo:region-start/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body"><fo:block>x</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        FoSimplePageMaster master = root.LayoutMasterSet!.GetSimplePageMaster("p")!;
        Assert.Equal(0, master.RegionStart!.Extent.Millipoints);
        Assert.Null(master.RegionEnd);
    }

    [Fact]
    public void BlockExposesItsMarkersByClassName()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoBlock block = root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().Single();

        FoMarker marker = Assert.Single(block.Markers);
        Assert.Equal("chapter", marker.MarkerClassName);

        FOText text = Assert.Single(marker.Children.OfType<FOText>());
        Assert.Equal("Chapter One", text.Text);
    }

    [Fact]
    public void MarkerIsNotPartOfNormalBlockTextContent()
    {
        // The block's own text content is "Before" + "After"; the marker text must not appear among
        // the block's direct FOText children.
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoBlock block = root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().Single();

        var directText = block.Children.OfType<FOText>().Select(t => t.Text).ToList();
        Assert.Contains("Before", directText);
        Assert.Contains("After", directText);
        Assert.DoesNotContain("Chapter One", directText);
    }

    [Fact]
    public void RetrieveMarkerParsesClassAndDefaultPosition()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoStaticContent start = root.PageSequences.First().GetStaticContent("xsl-region-start")!;
        FoBlock block = start.ChildObjects.OfType<FoBlock>().Single();

        FoRetrieveMarker retrieve = Assert.Single(block.ChildObjects.OfType<FoRetrieveMarker>());
        Assert.Equal("chapter", retrieve.RetrieveClassName);
        Assert.Equal(RetrievePosition.FirstStartingWithinPage, retrieve.RetrievePosition);
        Assert.Empty(retrieve.Children);
    }

    [Theory]
    [InlineData("first-starting-within-page", RetrievePosition.FirstStartingWithinPage)]
    [InlineData("first-including-carryover", RetrievePosition.FirstIncludingCarryover)]
    [InlineData("last-starting-within-page", RetrievePosition.LastStartingWithinPage)]
    [InlineData("last-ending-within-page", RetrievePosition.LastEndingWithinPage)]
    [InlineData("nonsense", RetrievePosition.FirstStartingWithinPage)]
    public void RetrievePositionKeywordMapping(string keyword, RetrievePosition expected)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:static-content flow-name="xsl-region-before">
                  <fo:block><fo:retrieve-marker retrieve-class-name="c" retrieve-position="{keyword}"/></fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body"><fo:block>x</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        FoStaticContent before = root.PageSequences.First().GetStaticContent("xsl-region-before")!;
        FoRetrieveMarker retrieve = before.ChildObjects.OfType<FoBlock>().Single()
            .ChildObjects.OfType<FoRetrieveMarker>().Single();
        Assert.Equal(expected, retrieve.RetrievePosition);
    }
}
