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

using System.Numerics;

namespace Fop.Util;

/// <summary>
/// Utility class for unit conversions.
/// <para>
/// Port of <c>org.apache.fop.util.UnitConv</c> (which delegates to
/// <c>org.apache.xmlgraphics.util.UnitConv</c>); the constants and formulas are inlined here.
/// The Java affine-transform helpers used <c>java.awt.geom.AffineTransform</c>; this port uses
/// <see cref="Matrix3x2"/> instead, converting only the translation components.
/// </para>
/// </summary>
public static class UnitConv
{
    /// <summary>Conversion factor from millimeters to inches.</summary>
    public const float In2Mm = 25.4f;

    /// <summary>Conversion factor from centimeters to inches.</summary>
    public const float In2Cm = 2.54f;

    /// <summary>Conversion factor from inches to points.</summary>
    public const int In2Pt = 72;

    /// <summary>Converts millimeters (mm) to points (pt).</summary>
    public static double Mm2Pt(double mm) => mm * In2Pt / In2Mm;

    /// <summary>Converts millimeters (mm) to millipoints (mpt).</summary>
    public static double Mm2Mpt(double mm) => mm * 1000 * In2Pt / In2Mm;

    /// <summary>Converts points (pt) to millimeters (mm).</summary>
    public static double Pt2Mm(double pt) => pt * In2Mm / In2Pt;

    /// <summary>Converts millimeters (mm) to inches (in).</summary>
    public static double Mm2In(double mm) => mm / In2Mm;

    /// <summary>
    /// Converts inches (in) to millimeters (mm). Named <c>InToMm</c> (not <c>In2Mm</c>) to avoid
    /// colliding with the <see cref="In2Mm"/> constant, which differs from the Java method only
    /// by case.
    /// </summary>
    public static double InToMm(double @in) => @in * In2Mm;

    /// <summary>Converts inches (in) to millipoints (mpt).</summary>
    public static double InToMpt(double @in) => @in * In2Pt * 1000;

    /// <summary>
    /// Converts inches (in) to points (pt). Named <c>InToPt</c> (not <c>In2Pt</c>) to avoid
    /// colliding with the <see cref="In2Pt"/> constant.
    /// </summary>
    public static double InToPt(double @in) => @in * In2Pt;

    /// <summary>Converts millipoints (mpt) to inches (in).</summary>
    public static double Mpt2In(double mpt) => mpt / In2Pt / 1000;

    /// <summary>Converts millimeters (mm) to pixels (px) at the given resolution.</summary>
    /// <param name="mm">the value in mm.</param>
    /// <param name="resolution">the resolution in dpi (dots per inch).</param>
    public static double Mm2Px(double mm, int resolution) => Mm2In(mm) * resolution;

    /// <summary>Converts millipoints (mpt) to pixels (px) at the given resolution.</summary>
    /// <param name="mpt">the value in mpt.</param>
    /// <param name="resolution">the resolution in dpi (dots per inch).</param>
    public static double Mpt2Px(double mpt, int resolution) => Mpt2In(mpt) * resolution;

    /// <summary>
    /// Converts a millipoint-based transformation matrix to points. Only the translation
    /// components are scaled (divided by 1000); the linear part is dimensionless.
    /// </summary>
    /// <param name="at">a millipoint-based transformation matrix.</param>
    /// <returns>a point-based transformation matrix.</returns>
    public static Matrix3x2 MptToPt(Matrix3x2 at) => at with
    {
        M31 = at.M31 / 1000f,
        M32 = at.M32 / 1000f,
    };

    /// <summary>
    /// Converts a point-based transformation matrix to millipoints. Only the translation
    /// components are scaled (multiplied by 1000); the linear part is dimensionless.
    /// </summary>
    /// <param name="at">a point-based transformation matrix.</param>
    /// <returns>a millipoint-based transformation matrix.</returns>
    public static Matrix3x2 PtToMpt(Matrix3x2 at) => at with
    {
        M31 = at.M31 * 1000f,
        M32 = at.M32 * 1000f,
    };
}
