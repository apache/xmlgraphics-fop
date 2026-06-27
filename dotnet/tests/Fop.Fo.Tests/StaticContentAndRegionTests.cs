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
/// FO-tree tests for running headers/footers: region-before/after extents, static-content flow-name
/// and retained blocks, page-sequence initial-page-number, and the page-number FO.
/// </summary>
public class StaticContentAndRegionTests
{
    private const string Fo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
              <fo:region-body margin-top="20pt" margin-bottom="20pt"/>
              <fo:region-before extent="15pt"/>
              <fo:region-after extent="12pt"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p" initial-page-number="3">
            <fo:static-content flow-name="xsl-region-before">
              <fo:block>Header text</fo:block>
            </fo:static-content>
            <fo:static-content flow-name="xsl-region-after">
              <fo:block>Page <fo:page-number/></fo:block>
            </fo:static-content>
            <fo:flow flow-name="xsl-region-body">
              <fo:block>Body</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void ParsesRegionBeforeAndAfterExtents()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoSimplePageMaster master = root.LayoutMasterSet!.GetSimplePageMaster("p")!;

        Assert.NotNull(master.RegionBefore);
        Assert.NotNull(master.RegionAfter);
        Assert.Equal(15_000, master.RegionBefore!.Extent.Millipoints);
        Assert.Equal(12_000, master.RegionAfter!.Extent.Millipoints);
    }

    [Fact]
    public void RegionExtentDefaultsToZeroWhenAbsent()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                  <fo:region-before/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body"><fo:block>x</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        FoSimplePageMaster master = root.LayoutMasterSet!.GetSimplePageMaster("p")!;
        Assert.Equal(0, master.RegionBefore!.Extent.Millipoints);
    }

    [Fact]
    public void StaticContentExposesFlowNameAndRetainsBlocks()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoPageSequence seq = root.PageSequences.First();

        FoStaticContent? before = seq.GetStaticContent("xsl-region-before");
        Assert.NotNull(before);
        Assert.Equal("xsl-region-before", before!.FlowName);

        var block = Assert.Single(before.ChildObjects.OfType<FoBlock>());
        FOText text = Assert.Single(block.Children.OfType<FOText>());
        Assert.Equal("Header text", text.Text);

        Assert.Equal(2, seq.StaticContents.Count());
        Assert.NotNull(seq.GetStaticContent("xsl-region-after"));
        Assert.Null(seq.GetStaticContent("xsl-region-start"));
    }

    [Fact]
    public void PageSequenceExposesInitialPageNumber()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        Assert.Equal(3, root.PageSequences.First().InitialPageNumber);
    }

    [Fact]
    public void InitialPageNumberIsNullWhenUnsetOrKeyword()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p" initial-page-number="auto">
                <fo:flow flow-name="xsl-region-body"><fo:block>x</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        Assert.Null(root.PageSequences.First().InitialPageNumber);
    }

    [Fact]
    public void PageNumberIsAnEmptyInlineFo()
    {
        FoRoot root = FoTreeBuilder.ParseString(Fo);
        FoStaticContent after = root.PageSequences.First().GetStaticContent("xsl-region-after")!;
        FoBlock block = after.ChildObjects.OfType<FoBlock>().Single();

        FoPageNumber pageNumber = Assert.Single(block.ChildObjects.OfType<FoPageNumber>());
        Assert.Equal("page-number", pageNumber.LocalName);
        Assert.Empty(pageNumber.Children);
    }
}
