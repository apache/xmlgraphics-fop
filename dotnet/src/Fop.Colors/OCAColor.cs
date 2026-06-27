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
/// A single-component colour representation mostly used in AFP/OCA documents to
/// represent text foreground colour. See also <see cref="OCAColorSpace"/>.
/// <para>
/// Port of <c>org.apache.fop.util.OCAColor</c>. In Java this extended
/// <c>java.awt.Color</c> via <c>super(oca.value)</c> &#8212; the <c>Color(int rgb)</c>
/// constructor, which interprets the int as 0x00RRGGBB and forces alpha to 255.
/// The OCA code therefore lives in the low 16 bits of the packed value, which is
/// how <see cref="Oca"/> recovers it (<c>getRGB() &amp; 0xFFFF</c>).
/// </para>
/// </summary>
public sealed class OCAColor : FopColor
{
    private readonly OCAColorValue _value;

    /// <summary>
    /// Creates an OCA colour for the given Standard OCA Color Value.
    /// </summary>
    public OCAColor(OCAColorValue oca)
        // Mirror super(oca.value): pack the OCA code as 0x00RRGGBB with alpha 255.
        : base(unchecked((int)0xFF000000) | ((int)oca & 0x00FFFFFF))
    {
        _value = oca;
    }

    /// <summary>The Standard OCA Color Value this colour represents.</summary>
    public OCAColorValue Value => _value;

    /// <summary>
    /// The two-byte OCA colour code, equivalent to Java's <c>getOCA()</c>
    /// (<c>getRGB() &amp; 0xFFFF</c>).
    /// </summary>
    public int Oca => Rgb & 0xFFFF;

    /// <summary>
    /// The OCA colour space for this colour.
    /// </summary>
    public OCAColorSpace OCAColorSpace => new();

    /// <inheritdoc/>
    public override string? ColorSpaceName => "#OCA";

    /// <summary>
    /// The sRGB components for this OCA colour, or <c>null</c> when the OCA value
    /// has no defined RGB mapping (e.g. device-default, medium-color). Mirrors
    /// Java's <c>getColorComponents(sRGB, null)</c> which delegated to
    /// <c>OCAColorSpace.toRGB(new float[]{ getOCA() })</c>.
    /// </summary>
    public float[]? GetRgbColorComponents()
    {
        var oca = new OCAColorSpace();
        return oca.ToRgb([Oca]);
    }

    /// <inheritdoc/>
    public override bool Equals(FopColor? other)
        => other is OCAColor o && o._value == _value;

    /// <inheritdoc/>
    public override int GetHashCode() => HashCode.Combine(typeof(OCAColor), (int)_value);
}
