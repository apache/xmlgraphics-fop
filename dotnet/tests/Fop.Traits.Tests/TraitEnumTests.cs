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

using Fop.Traits;
using Xunit;

namespace Fop.Traits.Tests;

/// <summary>
/// Coverage for the trait enumerations (Direction, WritingMode, BorderStyle, RuleStyle,
/// Visibility): names, EN_* values, and the round-trip behaviour of the <c>ValueOf</c> lookups.
/// </summary>
public class TraitEnumTests
{
    [Theory]
    [InlineData(Direction.Lr, "lr", 199)]
    [InlineData(Direction.Rl, "rl", 200)]
    [InlineData(Direction.Tb, "tb", 201)]
    [InlineData(Direction.Bt, "bt", 202)]
    public void DirectionNameAndValue(Direction direction, string name, int enumValue)
    {
        Assert.Equal(name, direction.GetName());
        Assert.Equal(enumValue, direction.GetEnumValue());
        Assert.Equal(direction, DirectionExtensions.ValueOf(name));
        Assert.Equal(direction, DirectionExtensions.ValueOf(name.ToUpperInvariant()));
        Assert.Equal(direction, DirectionExtensions.ValueOf(enumValue));
    }

    [Theory]
    [InlineData(Direction.Lr, true, false)]
    [InlineData(Direction.Rl, true, false)]
    [InlineData(Direction.Tb, false, true)]
    [InlineData(Direction.Bt, false, true)]
    public void DirectionOrientation(Direction direction, bool horizontal, bool vertical)
    {
        Assert.Equal(horizontal, direction.IsHorizontal());
        Assert.Equal(vertical, direction.IsVertical());
    }

    [Fact]
    public void DirectionValueOfIllegal()
    {
        Assert.Throws<ArgumentException>(() => DirectionExtensions.ValueOf("xx"));
        Assert.Throws<ArgumentException>(() => DirectionExtensions.ValueOf(-1));
    }

    [Theory]
    [InlineData(WritingMode.LrTb, "lr-tb", 79, true)]
    [InlineData(WritingMode.RlTb, "rl-tb", 121, true)]
    [InlineData(WritingMode.TbLr, "tb-lr", 203, false)]
    [InlineData(WritingMode.TbRl, "tb-rl", 140, false)]
    public void WritingModeNameValueAndOrientation(
        WritingMode mode, string name, int enumValue, bool horizontal)
    {
        Assert.Equal(name, mode.GetName());
        Assert.Equal(enumValue, mode.GetEnumValue());
        Assert.Equal(horizontal, mode.IsHorizontal());
        Assert.Equal(!horizontal, mode.IsVertical());
        Assert.Equal(mode, WritingModeExtensions.ValueOf(name));
        Assert.Equal(mode, WritingModeExtensions.ValueOf(enumValue));
    }

    [Fact]
    public void WritingModeValueOfIllegal()
    {
        Assert.Throws<ArgumentException>(() => WritingModeExtensions.ValueOf("xx"));
        Assert.Throws<ArgumentException>(() => WritingModeExtensions.ValueOf(-1));
    }

    [Theory]
    [InlineData(BorderStyle.None, "none", 95)]
    [InlineData(BorderStyle.Hidden, "hidden", 57)]
    [InlineData(BorderStyle.Dotted, "dotted", 36)]
    [InlineData(BorderStyle.Dashed, "dashed", 31)]
    [InlineData(BorderStyle.Solid, "solid", 133)]
    [InlineData(BorderStyle.Double, "double", 37)]
    [InlineData(BorderStyle.Groove, "groove", 55)]
    [InlineData(BorderStyle.Ridge, "ridge", 119)]
    [InlineData(BorderStyle.Inset, "inset", 67)]
    [InlineData(BorderStyle.Outset, "outset", 101)]
    public void BorderStyleNameValueAndLookup(BorderStyle style, string name, int enumValue)
    {
        Assert.Equal(name, style.GetName());
        Assert.Equal(enumValue, style.GetEnumValue());
        Assert.Equal("BorderStyle:" + name, style.ToDisplayString());
        Assert.Equal(style, BorderStyleExtensions.ValueOf(name));
        Assert.Equal(style, BorderStyleExtensions.ValueOf(enumValue));
    }

    [Fact]
    public void BorderStyleValueOfIllegal()
    {
        Assert.Throws<ArgumentException>(() => BorderStyleExtensions.ValueOf("xx"));
        Assert.Throws<ArgumentException>(() => BorderStyleExtensions.ValueOf(-1));
    }

    [Theory]
    [InlineData(RuleStyle.None, "none", 95)]
    [InlineData(RuleStyle.Dotted, "dotted", 36)]
    [InlineData(RuleStyle.Dashed, "dashed", 31)]
    [InlineData(RuleStyle.Solid, "solid", 133)]
    [InlineData(RuleStyle.Double, "double", 37)]
    [InlineData(RuleStyle.Groove, "groove", 55)]
    [InlineData(RuleStyle.Ridge, "ridge", 119)]
    public void RuleStyleNameValueAndLookup(RuleStyle style, string name, int enumValue)
    {
        Assert.Equal(name, style.GetName());
        Assert.Equal(enumValue, style.GetEnumValue());
        Assert.Equal("RuleStyle:" + name, style.ToDisplayString());
        Assert.Equal(style, RuleStyleExtensions.ValueOf(name));
        Assert.Equal(style, RuleStyleExtensions.ValueOf(enumValue));
    }

    [Fact]
    public void RuleStyleValueOfIllegal()
    {
        Assert.Throws<ArgumentException>(() => RuleStyleExtensions.ValueOf("xx"));
        Assert.Throws<ArgumentException>(() => RuleStyleExtensions.ValueOf(-1));
    }

    [Theory]
    [InlineData(Visibility.Visible, "visible", 159)]
    [InlineData(Visibility.Hidden, "hidden", 57)]
    [InlineData(Visibility.Collapse, "collapse", 26)]
    public void VisibilityNameValueAndLookup(Visibility visibility, string name, int enumValue)
    {
        Assert.Equal(name, visibility.GetName());
        Assert.Equal(enumValue, visibility.GetEnumValue());
        Assert.Equal(visibility, VisibilityExtensions.ValueOf(name));
        Assert.Equal(visibility, VisibilityExtensions.ValueOf(name.ToUpperInvariant()));
    }

    [Fact]
    public void VisibilityValueOfIllegal()
    {
        Assert.Throws<ArgumentException>(() => VisibilityExtensions.ValueOf("xx"));
    }
}
