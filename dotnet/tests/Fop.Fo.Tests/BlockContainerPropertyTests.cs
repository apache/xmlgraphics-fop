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

/// <summary>Property-parsing tests for <see cref="FoBlockContainer"/>.</summary>
public class BlockContainerPropertyTests
{
    private static FoBlockContainer FirstContainer(string markup)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  {markup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlockContainer>().First();
    }

    [Fact]
    public void ParsesAsBlockContainer()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container><fo:block>x</fo:block></fo:block-container>");
        Assert.Equal("block-container", c.LocalName);
        Assert.Single(c.BlockLevelChildren);
    }

    [Fact]
    public void AbsolutePosition_DefaultsToAuto()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container/>");
        Assert.Equal(AbsolutePosition.Auto, c.AbsolutePosition);
    }

    [Theory]
    [InlineData("absolute", AbsolutePosition.Absolute)]
    [InlineData("fixed", AbsolutePosition.Fixed)]
    [InlineData("auto", AbsolutePosition.Auto)]
    [InlineData("ABSOLUTE", AbsolutePosition.Absolute)]
    public void AbsolutePosition_Parses(string value, AbsolutePosition expected)
    {
        FoBlockContainer c = FirstContainer($"<fo:block-container absolute-position=\"{value}\"/>");
        Assert.Equal(expected, c.AbsolutePosition);
    }

    [Fact]
    public void TopLeftWidthHeight_Parse()
    {
        FoBlockContainer c = FirstContainer(
            "<fo:block-container top=\"10pt\" left=\"20pt\" width=\"100pt\" height=\"50pt\"/>");
        Assert.Equal(10_000, c.Top!.Value.Millipoints, 3);
        Assert.Equal(20_000, c.Left!.Value.Millipoints, 3);
        Assert.Equal(100_000, c.Width!.Value.Millipoints, 3);
        Assert.Equal(50_000, c.Height!.Value.Millipoints, 3);
    }

    [Fact]
    public void BottomRight_Parse()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container bottom=\"5pt\" right=\"8pt\"/>");
        Assert.Equal(5_000, c.Bottom!.Value.Millipoints, 3);
        Assert.Equal(8_000, c.Right!.Value.Millipoints, 3);
    }

    [Fact]
    public void Offsets_AutoAndUnsetAreNull()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container top=\"auto\"/>");
        Assert.Null(c.Top);
        Assert.Null(c.Left);
        Assert.Null(c.Width);
        Assert.Null(c.Height);
    }

    [Fact]
    public void Offsets_AcceptNegative()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container left=\"-10pt\" top=\"-5pt\"/>");
        Assert.Equal(-10_000, c.Left!.Value.Millipoints, 3);
        Assert.Equal(-5_000, c.Top!.Value.Millipoints, 3);
    }

    [Fact]
    public void ReferenceOrientation_DefaultsToZero()
    {
        FoBlockContainer c = FirstContainer("<fo:block-container/>");
        Assert.Equal(0, c.ReferenceOrientation);
    }

    [Theory]
    [InlineData("0", 0)]
    [InlineData("90", 90)]
    [InlineData("180", 180)]
    [InlineData("270", 270)]
    public void ReferenceOrientation_ParsesPositive(string value, int expected)
    {
        FoBlockContainer c = FirstContainer($"<fo:block-container reference-orientation=\"{value}\"/>");
        Assert.Equal(expected, c.ReferenceOrientation);
    }

    [Theory]
    [InlineData("-90", 270)]
    [InlineData("-180", 180)]
    [InlineData("-270", 90)]
    [InlineData("-360", 0)]
    public void ReferenceOrientation_NormalizesNegative(string value, int expected)
    {
        FoBlockContainer c = FirstContainer($"<fo:block-container reference-orientation=\"{value}\"/>");
        Assert.Equal(expected, c.ReferenceOrientation);
    }

    [Theory]
    [InlineData("45")]
    [InlineData("not-a-number")]
    [InlineData("")]
    public void ReferenceOrientation_InvalidFallsToZero(string value)
    {
        FoBlockContainer c = FirstContainer($"<fo:block-container reference-orientation=\"{value}\"/>");
        Assert.Equal(0, c.ReferenceOrientation);
    }

    [Fact]
    public void Box_ResolvesBorderAndBackground()
    {
        FoBlockContainer c = FirstContainer(
            "<fo:block-container background-color=\"#00ff00\" border=\"2pt solid #000000\"/>");
        Assert.True(c.Box.HasBackground);
        Assert.Equal(0, c.Box.BackgroundColor!.Red);
        Assert.Equal(255, c.Box.BackgroundColor.Green);
        Assert.True(c.Box.HasBorder);
    }
}
