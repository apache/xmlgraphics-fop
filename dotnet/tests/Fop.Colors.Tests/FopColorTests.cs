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
using Xunit;

namespace Fop.Colors.Tests;

public class FopColorTests
{
    [Fact]
    public void FromRgb_StoresComponentsVerbatim()
    {
        FopColor c = FopColor.FromRgb(10, 20, 30);
        Assert.Equal(10, c.Red);
        Assert.Equal(20, c.Green);
        Assert.Equal(30, c.Blue);
        Assert.Equal(255, c.Alpha);
    }

    [Fact]
    public void FromArgb_KeepsAlpha()
    {
        FopColor c = FopColor.FromArgb(10, 20, 30, 40);
        Assert.Equal(40, c.Alpha);
    }

    [Fact]
    public void FromFloat_RoundsHalfUp()
    {
        // 0.5 -> (int)(0.5*255+0.5) = 128, not 127
        FopColor c = FopColor.FromFloat(0.5f, 0.5f, 0.5f);
        Assert.Equal(128, c.Red);
    }

    [Fact]
    public void FromFloat_RetainsPreciseComponents()
    {
        FopColor c = FopColor.FromFloat(0.0274f, 0.2196f, 0.3216f);
        float[] comps = c.GetSRgbComponents();
        Assert.Equal(0.0274f, comps[0]);
        Assert.Equal(0.2196f, comps[1]);
        Assert.Equal(0.3216f, comps[2]);
        Assert.Equal(1.0f, comps[3]);
    }

    [Fact]
    public void FromArgb_SRgbComponentsDerivedFromBytes()
    {
        FopColor c = FopColor.FromRgb(255, 0, 0);
        float[] comps = c.GetSRgbComponents();
        Assert.Equal(1.0f, comps[0]);
        Assert.Equal(0.0f, comps[1]);
    }

    [Fact]
    public void OutOfRange_Throws()
    {
        Assert.Throws<ArgumentOutOfRangeException>(() => FopColor.FromRgb(256, 0, 0));
        Assert.Throws<ArgumentOutOfRangeException>(() => FopColor.FromRgb(-1, 0, 0));
    }

    [Fact]
    public void Equality_BasedOnPackedValue()
    {
        Assert.Equal(FopColor.FromRgb(1, 2, 3), FopColor.FromRgb(1, 2, 3));
        Assert.NotEqual(FopColor.FromRgb(1, 2, 3), FopColor.FromRgb(1, 2, 4));
    }

    [Fact]
    public void Equality_FloatAndIntConstructorsConverge()
    {
        // 1.0f -> 255, matching FromRgb(255,...).
        Assert.Equal(FopColor.FromRgb(255, 0, 0), FopColor.FromFloat(1.0f, 0.0f, 0.0f));
    }

    [Fact]
    public void HashCode_ConsistentWithEquals()
    {
        Assert.Equal(FopColor.FromRgb(9, 8, 7).GetHashCode(), FopColor.FromRgb(9, 8, 7).GetHashCode());
    }

    [Fact]
    public void ToString_MatchesJavaAwtForm()
    {
        Assert.Equal("FopColor[r=255,g=128,b=0]", FopColor.FromRgb(255, 128, 0).ToString());
    }

    [Fact]
    public void Rgb_PacksArgb()
    {
        FopColor c = FopColor.FromArgb(0x12, 0x34, 0x56, 0x78);
        Assert.Equal(unchecked((int)0x78123456), c.Rgb);
    }

    [Fact]
    public void DifferentSubtype_NotEqualToPlainColor()
    {
        FopColor plain = FopColor.FromRgb(0, 0, 0);
        var oca = new OCAColor(OCAColorValue.Black);
        Assert.NotEqual(plain, (FopColor)oca);
    }
}
