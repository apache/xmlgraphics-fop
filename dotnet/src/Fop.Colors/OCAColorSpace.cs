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
/// The OCA colour space: a subset of RGB with a limited set of named colours.
/// The colour value is one unsigned component selecting a named colour from the
/// Standard OCA Color Value table.
/// <para>
/// Port of <c>org.apache.fop.util.OCAColorSpace</c>. Java extended
/// <c>java.awt.color.ColorSpace</c> (an abstract type); here it is a small
/// standalone helper since the heavy AWT colour-space machinery is not ported.
/// </para>
/// </summary>
public sealed class OCAColorSpace
{
    /// <summary>The number of components in this colour space (always 1).</summary>
    public int NumComponents => 1;

    /// <summary>
    /// Conversion from CIE XYZ to OCA is not possible.
    /// </summary>
    public float[] FromCieXyz(float[] colorValue)
        => throw new NotSupportedException("Color conversion from CIE XYZ to OCA is not possible");

    /// <summary>
    /// Conversion from RGB to OCA is not possible.
    /// </summary>
    public float[] FromRgb(float[] rgbValue)
        => throw new NotSupportedException("Color conversion from RGB to OCA is not possible");

    /// <summary>
    /// Converts the single OCA component to sRGB, then to CIE XYZ.
    /// </summary>
    public float[] ToCieXyz(float[] colorValue)
    {
        // Java delegated to sRGB.toCIEXYZ(toRGB(...)). The sRGB->XYZ conversion
        // is part of java.awt.color.ColorSpace, which is not ported in this
        // slice; only the OCA->RGB mapping below is needed by the FOP port.
        throw new NotSupportedException(
            "Color conversion from OCA to CIE XYZ requires the sRGB color space (not ported)");
        // TODO: sRGB -> CIE XYZ conversion (needs java.awt.color.ColorSpace port)
    }

    /// <summary>
    /// Maps the single OCA component (a value from <see cref="OCAColorValue"/>) to
    /// its sRGB equivalent. Returns <c>null</c> for values that have no defined RGB
    /// mapping (e.g. device-default, medium-color), exactly as the Java original did.
    /// </summary>
    public float[]? ToRgb(float[] colorValue)
    {
        int oca = (int)colorValue[0];
        return oca switch
        {
            (int)OCAColorValue.Black => [0f, 0f, 0f],
            (int)OCAColorValue.Blue => [0f, 0f, 1.0f],
            (int)OCAColorValue.Brown => [0.565f, 0.188f, 0f],
            (int)OCAColorValue.Cyan => [0f, 1.0f, 1.0f],
            (int)OCAColorValue.Green => [0f, 1.0f, 0f],
            (int)OCAColorValue.Magenta => [1.0f, 0f, 1.0f],
            (int)OCAColorValue.Red => [1.0f, 0f, 0f],
            (int)OCAColorValue.Yellow => [1.0f, 1.0f, 0f],
            _ => null,
        };
    }
}
