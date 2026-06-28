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

using System;
using System.Collections.Generic;
using Fop.Fo;
using Xunit;

namespace Fop.Fo.Tests;

/// <summary>Tests for shorthand-property expansion via <see cref="PropertyList.GetRaw"/>.</summary>
public sealed class PropertyShorthandTests
{
    private static PropertyList List(params (string Name, string Value)[] props)
    {
        var dict = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var (name, value) in props)
        {
            dict[name] = value;
        }

        return new PropertyList(dict, null);
    }

    [Fact]
    public void MarginShorthandExpandsToAllEdges()
    {
        PropertyList p = List(("margin", "10pt"));
        Assert.Equal("10pt", p.GetRaw("margin-top"));
        Assert.Equal("10pt", p.GetRaw("margin-right"));
        Assert.Equal("10pt", p.GetRaw("margin-bottom"));
        Assert.Equal("10pt", p.GetRaw("margin-left"));
    }

    [Fact]
    public void MarginShorthandFourValuesMapTopRightBottomLeft()
    {
        PropertyList p = List(("margin", "1pt 2pt 3pt 4pt"));
        Assert.Equal("1pt", p.GetRaw("margin-top"));
        Assert.Equal("2pt", p.GetRaw("margin-right"));
        Assert.Equal("3pt", p.GetRaw("margin-bottom"));
        Assert.Equal("4pt", p.GetRaw("margin-left"));
    }

    [Fact]
    public void ExplicitLonghandWinsOverShorthand()
    {
        PropertyList p = List(("margin", "10pt"), ("margin-top", "99pt"));
        Assert.Equal("99pt", p.GetRaw("margin-top"));
        Assert.Equal("10pt", p.GetRaw("margin-left"));
    }

    [Fact]
    public void SizeNamedPageMapsToPageWidthHeight()
    {
        PropertyList p = List(("size", "a4"));
        Assert.Equal("595.28pt", p.GetRaw("page-width"));
        Assert.Equal("841.89pt", p.GetRaw("page-height"));
    }

    [Fact]
    public void SizeLandscapeSwapsDimensions()
    {
        PropertyList p = List(("size", "a4 landscape"));
        Assert.Equal("841.89pt", p.GetRaw("page-width"));
        Assert.Equal("595.28pt", p.GetRaw("page-height"));
    }

    [Fact]
    public void SizeTwoLengths()
    {
        PropertyList p = List(("size", "8in 11in"));
        Assert.Equal("8in", p.GetRaw("page-width"));
        Assert.Equal("11in", p.GetRaw("page-height"));
    }

    [Fact]
    public void FontShorthandDecomposes()
    {
        PropertyList p = List(("font", "italic bold 12pt/14pt Helvetica"));
        Assert.Equal("italic", p.GetRaw("font-style"));
        Assert.Equal("bold", p.GetRaw("font-weight"));
        Assert.Equal("12pt", p.GetRaw("font-size"));
        Assert.Equal("14pt", p.GetRaw("line-height"));
        Assert.Equal("Helvetica", p.GetRaw("font-family"));
    }

    [Fact]
    public void FontShorthandFeedsResolvedFontSize()
    {
        PropertyList p = List(("font", "10pt serif"));
        Assert.Equal(10_000, p.FontSizeMpt, 3);
        Assert.Equal("serif", p.FontFamily);
    }

    [Fact]
    public void FontSizeInheritsThroughShorthandOnAncestor()
    {
        PropertyList parent = List(("font", "16pt serif"));
        var child = new PropertyList(new Dictionary<string, string>(), parent);
        Assert.Equal(16_000, child.FontSizeMpt, 3);
    }

    [Fact]
    public void BackgroundShorthandYieldsColor()
    {
        PropertyList p = List(("background", "#ff0000 no-repeat"));
        Assert.Equal("#ff0000", p.GetRaw("background-color"));
        Assert.Equal(255, p.GetBox().BackgroundColor!.Red);
    }

    [Fact]
    public void PageBreakBeforeMapsToBreakBefore()
    {
        Assert.Equal(BreakKind.Page, List(("page-break-before", "always")).BreakBefore);
        Assert.Equal(BreakKind.EvenPage, List(("page-break-before", "left")).BreakBefore);
        Assert.Equal(BreakKind.OddPage, List(("page-break-after", "right")).BreakAfter);
    }

    [Fact]
    public void PageBreakInsideAvoidMapsToKeepTogether()
    {
        Assert.Equal(KeepStrength.Always, List(("page-break-inside", "avoid")).KeepTogetherWithinPage);
    }

    [Fact]
    public void WhiteSpaceNowrapSetsWrapOption()
    {
        PropertyList p = List(("white-space", "nowrap"));
        Assert.Equal("no-wrap", p.GetRaw("wrap-option"));
    }

    [Fact]
    public void UnrelatedPropertyIsUnaffected()
    {
        PropertyList p = List(("margin", "10pt"));
        Assert.Null(p.GetRaw("padding-top"));   // padding is resolved separately, not via margin
        Assert.Null(p.GetRaw("color"));
    }
}
