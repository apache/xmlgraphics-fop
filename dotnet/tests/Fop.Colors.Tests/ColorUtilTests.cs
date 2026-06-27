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

/// <summary>
/// Ports the non-ICC scenarios of <c>org.apache.fop.util.ColorUtilTestCase</c>.
/// The ICC-dependent scenarios (testRGBICC, testNamedColorProfile, and the real
/// profile branches) require a FOUserAgent/ICC loader and are deferred.
/// </summary>
public class ColorUtilTests
{
    // --- testSerialization ---

    [Fact]
    public void Serialization_RoundsFloatToHex()
    {
        // 0.5f -> (int)(0.5*255+0.5) = 128 = 0x80 (the test notes the old code gave 7f)
        FopColor col = FopColor.FromFloat(1.0f, 1.0f, 0.5f, 1.0f);
        Assert.Equal("#ffff80", ColorUtil.ColorToString(col));
    }

    [Fact]
    public void Serialization_EmitsAlphaWhenNotOpaque()
    {
        FopColor col = FopColor.FromFloat(1.0f, 0.0f, 0.0f, 0.8f);
        Assert.Equal("#ff0000cc", ColorUtil.ColorToString(col));
    }

    // --- testDeserialization ---

    [Fact]
    public void Deserialization_HexRrggbb()
    {
        FopColor col = ColorUtil.ParseColorString(null, "#ffff7f")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(255, col.Green);
        Assert.Equal(127, col.Blue);
        Assert.Equal(255, col.Alpha);
    }

    [Fact]
    public void Deserialization_HexRrggbbaa()
    {
        FopColor col = ColorUtil.ParseColorString(null, "#ff0000cc")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(0, col.Green);
        Assert.Equal(0, col.Blue);
        Assert.Equal(204, col.Alpha);
    }

    [Fact]
    public void Deserialization_HexRgbShort()
    {
        // #f00 -> each nibble * 0x11
        FopColor col = ColorUtil.ParseColorString(null, "#f00")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(0, col.Green);
        Assert.Equal(0, col.Blue);
        Assert.Equal(255, col.Alpha);
    }

    [Fact]
    public void Deserialization_HexRgbaShort()
    {
        // Quirk: in the #RGBA short form the RGB nibbles are expanded (*0x11) but
        // the alpha is read as the 2 chars at index 3 (substring(3)), NOT a nibble.
        // So #f008 -> r=ff, g=00, b=00, a=0x08.
        FopColor col = ColorUtil.ParseColorString(null, "#f008")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(0, col.Green);
        Assert.Equal(0, col.Blue);
        Assert.Equal(0x08, col.Alpha);
    }

    // --- testEquals ---

    [Fact]
    public void Equals_SameHexSpec()
    {
        FopColor col1 = ColorUtil.ParseColorString(null, "#ff0000cc")!;
        FopColor col2 = ColorUtil.ParseColorString(null, "#ff0000cc")!;
        Assert.Equal(col1, col2);
    }

    [Fact]
    public void Equals_DifferentCmykComponentsButSameSrgbAndProfile()
    {
        // Both share the same sRGB primary (0.5,0.5,0.5) and #CMYK profile but
        // differ in the CMYK components; Java's java.awt.Color.equals (packed-int
        // based) considers them equal while isSameColor() would not. Our
        // ColorWithAlternatives.Equals compares components too, so these differ.
        FopColor col1 = ColorUtil.ParseColorString(null, "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)")!;
        FopColor col2 = ColorUtil.ParseColorString(null, "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.5,0.5,0.5,0.0)")!;
        Assert.Equal(col1.Red, col2.Red);
        Assert.Equal(col1.Green, col2.Green);
        Assert.Equal(col1.Blue, col2.Blue);
        Assert.NotEqual(col1, col2);
    }

    // --- testRGB ---

    [Fact]
    public void Rgb_ParsesAndIsSrgb()
    {
        FopColor col = ColorUtil.ParseColorString(null, "rgb(255, 40, 0)")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(40, col.Green);
        Assert.Equal(0, col.Blue);
        Assert.Equal(255, col.Alpha);
        Assert.Null(col.ColorSpaceName);
    }

    [Fact]
    public void Rgb_Percentages()
    {
        FopColor col = ColorUtil.ParseColorString(null, "rgb(100%, 0%, 50%)")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(0, col.Green);
        // 50% -> 0.5 -> (int)(0.5*255+0.5) = 128
        Assert.Equal(128, col.Blue);
    }

