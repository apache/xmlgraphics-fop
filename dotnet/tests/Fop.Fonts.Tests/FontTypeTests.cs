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

public class FontTypeTests
{
    [Theory]
    [InlineData(FontType.Other, 0, "Other")]
    [InlineData(FontType.Type0, 1, "Type0")]
    [InlineData(FontType.Type1, 2, "Type1")]
    [InlineData(FontType.MMType1, 3, "MMType1")]
    [InlineData(FontType.Type3, 4, "Type3")]
    [InlineData(FontType.TrueType, 5, "TrueType")]
    [InlineData(FontType.Type1C, 6, "Type1C")]
    [InlineData(FontType.CIDType0, 7, "CIDFontType0")]
    public void NameAndValue_MatchJavaConstants(FontType type, int value, string name)
    {
        Assert.Equal(value, type.GetValue());
        Assert.Equal(name, type.GetName());
    }

    [Theory]
    [InlineData("Other", FontType.Other)]
    [InlineData("Type0", FontType.Type0)]
    [InlineData("Type1", FontType.Type1)]
    [InlineData("MMType1", FontType.MMType1)]
    [InlineData("Type3", FontType.Type3)]
    [InlineData("TrueType", FontType.TrueType)]
    public void ByName_RoundTrips(string name, FontType expected)
    {
        Assert.Equal(expected, FontTypeExtensions.ByName(name));
    }

    [Fact]
    public void ByName_IsCaseInsensitive()
    {
        Assert.Equal(FontType.TrueType, FontTypeExtensions.ByName("truetype"));
        Assert.Equal(FontType.MMType1, FontTypeExtensions.ByName("mmtype1"));
    }

    [Theory]
    [InlineData("Type1C")]
    [InlineData("CIDFontType0")]
    [InlineData("nonsense")]
    public void ByName_RejectsUnsupportedNames(string name)
    {
        // Matches the Java quirk: byName only knows Other..TrueType.
        Assert.Throws<ArgumentException>(() => FontTypeExtensions.ByName(name));
    }

    [Theory]
    [InlineData(0, FontType.Other)]
    [InlineData(1, FontType.Type0)]
    [InlineData(2, FontType.Type1)]
    [InlineData(3, FontType.MMType1)]
    [InlineData(4, FontType.Type3)]
    [InlineData(5, FontType.TrueType)]
    public void ByValue_RoundTrips(int value, FontType expected)
    {
        Assert.Equal(expected, FontTypeExtensions.ByValue(value));
    }

    [Theory]
    [InlineData(6)]
    [InlineData(7)]
    [InlineData(-1)]
    [InlineData(99)]
    public void ByValue_RejectsUnsupportedValues(int value)
    {
        // Matches the Java quirk: byValue only knows 0..5.
        Assert.Throws<ArgumentException>(() => FontTypeExtensions.ByValue(value));
    }
}
