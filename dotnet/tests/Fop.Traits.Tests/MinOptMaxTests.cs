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
/// Tests the <see cref="MinOptMax"/> type. Faithful port of
/// <c>org.apache.fop.traits.MinOptMaxTestCase</c> (same scenarios and assertions).
/// </summary>
public class MinOptMaxTests
{
    /// <summary>Tests that the constant <see cref="MinOptMax.Zero"/> is really zero.</summary>
    [Fact]
    public void TestZero()
    {
        Assert.Equal(MinOptMax.GetInstance(0), MinOptMax.Zero);

        // Java asserted reference identity (getInstance(0,0,0) == ZERO). For a value type the
        // equivalent observable property is value equality.
        Assert.True(MinOptMax.GetInstance(0, 0, 0) == MinOptMax.Zero);
    }

    [Fact]
    public void TestNewStiffMinOptMax()
    {
        MinOptMax value = MinOptMax.GetInstance(1);
        Assert.True(value.IsStiff());
        Assert.Equal(1, value.Min);
        Assert.Equal(1, value.Opt);
        Assert.Equal(1, value.Max);
    }

    [Fact]
    public void TestNewMinOptMax()
    {
        MinOptMax value = MinOptMax.GetInstance(1, 2, 3);
        Assert.True(value.IsElastic());
        Assert.Equal(1, value.Min);
        Assert.Equal(2, value.Opt);
        Assert.Equal(3, value.Max);
    }

    /// <summary>It is possible to create stiff instances with the normal factory method.</summary>
    [Fact]
    public void TestNewMinOptMaxStiff()
    {
        MinOptMax value = MinOptMax.GetInstance(1, 1, 1);
        Assert.True(value.IsStiff());
        Assert.Equal(1, value.Min);
        Assert.Equal(1, value.Opt);
        Assert.Equal(1, value.Max);
    }

    [Fact]
    public void TestNewMinOptMaxMinGreaterOpt()
    {
        ArgumentException e = Assert.Throws<ArgumentException>(() => MinOptMax.GetInstance(1, 0, 2));
        Assert.Equal("min (1) > opt (0)", e.Message);
    }

    [Fact]
    public void TestNewMinOptMaxMaxSmallerOpt()
    {
        ArgumentException e = Assert.Throws<ArgumentException>(() => MinOptMax.GetInstance(0, 1, 0));
        Assert.Equal("max (0) < opt (1)", e.Message);
    }

    [Fact]
    public void TestShrinkability()
    {
        Assert.Equal(0, MinOptMax.GetInstance(1).Shrink);
        Assert.Equal(1, MinOptMax.GetInstance(1, 2, 2).Shrink);
        Assert.Equal(2, MinOptMax.GetInstance(1, 3, 3).Shrink);
    }

    [Fact]
    public void TestStretchability()
    {
        Assert.Equal(0, MinOptMax.GetInstance(1).Stretch);
        Assert.Equal(1, MinOptMax.GetInstance(1, 1, 2).Stretch);
        Assert.Equal(2, MinOptMax.GetInstance(1, 1, 3).Stretch);
    }

    [Fact]
    public void TestPlus()
    {
        Assert.Equal(MinOptMax.Zero, MinOptMax.Zero.Plus(MinOptMax.Zero));
        Assert.Equal(
            MinOptMax.GetInstance(1, 2, 3),
            MinOptMax.Zero.Plus(MinOptMax.GetInstance(1, 2, 3)));
        Assert.Equal(
            MinOptMax.GetInstance(2, 4, 6),
            MinOptMax.GetInstance(1, 2, 3).Plus(MinOptMax.GetInstance(1, 2, 3)));
        Assert.Equal(MinOptMax.GetInstance(4, 5, 6), MinOptMax.GetInstance(1, 2, 3).Plus(3));
    }

    [Fact]
    public void TestMinus()
    {
        Assert.Equal(MinOptMax.Zero, MinOptMax.Zero.Minus(MinOptMax.Zero));
        Assert.Equal(
            MinOptMax.GetInstance(1, 2, 3),
            MinOptMax.GetInstance(1, 2, 3).Plus(MinOptMax.Zero));
        Assert.Equal(
            MinOptMax.GetInstance(1, 2, 3),
            MinOptMax.GetInstance(2, 4, 6).Minus(MinOptMax.GetInstance(1, 2, 3)));
        Assert.Equal(MinOptMax.GetInstance(1, 2, 3), MinOptMax.GetInstance(5, 6, 7).Minus(4));
    }

    [Fact]
    public void TestMinusFail1()
    {
        Assert.Throws<ArithmeticException>(
            () => MinOptMax.Zero.Minus(MinOptMax.GetInstance(1, 2, 3)));
    }

    [Fact]
    public void TestMinusFail2()
    {
        Assert.Throws<ArithmeticException>(
            () => MinOptMax.GetInstance(1, 2, 3).Minus(MinOptMax.GetInstance(1, 3, 3)));
    }

    [Fact]
    public void TestMinusFail3()
    {
        Assert.Throws<ArithmeticException>(
            () => MinOptMax.Zero.Minus(MinOptMax.GetInstance(1, 1, 2)));
    }

