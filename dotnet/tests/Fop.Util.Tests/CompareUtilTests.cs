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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class CompareUtilTests
{
    [Fact]
    public void Equal_TreatsTwoNullsAsEqual()
        => Assert.True(CompareUtil.Equal((object?)null, null));

    [Fact]
    public void Equal_NullAndNonNullAreNotEqual()
    {
        Assert.False(CompareUtil.Equal(null, "x"));
        Assert.False(CompareUtil.Equal("x", null));
    }

    [Fact]
    public void Equal_UsesValueEquality()
    {
        Assert.True(CompareUtil.Equal("abc", "ab" + "c"));
        Assert.False(CompareUtil.Equal("abc", "abd"));
    }

    [Fact]
    public void GetHashCode_NullIsZero()
        => Assert.Equal(0, CompareUtil.GetHashCode((object?)null));

    [Theory]
    [InlineData(1.5, 1.5, true)]
    [InlineData(1.5, 2.5, false)]
    [InlineData(0.0, -0.0, false)] // matches Java Double.equals: +0.0 != -0.0
    public void Equal_Double_FollowsJavaSemantics(double a, double b, bool expected)
        => Assert.Equal(expected, CompareUtil.Equal(a, b));

    [Fact]
    public void Equal_Double_AllNaNsAreEqual()
        => Assert.True(CompareUtil.Equal(double.NaN, 0.0 / 0.0));

    [Fact]
    public void GetHashCode_Double_NaNsShareHashCode()
        => Assert.Equal(CompareUtil.GetHashCode(double.NaN), CompareUtil.GetHashCode(0.0 / 0.0));

    [Fact]
    public void GetHashCode_Double_EqualValuesShareHashCode()
        => Assert.Equal(CompareUtil.GetHashCode(3.14), CompareUtil.GetHashCode(3.14));
}
