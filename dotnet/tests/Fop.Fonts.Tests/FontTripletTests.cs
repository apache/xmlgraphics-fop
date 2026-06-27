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

using Fop.Fonts;
using Xunit;

namespace Fop.Fonts.Tests;

public class FontTripletTests
{
    [Fact]
    public void Constructor_StoresAllProperties()
    {
        FontTriplet t = new("Helvetica", "italic", 700, 3);
        Assert.Equal("Helvetica", t.Name);
        Assert.Equal("italic", t.Style);
        Assert.Equal(700, t.Weight);
        Assert.Equal(3, t.Priority);
    }

    [Fact]
    public void Constructor_DefaultsPriorityToZero()
    {
        FontTriplet t = new("Helvetica", "normal", 400);
        Assert.Equal(0, t.Priority);
    }

    [Fact]
    public void DefaultFontTriplet_HasExpectedValues()
    {
        FontTriplet d = FontTriplet.DefaultFontTriplet;
        Assert.Equal("any", d.Name);
        Assert.Equal("normal", d.Style);
        Assert.Equal(400, d.Weight);
        Assert.Equal(0, d.Priority);
        Assert.Equal("any,normal,400", d.ToString());
    }

    [Fact]
    public void ToString_IsNameCommaStyleCommaWeight()
    {
        FontTriplet t = new("Helvetica", "italic", 700, 5);
        // Priority is not part of the key.
        Assert.Equal("Helvetica,italic,700", t.ToString());
    }

    [Fact]
    public void Equals_IgnoresPriority()
    {
        FontTriplet a = new("Helvetica", "normal", 400, 0);
        FontTriplet b = new("Helvetica", "normal", 400, 9);
        Assert.Equal(a, b);
        Assert.True(a == b);
        Assert.False(a != b);
        Assert.Equal(a.GetHashCode(), b.GetHashCode());
    }

    [Fact]
    public void Equals_DistinguishesNameStyleWeight()
    {
        FontTriplet baseline = new("Helvetica", "normal", 400);
        Assert.NotEqual(baseline, new FontTriplet("Times", "normal", 400));
        Assert.NotEqual(baseline, new FontTriplet("Helvetica", "italic", 400));
        Assert.NotEqual(baseline, new FontTriplet("Helvetica", "normal", 700));
    }

    [Fact]
    public void Equals_HandlesNullAndOtherTypes()
    {
        FontTriplet t = new("Helvetica", "normal", 400);
        Assert.False(t.Equals(null));
        Assert.False(t.Equals("Helvetica,normal,400"));
        Assert.True(t.Equals(t));
    }

    [Fact]
    public void CompareTo_OrdersByKeyOrdinally()
    {
        FontTriplet a = new("Arial", "normal", 400);
        FontTriplet b = new("Helvetica", "normal", 400);
        Assert.True(a.CompareTo(b) < 0);
        Assert.True(b.CompareTo(a) > 0);
        Assert.Equal(0, a.CompareTo(new FontTriplet("Arial", "normal", 400, 7)));
    }

    [Fact]
    public void CompareTo_OrdersByWeightWithinSameNameAndStyle()
    {
        // Keys "Arial,normal,400" vs "Arial,normal,700" -> ordinal string comparison.
        FontTriplet light = new("Arial", "normal", 400);
        FontTriplet bold = new("Arial", "normal", 700);
        Assert.True(light.CompareTo(bold) < 0);
    }

    [Fact]
    public void Sorting_UsesKeyOrder()
    {
        List<FontTriplet> list =
        [
            new("Helvetica", "normal", 400),
            new("Arial", "italic", 700),
            new("Arial", "normal", 400),
        ];
        list.Sort();
        Assert.Equal(["Arial,italic,700", "Arial,normal,400", "Helvetica,normal,400"],
            list.ConvertAll(t => t.ToString()));
    }

    [Fact]
    public void RelationalOperators_Work()
    {
        FontTriplet a = new("Arial", "normal", 400);
        FontTriplet b = new("Helvetica", "normal", 400);
        Assert.True(a < b);
        Assert.True(a <= b);
        Assert.True(b > a);
        Assert.True(b >= a);
        Assert.True(a <= new FontTriplet("Arial", "normal", 400));
    }

    [Fact]
    public void Matcher_CanBeImplemented()
    {
        FontTriplet.IMatcher matcher = new NameMatcher("Arial");
        Assert.True(matcher.Matches(new FontTriplet("Arial", "normal", 400)));
        Assert.False(matcher.Matches(new FontTriplet("Times", "normal", 400)));
    }

    private sealed class NameMatcher(string name) : FontTriplet.IMatcher
    {
        public bool Matches(FontTriplet triplet) => triplet.Name == name;
    }
}
