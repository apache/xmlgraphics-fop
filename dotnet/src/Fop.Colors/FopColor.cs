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

using System.Globalization;

namespace Fop.Colors;

/// <summary>
/// Immutable managed colour value. This is the C# port's stand-in for
/// <c>java.awt.Color</c> throughout the FOP port.
/// <para>
/// The primary colour is always an sRGB value, stored as four 8-bit components
/// (red, green, blue, alpha) packed into a single 32-bit ARGB integer, exactly
/// as <c>java.awt.Color</c> does. This preserves the two distinct rounding
/// behaviours of the Java type, which the original FOP code (and its tests)
/// depend on:
/// </para>
/// <list type="bullet">
/// <item>The <c>(int r, int g, int b[, int a])</c> constructor stores bytes
/// verbatim. Mirrored by <see cref="FromArgb(int,int,int,int)"/>.</item>
/// <item>The <c>(float r, float g, float b[, float a])</c> constructor rounds
/// each component with <c>(int)(c * 255 + 0.5)</c>. Mirrored by
/// <see cref="FromFloat(float,float,float,float)"/>.</item>
/// </list>
/// <para>
/// Optional <see cref="ColorSpaceName"/> and <see cref="Components"/> carry an
/// alternate colour-space representation (e.g. CMYK, a named/separation colour,
/// or an ICC profile) whose actual resolution is deferred in this slice of the
/// port. When absent, <see cref="Components"/> reports the three sRGB components.
/// </para>
/// </summary>
public class FopColor : IEquatable<FopColor>
{
    private readonly int _value; // packed 0xAARRGGBB, like java.awt.Color

    // Precise sRGB components (r,g,b,a in 0..1) when this colour was built from
    // floats, else null. java.awt.Color keeps the original float values too
    // ("frgbvalue") and returns them from getComponents(); FOP's serialization
    // relies on that to round-trip e.g. 0.67840004 rather than 173/255.
    private readonly float[]? _frgb;

    /// <summary>
    /// Creates an sRGB colour from the packed 0xAARRGGBB integer.
    /// </summary>
    protected FopColor(int argb)
    {
        _value = argb;
        _frgb = null;
    }

    private FopColor(int argb, float[] frgb)
    {
        _value = argb;
        _frgb = frgb;
    }

    /// <summary>
    /// Creates an sRGB colour from 8-bit components (0..255). Stored verbatim,
    /// mirroring <c>new java.awt.Color(int, int, int, int)</c>.
    /// </summary>
    public static FopColor FromArgb(int r, int g, int b, int a = 255)
    {
        CheckByte(r, nameof(r));
        CheckByte(g, nameof(g));
        CheckByte(b, nameof(b));
        CheckByte(a, nameof(a));
        return new FopColor(Pack(r, g, b, a));
    }

    /// <summary>
    /// Creates an opaque sRGB colour from 8-bit components (0..255).
    /// </summary>
    public static FopColor FromRgb(int r, int g, int b) => FromArgb(r, g, b, 255);

    /// <summary>
    /// Creates an sRGB colour from float components (0..1), rounding each with
    /// <c>(int)(c * 255 + 0.5)</c> exactly like <c>new java.awt.Color(float...)</c>.
    /// </summary>
    public static FopColor FromFloat(float r, float g, float b, float a = 1.0f)
    {
        int packed = Pack(
            RoundFloatComponent(r),
            RoundFloatComponent(g),
            RoundFloatComponent(b),
            RoundFloatComponent(a));
        return new FopColor(packed, [r, g, b, a]);
    }

    /// <summary>The red component (0..255).</summary>
    public int Red => (_value >> 16) & 0xFF;

    /// <summary>The green component (0..255).</summary>
    public int Green => (_value >> 8) & 0xFF;

    /// <summary>The blue component (0..255).</summary>
    public int Blue => _value & 0xFF;

