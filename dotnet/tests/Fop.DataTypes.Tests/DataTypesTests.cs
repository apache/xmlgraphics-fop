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

using Fop.DataTypes;
using Xunit;

namespace Fop.DataTypes.Tests;

/// <summary>Coverage for the simple datatype value classes.</summary>
public class DataTypesTests
{
    [Fact]
    public void SimplePercentBaseContext_ReturnsOwnValueForMatchingBase()
    {
        var context = new SimplePercentBaseContext(null, LengthBase.ContainingBlockWidth, 5000);

        Assert.Equal(5000, context.GetBaseLength(LengthBase.ContainingBlockWidth, null));
    }

    [Fact]
    public void SimplePercentBaseContext_ReturnsMinusOneForUnknownBaseWithoutParent()
    {
        var context = new SimplePercentBaseContext(null, LengthBase.ContainingBlockWidth, 5000);

        Assert.Equal(-1, context.GetBaseLength(LengthBase.ContainingBlockHeight, null));
    }

    [Fact]
    public void SimplePercentBaseContext_DelegatesToParentForOtherBases()
    {
        var parent = new SimplePercentBaseContext(null, LengthBase.ContainingBlockHeight, 7000);
        var child = new SimplePercentBaseContext(parent, LengthBase.ContainingBlockWidth, 5000);

        Assert.Equal(5000, child.GetBaseLength(LengthBase.ContainingBlockWidth, null));
        Assert.Equal(7000, child.GetBaseLength(LengthBase.ContainingBlockHeight, null));
        Assert.Equal(-1, child.GetBaseLength(LengthBase.FontSize, null));
    }

    [Fact]
    public void ValidationPercentBaseContext_AlwaysReturnsNonZero()
    {
        var context = ValidationPercentBaseContext.GetPseudoContext();

        Assert.Equal(100000, context.GetBaseLength(LengthBase.ContainingBlockWidth, null));
        Assert.Equal(100000, context.GetBaseLength(LengthBase.FontSize, "anything"));
    }

    [Fact]
    public void ValidationPercentBaseContext_ReturnsSharedInstance()
    {
        Assert.Same(
            ValidationPercentBaseContext.GetPseudoContext(),
            ValidationPercentBaseContext.GetPseudoContext());
    }

    [Fact]
    public void LengthBase_DimensionAndBaseValueAreConstant()
    {
        var lengthBase = new LengthBase(null, LengthBase.ContainingBlockWidth);

        Assert.Equal(1, lengthBase.Dimension);
        Assert.Equal(1.0, lengthBase.BaseValue);
    }

    [Fact]
    public void LengthBase_GetBaseLengthWithoutContextReturnsZero()
    {
        var lengthBase = new LengthBase(null, LengthBase.ContainingBlockWidth);

        Assert.Equal(0, lengthBase.GetBaseLength(null));
    }

    [Fact]
    public void LengthBase_GetBaseLengthDelegatesToContextForLayoutBases()
    {
        var lengthBase = new LengthBase("fo", LengthBase.ContainingBlockWidth);
        var context = new SimplePercentBaseContext(null, LengthBase.ContainingBlockWidth, 4200);

        Assert.Equal(4200, lengthBase.GetBaseLength(context));
    }

    [Fact]
    public void LengthBase_EqualityIsValueBased()
    {
        var a = new LengthBase("fo", LengthBase.FontSize);
        var b = new LengthBase("fo", LengthBase.FontSize);
        var c = new LengthBase("fo", LengthBase.ContainingBlockWidth);

        Assert.Equal(a, b);
        Assert.Equal(a.GetHashCode(), b.GetHashCode());
        Assert.NotEqual(a, c);
    }

    [Fact]
    public void FODimension_ExposesIpdAndBpd()
    {
        var dimension = new FODimension(100, 200);

        Assert.Equal(100, dimension.Ipd);
        Assert.Equal(200, dimension.Bpd);
    }

    [Fact]
    public void FODimension_ToStringContainsValues()
    {
        var dimension = new FODimension(100, 200);

        Assert.Contains("ipd=100", dimension.ToString());
        Assert.Contains("bpd=200", dimension.ToString());
    }

    [Fact]
    public void KeepValue_ExposesTypeAndValue()
    {
        var keep = new KeepValue(KeepValue.KeepWithValue, 42);

        Assert.Equal(KeepValue.KeepWithValue, keep.Type);
        Assert.Equal(42, keep.Value);
    }

    [Fact]
    public void KeepValue_ToStringReturnsType()
    {
        var keep = new KeepValue(KeepValue.KeepWithAlways, 0);

        Assert.Equal("KEEP_WITH_ALWAYS", keep.ToString());
    }
}
