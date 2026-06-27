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

using Fop.Colors;
using Fop.Fo;

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>Integration tests for routing property resolution through the expression evaluator.</summary>
public sealed class PropertyListExprTests
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

    private static PropertyList List(PropertyList parent, params (string Name, string Value)[] props)
    {
        var dict = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var (name, value) in props)
        {
            dict[name] = value;
        }

        return new PropertyList(dict, parent);
    }

    [Fact]
    public void GetLengthEvaluatesArithmetic()
    {
        PropertyList list = List(("margin-top", "2pt + 3pt"));
        Assert.Equal(5_000, list.GetLength("margin-top", FoLength.Zero).Millipoints, 3);
    }

    [Fact]
    public void GetLengthEvaluatesFunction()
    {
        PropertyList list = List(("margin-top", "max(2pt, 7pt)"));
        Assert.Equal(7_000, list.GetLength("margin-top", FoLength.Zero).Millipoints, 3);
    }

    [Fact]
    public void GetLengthEvaluatesEmExpression()
    {
        // font-size 20pt, so 2 * 1em == 40pt.
        PropertyList list = List(("font-size", "20pt"), ("margin-top", "2 * 1em"));
        Assert.Equal(40_000, list.GetLength("margin-top", FoLength.Zero).Millipoints, 1);
    }

    [Fact]
    public void PlainLengthUsesFastPath()
    {
        PropertyList list = List(("margin-top", "12pt"));
        Assert.Equal(12_000, list.GetLength("margin-top", FoLength.Zero).Millipoints, 3);
    }

    [Fact]
    public void PlainPercentIsNotTreatedAsExpression()
    {
        PropertyList list = List(("width", "80%"));
        Assert.Equal(80_000, list.GetLength("width", FoLength.Zero, percentBaseMpt: 100_000).Millipoints, 1);
    }

    [Fact]
    public void GetColorEvaluatesRgbFunction()
    {
        PropertyList list = List(("color", "rgb(255, 0, 0)"));
        Assert.Equal(FopColor.FromRgb(255, 0, 0), list.GetColor());
    }

    [Fact]
    public void GetColorPlainHexUsesFastPath()
    {
        PropertyList list = List(("color", "#abc"));
        Assert.Equal(ColorUtil.ParseColorString(null, "#abc"), list.GetColor());
    }

    [Fact]
    public void FontSizeEvaluatesExpression()
    {
        PropertyList parent = List(("font-size", "10pt"));
        PropertyList child = List(parent, ("font-size", "2 * 6pt"));
        Assert.Equal(12_000, child.FontSizeMpt, 1);
    }

    [Fact]
    public void FromParentResolvesAgainstParentChain()
    {
        PropertyList parent = List(("font-size", "20pt"));
        PropertyList child = List(parent, ("margin-top", "from-parent(\"font-size\")"));
        Assert.Equal(20_000, child.GetLength("margin-top", FoLength.Zero).Millipoints, 1);
    }

    [Fact]
    public void InheritedPropertyValueResolvesAgainstParentChain()
    {
        PropertyList parent = List(("font-size", "16pt"));
        PropertyList child = List(parent, ("margin-top", "inherited-property-value(\"font-size\") + 4pt"));
        Assert.Equal(20_000, child.GetLength("margin-top", FoLength.Zero).Millipoints, 1);
    }

    [Fact]
    public void FromNearestSpecifiedWalksAncestors()
    {
        PropertyList grand = List(("margin-left", "30pt"));
        PropertyList parent = List(grand); // does not specify margin-left
        PropertyList child = List(parent, ("margin-top", "from-nearest-specified-value(\"margin-left\")"));
        Assert.Equal(30_000, child.GetLength("margin-top", FoLength.Zero).Millipoints, 1);
    }

    [Fact]
    public void MalformedExpressionFallsBackToDefault()
    {
        PropertyList list = List(("margin-top", "2pt + "));
        FoLength fallback = FoLength.FromPoints(99);
        Assert.Equal(99_000, list.GetLength("margin-top", fallback).Millipoints, 3);
    }

    [Fact]
    public void MalformedColorExpressionFallsBackToBlack()
    {
        PropertyList list = List(("color", "rgb(255, 0)"));
        Assert.Equal(FopColor.FromRgb(0, 0, 0), list.GetColor());
    }
}
