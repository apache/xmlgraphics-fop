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
/// An sRGB <see cref="FopColor"/> that also carries an alternate device colour
/// representation (its native components and colour-space name). Device colours
/// such as CMYK are stored this way: the sRGB value is the primary, with the CMYK
/// values as the alternate.
/// <para>
/// Stand-in for <c>org.apache.xmlgraphics.java2d.color.ColorWithAlternatives</c>
/// as used by FOP. The full xmlgraphics type holds an array of alternative
/// <c>Color</c> objects; here a single alternate colour space (name + native
/// components) suffices for the parsing/serialization paths that are ported.
/// </para>
/// </summary>
public sealed class ColorWithAlternatives : FopColor
{
    private readonly string _colorSpaceName;
    private readonly float[] _components;
    private readonly FopColor _sRgb;

    /// <summary>
    /// Creates a colour whose primary is the given sRGB colour and which carries
    /// the supplied alternate colour space.
    /// </summary>
    /// <param name="sRgb">the sRGB primary colour</param>
    /// <param name="colorSpaceName">the alternate colour-space name (e.g. <c>#CMYK</c>)</param>
    /// <param name="components">the native components in that colour space</param>
    public ColorWithAlternatives(FopColor sRgb, string colorSpaceName, float[] components)
        : base((sRgb ?? throw new ArgumentNullException(nameof(sRgb))).Rgb)
    {
        ArgumentNullException.ThrowIfNull(colorSpaceName);
        ArgumentNullException.ThrowIfNull(components);
        _sRgb = sRgb;
        _colorSpaceName = colorSpaceName;
        _components = (float[])components.Clone();
    }

    /// <summary>The sRGB primary colour, retaining its precise float components.</summary>
    public FopColor SRgb => _sRgb;

    /// <inheritdoc/>
    public override string? ColorSpaceName => _colorSpaceName;

    /// <inheritdoc/>
    public override IReadOnlyList<float> Components => _components;

    /// <inheritdoc/>
    public override bool HasAlternativeColors => true;

    /// <inheritdoc/>
    public override bool Equals(FopColor? other)
    {
        if (other is not ColorWithAlternatives cwa)
        {
            return false;
        }

        if (ReferenceEquals(this, other))
        {
            return true;
        }

        return Rgb == cwa.Rgb
            && _colorSpaceName == cwa._colorSpaceName
            && _components.AsSpan().SequenceEqual(cwa._components);
    }

    /// <inheritdoc/>
    public override int GetHashCode() => HashCode.Combine(Rgb, _colorSpaceName, _components.Length);
}
