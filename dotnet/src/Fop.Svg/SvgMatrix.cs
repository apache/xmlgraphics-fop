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

namespace Fop.Svg;

/// <summary>
/// A 2-D affine transform <c>[a b c d e f]</c> in the SVG <c>matrix(a,b,c,d,e,f)</c> convention,
/// mapping a point (x,y) to (a*x + c*y + e, b*x + d*y + f). Used (in <c>double</c> precision) to
/// accumulate SVG element transforms while flattening a document.
/// </summary>
internal readonly record struct SvgMatrix(double A, double B, double C, double D, double E, double F)
{
    /// <summary>The identity transform.</summary>
    public static readonly SvgMatrix Identity = new(1, 0, 0, 1, 0, 0);

    /// <summary>Maps a point through this transform.</summary>
    public (double X, double Y) Apply(double x, double y) => (A * x + C * y + E, B * x + D * y + F);

    /// <summary>Maps a vector (ignoring translation) through this transform.</summary>
    public (double X, double Y) ApplyVector(double x, double y) => (A * x + C * y, B * x + D * y);

    /// <summary>
    /// Returns <c>this * o</c>: the transform that applies <paramref name="o"/> first, then this one
    /// (so post-multiplying nests a child transform inside the current one).
    /// </summary>
    public SvgMatrix Multiply(SvgMatrix o) => new(
        A * o.A + C * o.B,
        B * o.A + D * o.B,
        A * o.C + C * o.D,
        B * o.C + D * o.D,
        A * o.E + C * o.F + E,
        B * o.E + D * o.F + F);

    /// <summary>
    /// A representative uniform scale factor (the geometric mean of the x- and y-axis scale lengths),
    /// used to scale scalar quantities such as stroke width through the transform.
    /// </summary>
    public double MeanScale
    {
        get
        {
            double sx = Math.Sqrt(A * A + B * B);
            double sy = Math.Sqrt(C * C + D * D);
            return Math.Sqrt(sx * sy);
        }
    }

    public static SvgMatrix Translate(double tx, double ty) => new(1, 0, 0, 1, tx, ty);

    public static SvgMatrix Scale(double sx, double sy) => new(sx, 0, 0, sy, 0, 0);

    public static SvgMatrix Rotate(double degrees)
    {
        double r = degrees * Math.PI / 180.0;
        double cos = Math.Cos(r);
        double sin = Math.Sin(r);
        return new SvgMatrix(cos, sin, -sin, cos, 0, 0);
    }

    public static SvgMatrix SkewX(double degrees) => new(1, 0, Math.Tan(degrees * Math.PI / 180.0), 1, 0, 0);

    public static SvgMatrix SkewY(double degrees) => new(1, Math.Tan(degrees * Math.PI / 180.0), 0, 1, 0, 0);
}
