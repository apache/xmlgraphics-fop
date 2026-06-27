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

public class OcaColorTests
{
    [Theory]
    [InlineData(OCAColorValue.Blue, 0x1)]
    [InlineData(OCAColorValue.Red, 0x2)]
    [InlineData(OCAColorValue.Black, 0x8)]
    [InlineData(OCAColorValue.Brown, 0x10)]
    [InlineData(OCAColorValue.DeviceDefault, 0xFF07)]
    [InlineData(OCAColorValue.MediumColor, 0xFF08)]
    public void Oca_ReturnsTwoByteCode(OCAColorValue value, int expectedCode)
    {
        var c = new OCAColor(value);
        Assert.Equal(expectedCode, c.Oca);
        Assert.Equal(value, c.Value);
    }

    [Fact]
    public void ParseAsOca_ProducesOcaColor()
    {
        FopColor c = ColorUtil.ParseColorString(null, "oca(red)")!;
        var oca = Assert.IsType<OCAColor>(c);
        Assert.Equal(OCAColorValue.Red, oca.Value);
    }

    [Fact]
    public void Oca_ColorComponents_Red()
    {
        var c = new OCAColor(OCAColorValue.Red);
        float[]? rgb = c.GetRgbColorComponents();
        Assert.NotNull(rgb);
        Assert.Equal([1.0f, 0f, 0f], rgb);
    }

    [Fact]
    public void Oca_ColorComponents_Brown()
    {
        var c = new OCAColor(OCAColorValue.Brown);
        float[]? rgb = c.GetRgbColorComponents();
        Assert.NotNull(rgb);
        Assert.Equal([0.565f, 0.188f, 0f], rgb);
    }

    [Fact]
    public void Oca_DeviceDefault_HasNoRgbMapping()
    {
        var c = new OCAColor(OCAColorValue.DeviceDefault);
        Assert.Null(c.GetRgbColorComponents());
    }

    [Fact]
    public void Oca_Equality()
    {
        Assert.Equal(new OCAColor(OCAColorValue.Green), new OCAColor(OCAColorValue.Green));
        Assert.NotEqual(new OCAColor(OCAColorValue.Green), new OCAColor(OCAColorValue.Cyan));
    }
}

public class OcaColorSpaceTests
{
    [Fact]
    public void ToRgb_MapsKnownValues()
    {
        var cs = new OCAColorSpace();
        Assert.Equal<float>([0f, 0f, 1.0f], cs.ToRgb([(int)OCAColorValue.Blue])!);
        Assert.Equal<float>([1.0f, 1.0f, 0f], cs.ToRgb([(int)OCAColorValue.Yellow])!);
        Assert.Equal<float>([0f, 0f, 0f], cs.ToRgb([(int)OCAColorValue.Black])!);
    }

    [Fact]
    public void ToRgb_UnknownReturnsNull()
    {
        var cs = new OCAColorSpace();
        Assert.Null(cs.ToRgb([0x9999]));
    }

    [Fact]
    public void FromRgb_NotSupported()
    {
        var cs = new OCAColorSpace();
        Assert.Throws<NotSupportedException>(() => cs.FromRgb([1f, 0f, 0f]));
    }

    [Fact]
    public void FromCieXyz_NotSupported()
    {
        var cs = new OCAColorSpace();
        Assert.Throws<NotSupportedException>(() => cs.FromCieXyz([0f, 0f, 0f]));
    }

    [Fact]
    public void NumComponents_IsOne()
    {
        Assert.Equal(1, new OCAColorSpace().NumComponents);
    }
}

public class ColorWithFallbackTests
{
    [Fact]
    public void CarriesFallbackAndComponents()
    {
        FopColor fallback = FopColor.FromFloat(1.0f, 0.8f, 0.0f);
        var cwf = new ColorWithFallback("#Separation:Postgelb", [1.0f], fallback);
        Assert.Same(fallback, cwf.FallbackColor);
        Assert.Equal([1.0f], [.. cwf.Components]);
        Assert.Equal("#Separation:Postgelb", cwf.ColorSpaceName);
        // The packed sRGB value comes from the fallback.
        Assert.Equal(fallback.Red, cwf.Red);
    }

    [Fact]
    public void Equality_RequiresMatchingFallbackAndComponents()
    {
        FopColor fallback = FopColor.FromFloat(1.0f, 0.8f, 0.0f);
        var a = new ColorWithFallback("#Separation:Postgelb", [1.0f], fallback);
        var b = new ColorWithFallback("#Separation:Postgelb", [1.0f], fallback);
        var c = new ColorWithFallback("#Separation:Other", [1.0f], fallback);
        Assert.Equal(a, b);
        Assert.NotEqual(a, c);
    }
}