    [Fact]
    public void TestMinusFail4()
    {
        Assert.Throws<ArithmeticException>(
            () => MinOptMax.GetInstance(1, 2, 3).Minus(MinOptMax.GetInstance(1, 1, 3)));
    }

    [Fact]
    public void TestMult()
    {
        Assert.Equal(MinOptMax.Zero, MinOptMax.Zero.Mult(0));
        Assert.Equal(MinOptMax.GetInstance(1, 2, 3), MinOptMax.GetInstance(1, 2, 3).Mult(1));
        Assert.Equal(MinOptMax.GetInstance(2, 4, 6), MinOptMax.GetInstance(1, 2, 3).Mult(2));
    }

    [Fact]
    public void TestMultFail()
    {
        ArgumentException e = Assert.Throws<ArgumentException>(
            () => MinOptMax.GetInstance(1, 2, 3).Mult(-1));
        Assert.Equal("factor < 0; was: -1", e.Message);
    }

    [Fact]
    public void TestNonZero()
    {
        Assert.False(MinOptMax.Zero.IsNonZero());
        Assert.True(MinOptMax.GetInstance(1).IsNonZero());
        Assert.True(MinOptMax.GetInstance(1, 2, 3).IsNonZero());
    }

    [Fact]
    public void TestExtendMinimum()
    {
        Assert.Equal(MinOptMax.GetInstance(1, 1, 1), MinOptMax.Zero.ExtendMinimum(1));
        Assert.Equal(
            MinOptMax.GetInstance(1, 2, 3),
            MinOptMax.GetInstance(1, 2, 3).ExtendMinimum(1));
        Assert.Equal(
            MinOptMax.GetInstance(2, 2, 3),
            MinOptMax.GetInstance(1, 2, 3).ExtendMinimum(2));
        Assert.Equal(
            MinOptMax.GetInstance(3, 3, 3),
            MinOptMax.GetInstance(1, 2, 3).ExtendMinimum(3));
        Assert.Equal(
            MinOptMax.GetInstance(4, 4, 4),
            MinOptMax.GetInstance(1, 2, 3).ExtendMinimum(4));
    }

    [Fact]
    public void TestEquals()
    {
        MinOptMax number = MinOptMax.GetInstance(1, 3, 5);
        Assert.Equal(number, number);
        Assert.Equal(number, MinOptMax.GetInstance(1, 3, 5));
        Assert.False(number.Equals(MinOptMax.GetInstance(2, 3, 5)));
        Assert.False(number.Equals(MinOptMax.GetInstance(1, 4, 5)));
        Assert.False(number.Equals(MinOptMax.GetInstance(1, 3, 4)));

        // Java: assertFalse(number.equals(null)); equals(1) -> different type, false.
        Assert.False(number.Equals(null));
        Assert.False(number.Equals(1));
    }

    [Fact]
    public void TestHashCode()
    {
        MinOptMax number = MinOptMax.GetInstance(1, 2, 3);
        Assert.Equal(number.GetHashCode(), number.GetHashCode());
        Assert.Equal(number.GetHashCode(), MinOptMax.GetInstance(1, 2, 3).GetHashCode());
    }

    /// <summary>Additional coverage: the operator overloads mirror the named methods.</summary>
    [Fact]
    public void TestOperators()
    {
        Assert.Equal(
            MinOptMax.GetInstance(2, 4, 6),
            MinOptMax.GetInstance(1, 2, 3) + MinOptMax.GetInstance(1, 2, 3));
        Assert.Equal(MinOptMax.GetInstance(4, 5, 6), MinOptMax.GetInstance(1, 2, 3) + 3);
        Assert.Equal(
            MinOptMax.GetInstance(1, 2, 3),
            MinOptMax.GetInstance(2, 4, 6) - MinOptMax.GetInstance(1, 2, 3));
        Assert.Equal(MinOptMax.GetInstance(1, 2, 3), MinOptMax.GetInstance(5, 6, 7) - 4);
        Assert.Equal(MinOptMax.GetInstance(2, 4, 6), MinOptMax.GetInstance(1, 2, 3) * 2);
    }

    /// <summary>Additional coverage for the backwards-compatibility min/max mutators and ToString.</summary>
    [Fact]
    public void TestBackwardsCompatibilityMutatorsAndToString()
    {
        Assert.Equal(MinOptMax.GetInstance(2, 3, 4), MinOptMax.GetInstance(1, 3, 4).PlusMin(1));
        Assert.Equal(MinOptMax.GetInstance(1, 3, 4), MinOptMax.GetInstance(2, 3, 4).MinusMin(1));
        Assert.Equal(MinOptMax.GetInstance(1, 3, 5), MinOptMax.GetInstance(1, 3, 4).PlusMax(1));
        Assert.Equal(MinOptMax.GetInstance(1, 3, 4), MinOptMax.GetInstance(1, 3, 5).MinusMax(1));

        Assert.Equal("MinOptMax[min = 1, opt = 2, max = 3]", MinOptMax.GetInstance(1, 2, 3).ToString());
    }
}
