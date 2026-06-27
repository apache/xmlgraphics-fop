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
/// Parses an SVG <c>transform</c> attribute (a list of <c>translate</c>/<c>scale</c>/<c>rotate</c>/
/// <c>matrix</c>/<c>skewX</c>/<c>skewY</c> functions) into a single composed <see cref="SvgMatrix"/>.
/// The functions compose left-to-right, matching SVG's nesting semantics.
/// </summary>
internal static class SvgTransform
{
    public static SvgMatrix Parse(string transform)
    {
        SvgMatrix m = SvgMatrix.Identity;
        int i = 0;
        while (i < transform.Length)
        {
            while (i < transform.Length && (char.IsWhiteSpace(transform[i]) || transform[i] == ','))
            {
                i++;
            }

            int nameStart = i;
            while (i < transform.Length && (char.IsLetter(transform[i])))
            {
                i++;
            }

            string name = transform[nameStart..i].Trim();
            if (name.Length == 0)
            {
                break;
            }

            int open = transform.IndexOf('(', i);
            if (open < 0)
            {
                break;
            }

            int close = transform.IndexOf(')', open);
            if (close < 0)
            {
                break;
            }

            double[] args = SvgParser.ParseNumberList(transform[(open + 1)..close]);
            i = close + 1;

            m = m.Multiply(ToMatrix(name, args));
        }

        return m;
    }

    private static SvgMatrix ToMatrix(string name, double[] a) => name switch
    {
        "translate" => SvgMatrix.Translate(Arg(a, 0), Arg(a, 1)),
        "scale" => SvgMatrix.Scale(Arg(a, 0, 1), a.Length > 1 ? a[1] : Arg(a, 0, 1)),
        "rotate" => RotateAbout(a),
        "matrix" when a.Length == 6 => new SvgMatrix(a[0], a[1], a[2], a[3], a[4], a[5]),
        "skewX" => SvgMatrix.SkewX(Arg(a, 0)),
        "skewY" => SvgMatrix.SkewY(Arg(a, 0)),
        _ => SvgMatrix.Identity,
    };

    private static SvgMatrix RotateAbout(double[] a)
    {
        double angle = Arg(a, 0);
        if (a.Length >= 3)
        {
            double cx = a[1], cy = a[2];
            return SvgMatrix.Translate(cx, cy)
                .Multiply(SvgMatrix.Rotate(angle))
                .Multiply(SvgMatrix.Translate(-cx, -cy));
        }

        return SvgMatrix.Rotate(angle);
    }

    private static double Arg(double[] a, int index, double @default = 0) =>
        index < a.Length ? a[index] : @default;
}