    // --- testCMYK ---

    [Fact]
    public void Cmyk_Yellow()
    {
        FopColor col = ColorUtil.ParseColorString(null, "cmyk(0.0, 0.0, 1.0, 0.0)")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(255, col.Green);
        Assert.Equal(0, col.Blue);
        Assert.Equal(ColorUtil.CmykPseudoProfile, col.ColorSpaceName);
        float[] comps = [.. col.Components];
        Assert.Equal(4, comps.Length);
        Assert.Equal(0f, comps[0]);
        Assert.Equal(0f, comps[1]);
        Assert.Equal(1f, comps[2]);
        Assert.Equal(0f, comps[3]);
        Assert.Equal("fop-rgb-icc(1.0,1.0,0.0,#CMYK,,0.0,0.0,1.0,0.0)", ColorUtil.ColorToString(col));
    }

    [Fact]
    public void Cmyk_FractionalRoundTripsFloatComponents()
    {
        FopColor col = ColorUtil.ParseColorString(null, "cmyk(0.0274, 0.2196, 0.3216, 0.0)")!;
        Assert.InRange(col.Red, 247, 249);
        Assert.InRange(col.Green, 198, 200);
        Assert.InRange(col.Blue, 171, 174);
        // Serialization must preserve the exact float arithmetic (1 - 0.3216 = 0.67840004).
        Assert.Equal("fop-rgb-icc(0.9726,0.7804,0.67840004,#CMYK,,0.0274,0.2196,0.3216,0.0)",
            ColorUtil.ColorToString(col));
    }

    [Fact]
    public void FopRgbIcc_CmykRoundTrip()
    {
        const string spec = "fop-rgb-icc(1.0,1.0,0.0,#CMYK,,0.0,0.0,1.0,0.0)";
        FopColor col = ColorUtil.ParseColorString(null, spec)!;
        Assert.Equal(255, col.Red);
        Assert.Equal(255, col.Green);
        Assert.Equal(0, col.Blue);
        float[] comps = [.. col.Components];
        Assert.Equal(4, comps.Length);
        Assert.Equal(0f, comps[0]);
        Assert.Equal(0f, comps[1]);
        Assert.Equal(1f, comps[2]);
        Assert.Equal(0f, comps[3]);
        Assert.Equal(spec, ColorUtil.ColorToString(col));
    }

    [Fact]
    public void FopRgbIcc_CmykGrayRoundTrip()
    {
        const string spec = "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)";
        FopColor col = ColorUtil.ParseColorString(null, spec)!;
        Assert.InRange(col.Red, 126, 128);
        Assert.InRange(col.Green, 126, 128);
        Assert.InRange(col.Blue, 126, 128);
        float[] comps = [.. col.Components];
        Assert.Equal(0f, comps[0]);
        Assert.Equal(0f, comps[1]);
        Assert.Equal(0f, comps[2]);
        Assert.Equal(0.5f, comps[3]);
        Assert.Equal(spec, ColorUtil.ColorToString(col));
    }

    [Fact]
    public void Cmyk_AndFopRgbIccCmyk_HaveSameComponents()
    {
        FopColor cmyk = ColorUtil.ParseColorString(null, "cmyk(0,0,0,0.5)")!;
        FopColor icc = ColorUtil.ParseColorString(null, "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)")!;
        Assert.Equal<float>([.. cmyk.Components], [.. icc.Components]);
    }

    // --- testSeparationColor ---

    [Fact]
    public void Separation_ParsesNameAndFullTintAndRoundTrips()
    {
        const string spec = "fop-rgb-icc(1.0,0.8,0.0,#Separation,,Postgelb)";
        FopColor col = ColorUtil.ParseColorString(null, spec)!;

        // Fallback sRGB primary.
        Assert.Equal(255, col.Red);
        Assert.Equal(204, col.Green);
        Assert.Equal(0, col.Blue);

        var cwf = Assert.IsType<ColorWithFallback>(col);
        Assert.False(cwf.HasAlternativeColors);
        Assert.StartsWith(ColorUtil.SeparationPseudoProfile, cwf.ColorSpaceName);
        Assert.EndsWith("Postgelb", cwf.ColorSpaceName);

        // Tint defaults to full (1.0) when not specified.
        float[] comps = [.. cwf.Components];
        Assert.Single(comps);
        Assert.Equal(1f, comps[0]);

        Assert.Equal(spec, ColorUtil.ColorToString(col));
    }

