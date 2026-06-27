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

/// <summary>Parsing + property tests for the list formatting objects.</summary>
public sealed class FoListTests
{
    private static FoListBlock FirstList(string listMarkup)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" color="#000000">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="400pt" page-height="400pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  {listMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoListBlock>().Single();
    }

    [Fact]
    public void ParsesListStructure()
    {
        FoListBlock list = FirstList("""
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>1.</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>first</fo:block></fo:list-item-body>
              </fo:list-item>
              <fo:list-item>
                <fo:list-item-label><fo:block>2.</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>second</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        Assert.Equal(2, list.Items.Count());

        FoListItem first = list.Items.First();
        Assert.NotNull(first.Label);
        Assert.NotNull(first.Body);

        FoBlock labelBlock = Assert.Single(first.Label!.Blocks);
        Assert.Equal("1.", labelBlock.Children.OfType<FOText>().Single().Text);

        FoBlock bodyBlock = Assert.Single(first.Body!.Blocks);
        Assert.Equal("first", bodyBlock.Children.OfType<FOText>().Single().Text);
    }

    [Fact]
    public void ProvisionalDistances_DefaultTo24And6()
    {
        FoListBlock list = FirstList("""
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        Assert.Equal(24_000, list.ProvisionalDistanceBetweenStarts.Millipoints, 3);
        Assert.Equal(6_000, list.ProvisionalLabelSeparation.Millipoints, 3);
    }

    [Fact]
    public void ProvisionalDistances_Override()
    {
        FoListBlock list = FirstList("""
            <fo:list-block provisional-distance-between-starts="40pt" provisional-label-separation="10pt">
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        Assert.Equal(40_000, list.ProvisionalDistanceBetweenStarts.Millipoints, 3);
        Assert.Equal(10_000, list.ProvisionalLabelSeparation.Millipoints, 3);
    }

    [Fact]
    public void SpaceAndIndentAndBoxResolve()
    {
        FoListBlock list = FirstList("""
            <fo:list-block space-before="5pt" space-after="7pt" start-indent="12pt"
                background-color="#ff0000" border="1pt solid #000000">
              <fo:list-item space-before="3pt">
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        Assert.Equal(5_000, list.SpaceBefore.Millipoints, 3);
        Assert.Equal(7_000, list.SpaceAfter.Millipoints, 3);
        Assert.Equal(12_000, list.StartIndent.Millipoints, 3);
        Assert.True(list.Box.HasBackground);
        Assert.True(list.Box.HasBorder);

        FoListItem item = list.Items.Single();
        Assert.Equal(3_000, item.SpaceBefore.Millipoints, 3);
    }
}
