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

namespace Fop.Colors;

/// <summary>
/// A <see cref="FopColor"/> carrying a fallback colour that FOP uses to
/// re-serialize colour specifications as textual functions. The fallback is
/// otherwise not used in producing output formats.
/// <para>
/// Port of <c>org.apache.fop.util.ColorWithFallback</c>. In Java this extended
/// <c>ColorWithAlternatives</c>; in this slice of the port the alternate
/// colour-space representation (space name + native components) is carried
/// directly on <see cref="FopColor"/>, so this type only adds the fallback.
/// </para>
/// </summary>
public sealed class ColorWithFallback : FopColor
{
    private readonly string? _colorSpaceName;
    private readonly float[] _components;
    private readonly FopColor _fallback;

    /// <summary>
    /// Creates a new colour with an alternate colour space and a fallback.
    /// The packed sRGB/alpha value of this colour is taken from the fallback so
    /// that the red/green/blue accessors return usable sRGB approximations.
    /// </summary>
    /// <param name="colorSpaceName">the name of the primary colour space (e.g. a
    /// pseudo-profile such as <c>#CMYK</c> or <c>#Separation</c>)</param>
    /// <param name="components">the native colour components for that space</param>
    /// <param name="fallback">the fallback colour (usually an sRGB colour)</param>
    public ColorWithFallback(string? colorSpaceName, float[] components, FopColor fallback)
        : base(fallback.Rgb)
    {
        ArgumentNullException.ThrowIfNull(components);
        ArgumentNullException.ThrowIfNull(fallback);
        _colorSpaceName = colorSpaceName;
        _components = (float[])components.Clone();
        _fallback = fallback;
    }

    /// <summary>The fallback colour (usually an sRGB colour).</summary>
    public FopColor FallbackColor => _fallback;

    /// <inheritdoc/>
    public override string? ColorSpaceName => _colorSpaceName;

    /// <inheritdoc/>
    public override IReadOnlyList<float> Components => _components;

    // A ColorWithFallback carries its alternate colour space directly; it does
    // not also carry a separate array of alternative Color objects.
    /// <inheritdoc/>
    public override bool HasAlternativeColors => false;

    /// <inheritdoc/>
    public override bool Equals(FopColor? other)
    {
        if (other is not ColorWithFallback cwf)
        {
            return false;
        }

        if (ReferenceEquals(this, other))
        {
            return true;
        }

        return Rgb == cwf.Rgb
            && _colorSpaceName == cwf._colorSpaceName
            && _components.AsSpan().SequenceEqual(cwf._components)
            && _fallback.Equals(cwf._fallback);
    }

    /// <inheritdoc/>
    public override int GetHashCode() => HashCode.Combine(Rgb, _colorSpaceName, _fallback);
}