    // --- testAlphaColor ---

    [Fact]
    public void Alpha_PseudoProfileRoundTrips()
    {
        const string spec = "fop-rgb-icc(0.6,0.6,0.4,#alpha,0.4,#CMYK,,0.0,0.0,0.2,0.4)";
        FopColor col = ColorUtil.ParseColorString(null, spec)!;
        // alpha 0.4 -> (int)(0.4*255+0.5) = 102
        Assert.Equal(102, col.Alpha);
        Assert.Equal(spec, ColorUtil.ColorToString(col));
    }

    // --- named colours / system-color ---

    [Fact]
    public void NamedColor_Resolves()
    {
        FopColor col = ColorUtil.ParseColorString(null, "red")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(0, col.Green);
        Assert.Equal(0, col.Blue);
    }

    [Fact]
    public void NamedColor_IsCaseInsensitive()
    {
        FopColor col = ColorUtil.ParseColorString(null, "CornflowerBlue")!;
        Assert.Equal(FopColor.FromRgb(100, 149, 237), col);
    }

    [Fact]
    public void Transparent_IsZeroAlpha()
    {
        FopColor col = ColorUtil.ParseColorString(null, "transparent")!;
        Assert.Equal(0, col.Alpha);
        Assert.Equal(0, col.Red);
    }

    [Fact]
    public void SystemColor_ResolvesViaMap()
    {
        FopColor col = ColorUtil.ParseColorString(null, "system-color(blue)")!;
        Assert.Equal(FopColor.FromRgb(0, 0, 255), col);
    }

    // --- java.awt.Color toString form ---

    [Fact]
    public void JavaAwtColor_FormParses()
    {
        FopColor col = ColorUtil.ParseColorString(null, "java.awt.Color[r=255,g=128,b=0]")!;
        Assert.Equal(255, col.Red);
        Assert.Equal(128, col.Green);
        Assert.Equal(0, col.Blue);
    }

    // --- error handling ---

    [Fact]
    public void NullValue_ReturnsNull()
    {
        Assert.Null(ColorUtil.ParseColorString(null, null));
    }

    [Fact]
    public void UnknownColor_Throws()
    {
        Assert.Throws<ColorParseException>(() => ColorUtil.ParseColorString(null, "notacolor"));
    }

    [Fact]
    public void Url_NotSupported_Throws()
    {
        Assert.Throws<ColorParseException>(() => ColorUtil.ParseColorString(null, "url(#foo)"));
    }

    [Fact]
    public void Rgb_OutOfRange_Throws()
    {
        Assert.Throws<ColorParseException>(() => ColorUtil.ParseColorString(null, "rgb(300, 0, 0)"));
    }

    [Fact]
    public void BadHexLength_Throws()
    {
        // len 3 (#ab) is not one of 4/5/7/9, so it is rejected.
        Assert.Throws<ColorParseException>(() => ColorUtil.ParseColorString(null, "#ab"));
    }

    [Fact]
    public void Oca_Unknown_Throws()
    {
        Assert.Throws<ColorParseException>(() => ColorUtil.ParseColorString(null, "oca(turquoise)"));
    }

    // --- helpers ---

    [Theory]
    [InlineData("#CMYK", true)]
    [InlineData("#cmyk", true)]
    [InlineData("#Separation", true)]
    [InlineData("sRGB", false)]
    public void IsPseudoProfile_Works(string name, bool expected)
        => Assert.Equal(expected, ColorUtil.IsPseudoProfile(name));

    [Fact]
    public void IsGray_DetectsGray()
    {
        Assert.True(ColorUtil.IsGray(FopColor.FromRgb(128, 128, 128)));
        Assert.False(ColorUtil.IsGray(FopColor.FromRgb(128, 128, 129)));
    }

    [Fact]
    public void LightenColor_MovesTowardWhite()
    {
        FopColor lighter = ColorUtil.LightenColor(FopColor.FromRgb(100, 100, 100), 0.5f);
        Assert.True(lighter.Red > 100);
    }

    [Fact]
    public void LightenColor_NegativeDarkens()
    {
        FopColor darker = ColorUtil.LightenColor(FopColor.FromRgb(200, 200, 200), -0.5f);
        Assert.True(darker.Red < 200);
    }
}
