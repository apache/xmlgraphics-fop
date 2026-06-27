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

/// <summary>Parsing tests for the <c>break-before</c>/<c>break-after</c>/<c>keep-together</c> properties.</summary>
public sealed class BreakAndKeepPropertyTests
{
    private static FoBlock FirstBlock(string blockMarkup)
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
                  {blockMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().First();
    }

    // ----- break-before / break-after ----------------------------------------------------

    [Theory]
    [InlineData("auto", BreakKind.Auto)]
    [InlineData("page", BreakKind.Page)]
    [InlineData("column", BreakKind.Page)]
    [InlineData("even-page", BreakKind.EvenPage)]
    [InlineData("odd-page", BreakKind.OddPage)]
    [InlineData("EVEN-PAGE", BreakKind.EvenPage)]
    public void ParsesBreakBefore(string value, BreakKind expected)
    {
        FoBlock block = FirstBlock($"<fo:block break-before=\"{value}\">x</fo:block>");
        Assert.Equal(expected, block.BreakBefore);
    }

    [Theory]
    [InlineData("page", BreakKind.Page)]
    [InlineData("odd-page", BreakKind.OddPage)]
    public void ParsesBreakAfter(string value, BreakKind expected)
    {
        FoBlock block = FirstBlock($"<fo:block break-after=\"{value}\">x</fo:block>");
        Assert.Equal(expected, block.BreakAfter);
    }

    [Fact]
    public void BreakDefaultsToAuto_WhenUnset()
    {
        FoBlock block = FirstBlock("<fo:block>x</fo:block>");
        Assert.Equal(BreakKind.Auto, block.BreakBefore);
        Assert.Equal(BreakKind.Auto, block.BreakAfter);
    }

    [Fact]
    public void UnrecognisedBreakValue_FallsBackToAuto()
    {
        FoBlock block = FirstBlock("<fo:block break-before=\"banana\">x</fo:block>");
        Assert.Equal(BreakKind.Auto, block.BreakBefore);
    }

    // ----- keep-together -----------------------------------------------------------------

    [Fact]
    public void KeepTogetherAlways_ParsesAsAlways()
    {
        FoBlock block = FirstBlock("<fo:block keep-together=\"always\">x</fo:block>");
        Assert.Equal(KeepStrength.Always, block.KeepTogetherWithinPage);
    }

    [Fact]
    public void KeepTogetherWithinPage_TakesPrecedenceOverShorthand()
    {
        FoBlock block = FirstBlock(
            "<fo:block keep-together=\"auto\" keep-together.within-page=\"always\">x</fo:block>");
        Assert.Equal(KeepStrength.Always, block.KeepTogetherWithinPage);
    }

    [Fact]
    public void KeepTogetherInteger_TreatedAsAlways()
    {
        FoBlock block = FirstBlock("<fo:block keep-together.within-page=\"5\">x</fo:block>");
        Assert.Equal(KeepStrength.Always, block.KeepTogetherWithinPage);
    }

    [Fact]
    public void KeepTogether_DefaultsToAuto()
    {
        FoBlock block = FirstBlock("<fo:block>x</fo:block>");
        Assert.Equal(KeepStrength.Auto, block.KeepTogetherWithinPage);
    }

    [Fact]
    public void KeepAndBreak_AreNotInherited()
    {
        // A child block must not inherit the parent's break/keep.
        FoBlock outer = FirstBlock(
            "<fo:block break-before=\"page\" keep-together=\"always\"><fo:block>c</fo:block></fo:block>");
        FoBlock inner = outer.ChildObjects.OfType<FoBlock>().Single();
        Assert.Equal(BreakKind.Auto, inner.BreakBefore);
        Assert.Equal(KeepStrength.Auto, inner.KeepTogetherWithinPage);
    }
}