    /// <summary>The alpha component (0..255). 255 means fully opaque.</summary>
    public int Alpha => (_value >> 24) & 0xFF;

    /// <summary>The packed 0xAARRGGBB value, equivalent to Java's <c>getRGB()</c>.</summary>
    public int Rgb => _value;

    /// <summary>The red component as a float in 0..1.</summary>
    public float RedFloat => Red / 255f;

    /// <summary>The green component as a float in 0..1.</summary>
    public float GreenFloat => Green / 255f;

    /// <summary>The blue component as a float in 0..1.</summary>
    public float BlueFloat => Blue / 255f;

    /// <summary>The alpha component as a float in 0..1.</summary>
    public float AlphaFloat => Alpha / 255f;

    /// <summary>
    /// The precise sRGB components <c>[r, g, b, a]</c> (0..1). When this colour was
    /// constructed from floats these are the original, un-rounded values (matching
    /// Java's <c>Color.getComponents()</c>); otherwise they are derived from the
    /// 8-bit components. Used by serialization to round-trip exact float values.
    /// </summary>
    public float[] GetSRgbComponents()
        => _frgb is not null ? (float[])_frgb.Clone() : [RedFloat, GreenFloat, BlueFloat, AlphaFloat];

    /// <summary>
    /// The name of the colour space the <see cref="Components"/> belong to, or
    /// <c>null</c> for a plain sRGB colour. Pseudo-profile names such as
    /// <c>#CMYK</c> and <c>#Separation</c> are used by the FOP port.
    /// </summary>
    public virtual string? ColorSpaceName => null;

    /// <summary>
    /// The native colour components for <see cref="ColorSpaceName"/>. For a plain
    /// sRGB colour this is the three sRGB components (0..1). For an alternate
    /// colour space (CMYK, separation, ICC) these are the alternate components.
    /// </summary>
    public virtual IReadOnlyList<float> Components => [RedFloat, GreenFloat, BlueFloat];

    /// <summary>
    /// Indicates whether this colour carries an alternate colour space (i.e. a
    /// non-sRGB representation that should be used in preference where supported).
    /// </summary>
    public virtual bool HasAlternativeColors => ColorSpaceName is not null;

    /// <summary>True when red, green and blue are equal.</summary>
    public bool IsGray => Red == Green && Green == Blue;

    /// <summary>
    /// Value equality based on the packed sRGB/alpha integer, matching
    /// <c>java.awt.Color.equals</c>. Subclasses refine this with their extra state.
    /// </summary>
    public virtual bool Equals(FopColor? other)
    {
        if (other is null)
        {
            return false;
        }

        if (ReferenceEquals(this, other))
        {
            return true;
        }

        // java.awt.Color.equals only compares the packed value, but it also
        // requires the runtime class to be Color (an OCAColor/ColorWithFallback
        // is not equal to a plain Color). We mirror that with an exact-type check.
        return GetType() == other.GetType() && _value == other._value;
    }

    /// <inheritdoc/>
    public override bool Equals(object? obj) => Equals(obj as FopColor);

    /// <inheritdoc/>
    public override int GetHashCode() => _value;

    /// <summary>
    /// Mirrors <c>java.awt.Color.toString()</c>:
    /// <c>FopColor[r=...,g=...,b=...]</c> (alpha is not part of Java's output).
    /// </summary>
    public override string ToString()
    {
        return string.Create(CultureInfo.InvariantCulture,
            $"{GetType().Name}[r={Red},g={Green},b={Blue}]");
    }

    private static int Pack(int r, int g, int b, int a)
        => ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

    private static int RoundFloatComponent(float c)
    {
        // java.awt.Color(float...) uses (int)(c * 255 + 0.5).
        return (int)(c * 255 + 0.5f);
    }

    private static void CheckByte(int value, string name)
    {
        if (value is < 0 or > 255)
        {
            throw new ArgumentOutOfRangeException(name,
                "Color parameter outside of expected range: 0..255");
        }
    }
}
