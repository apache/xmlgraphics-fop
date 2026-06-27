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

namespace Fop.Svg;

/// <summary>
/// Parses an SVG path <c>d</c> attribute into a flat list of <see cref="SvgPathSegment"/>s in the
/// element's local coordinate space. Supports the full command set
/// (<c>M/L/H/V/C/S/Q/T/A/Z</c> and their relative lower-case forms); elliptical arcs are converted to
/// a sequence of cubic Beziers. Malformed input stops parsing at the offending token (best-effort).
/// </summary>
internal static class SvgPathData
{
    public static List<SvgPathSegment> Parse(string d)
    {
        var segments = new List<SvgPathSegment>();
        var t = new Tokenizer(d);

        double curX = 0, curY = 0;     // current point
        double startX = 0, startY = 0; // current subpath start
        double lastCx = 0, lastCy = 0; // last cubic control reflection point
        double lastQx = 0, lastQy = 0; // last quadratic control reflection point
        char prevCmd = '\0';
        char cmd = '\0';

        while (t.HasMore)
        {
            char next = t.PeekCommand();
            if (next != '\0')
            {
                cmd = t.ReadCommand();
            }
            else if (cmd == '\0')
            {
                break; // numbers before any command: invalid
            }
            else if (cmd is 'M')
            {
                cmd = 'L'; // implicit line-to after the first move-to pair
            }
            else if (cmd is 'm')
            {
                cmd = 'l';
            }

            bool rel = char.IsLower(cmd);
            switch (char.ToUpperInvariant(cmd))
            {
                case 'M':
                {
                    if (!t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    curX = rel ? curX + x : x;
                    curY = rel ? curY + y : y;
                    startX = curX; startY = curY;
                    segments.Add(new SvgPathSegment(SvgVerb.MoveTo, curX, curY));
                    break;
                }

                case 'L':
                {
                    if (!t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    curX = rel ? curX + x : x;
                    curY = rel ? curY + y : y;
                    segments.Add(new SvgPathSegment(SvgVerb.LineTo, curX, curY));
                    break;
                }

                case 'H':
                {
                    if (!t.TryReadNumber(out double x)) { t.Stop(); break; }
                    curX = rel ? curX + x : x;
                    segments.Add(new SvgPathSegment(SvgVerb.LineTo, curX, curY));
                    break;
                }

                case 'V':
                {
                    if (!t.TryReadNumber(out double y)) { t.Stop(); break; }
                    curY = rel ? curY + y : y;
                    segments.Add(new SvgPathSegment(SvgVerb.LineTo, curX, curY));
                    break;
                }

                case 'C':
                {
                    if (!t.TryReadPair(out double c1x, out double c1y)
                        || !t.TryReadPair(out double c2x, out double c2y)
                        || !t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    if (rel) { c1x += curX; c1y += curY; c2x += curX; c2y += curY; x += curX; y += curY; }
                    segments.Add(new SvgPathSegment(SvgVerb.CubicTo, c1x, c1y, c2x, c2y, x, y));
                    lastCx = c2x; lastCy = c2y; curX = x; curY = y;
                    break;
                }

                case 'S':
                {
                    if (!t.TryReadPair(out double c2x, out double c2y)
                        || !t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    if (rel) { c2x += curX; c2y += curY; x += curX; y += curY; }
                    // First control is the reflection of the previous cubic's second control.
                    bool wasCubic = char.ToUpperInvariant(prevCmd) is 'C' or 'S';
                    double c1x = wasCubic ? 2 * curX - lastCx : curX;
                    double c1y = wasCubic ? 2 * curY - lastCy : curY;
                    segments.Add(new SvgPathSegment(SvgVerb.CubicTo, c1x, c1y, c2x, c2y, x, y));
                    lastCx = c2x; lastCy = c2y; curX = x; curY = y;
                    break;
                }

                case 'Q':
                {
                    if (!t.TryReadPair(out double cx, out double cy)
                        || !t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    if (rel) { cx += curX; cy += curY; x += curX; y += curY; }
                    segments.Add(new SvgPathSegment(SvgVerb.QuadTo, cx, cy, x, y));
                    lastQx = cx; lastQy = cy; curX = x; curY = y;
                    break;
                }

                case 'T':
                {
                    if (!t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    if (rel) { x += curX; y += curY; }
                    bool wasQuad = char.ToUpperInvariant(prevCmd) is 'Q' or 'T';
                    double cx = wasQuad ? 2 * curX - lastQx : curX;
                    double cy = wasQuad ? 2 * curY - lastQy : curY;
                    segments.Add(new SvgPathSegment(SvgVerb.QuadTo, cx, cy, x, y));
                    lastQx = cx; lastQy = cy; curX = x; curY = y;
                    break;
                }

                case 'A':
                {
                    if (!t.TryReadNumber(out double rx) || !t.TryReadNumber(out double ry)
                        || !t.TryReadNumber(out double xAxisRot)
                        || !t.TryReadFlag(out bool largeArc) || !t.TryReadFlag(out bool sweep)
                        || !t.TryReadPair(out double x, out double y)) { t.Stop(); break; }
                    if (rel) { x += curX; y += curY; }
                    AppendArc(segments, curX, curY, rx, ry, xAxisRot, largeArc, sweep, x, y);
                    curX = x; curY = y;
                    break;
                }

                case 'Z':
                {
                    segments.Add(new SvgPathSegment(SvgVerb.Close));
                    curX = startX; curY = startY;
                    break;
                }

                default:
                    t.Stop();
                    break;
            }

            prevCmd = cmd;
        }

        return segments;
    }

    /// <summary>
    /// Appends cubic Beziers approximating an elliptical arc from (x0,y0) to (x,y), using the SVG
    /// endpoint-to-centre parameterisation (F.6 of the SVG spec) and splitting the swept angle into
    /// quarter-circle-or-smaller cubic segments.
    /// </summary>
    private static void AppendArc(List<SvgPathSegment> segments, double x0, double y0,
        double rx, double ry, double xAxisRotDeg, bool largeArc, bool sweep, double x, double y)
    {
        if (rx == 0 || ry == 0 || (x0 == x && y0 == y))
        {
            segments.Add(new SvgPathSegment(SvgVerb.LineTo, x, y));
            return;
        }

        rx = Math.Abs(rx);
        ry = Math.Abs(ry);
        double phi = xAxisRotDeg * Math.PI / 180.0;
        double cosPhi = Math.Cos(phi);
        double sinPhi = Math.Sin(phi);

        // Step 1: compute (x1', y1').
        double dx = (x0 - x) / 2.0;
        double dy = (y0 - y) / 2.0;
        double x1p = cosPhi * dx + sinPhi * dy;
        double y1p = -sinPhi * dx + cosPhi * dy;

        // Correct out-of-range radii.
        double lambda = x1p * x1p / (rx * rx) + y1p * y1p / (ry * ry);
        if (lambda > 1)
        {
            double s = Math.Sqrt(lambda);
            rx *= s;
            ry *= s;
        }

        // Step 2: compute (cx', cy').
        double rx2 = rx * rx, ry2 = ry * ry;
        double x1p2 = x1p * x1p, y1p2 = y1p * y1p;
        double num = rx2 * ry2 - rx2 * y1p2 - ry2 * x1p2;
        double den = rx2 * y1p2 + ry2 * x1p2;
        double factor = den == 0 ? 0 : Math.Sqrt(Math.Max(0, num / den));
        if (largeArc == sweep)
        {
            factor = -factor;
        }

        double cxp = factor * rx * y1p / ry;
        double cyp = -factor * ry * x1p / rx;

        // Step 3: compute (cx, cy).
        double cx = cosPhi * cxp - sinPhi * cyp + (x0 + x) / 2.0;
        double cy = sinPhi * cxp + cosPhi * cyp + (y0 + y) / 2.0;

        // Step 4: compute the start angle and the angular sweep.
        double theta1 = Angle(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry);
        double dtheta = Angle((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry);
        if (!sweep && dtheta > 0)
        {
            dtheta -= 2 * Math.PI;
        }
        else if (sweep && dtheta < 0)
        {
            dtheta += 2 * Math.PI;
        }

        int n = (int)Math.Ceiling(Math.Abs(dtheta) / (Math.PI / 2));
        n = Math.Max(1, n);
        double delta = dtheta / n;
        double tVal = 8.0 / 3.0 * Math.Sin(delta / 4) * Math.Sin(delta / 4) / Math.Sin(delta / 2);

        double theta = theta1;
        for (int i = 0; i < n; i++)
        {
            double cosT1 = Math.Cos(theta);
            double sinT1 = Math.Sin(theta);
            double theta2 = theta + delta;
            double cosT2 = Math.Cos(theta2);
            double sinT2 = Math.Sin(theta2);

            // Endpoint and control points on the unit circle, scaled by radii and rotated.
            var (e1x, e1y) = MapEllipse(cx, cy, rx, ry, cosPhi, sinPhi, cosT1, sinT1);
            var (e2x, e2y) = MapEllipse(cx, cy, rx, ry, cosPhi, sinPhi, cosT2, sinT2);
            var (d1x, d1y) = MapEllipseVector(rx, ry, cosPhi, sinPhi, -sinT1, cosT1);
            var (d2x, d2y) = MapEllipseVector(rx, ry, cosPhi, sinPhi, -sinT2, cosT2);

            double c1x = e1x + tVal * d1x;
            double c1y = e1y + tVal * d1y;
            double c2x = e2x - tVal * d2x;
            double c2y = e2y - tVal * d2y;
            segments.Add(new SvgPathSegment(SvgVerb.CubicTo, c1x, c1y, c2x, c2y, e2x, e2y));
            theta = theta2;
        }
    }

    private static (double X, double Y) MapEllipse(double cx, double cy, double rx, double ry,
        double cosPhi, double sinPhi, double cosT, double sinT)
    {
        double ex = rx * cosT;
        double ey = ry * sinT;
        return (cx + cosPhi * ex - sinPhi * ey, cy + sinPhi * ex + cosPhi * ey);
    }

    private static (double X, double Y) MapEllipseVector(double rx, double ry,
        double cosPhi, double sinPhi, double dx, double dy)
    {
        double ex = rx * dx;
        double ey = ry * dy;
        return (cosPhi * ex - sinPhi * ey, sinPhi * ex + cosPhi * ey);
    }

    private static double Angle(double ux, double uy, double vx, double vy)
    {
        double dot = ux * vx + uy * vy;
        double len = Math.Sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        double ang = Math.Acos(Math.Clamp(len == 0 ? 1 : dot / len, -1, 1));
        if (ux * vy - uy * vx < 0)
        {
            ang = -ang;
        }

        return ang;
    }

    /// <summary>A small scanner over a path's <c>d</c> string yielding commands, numbers and flags.</summary>
    private sealed class Tokenizer(string s)
    {
        private int pos;
        private bool stopped;

        public bool HasMore
        {
            get
            {
                SkipSeparators();
                return !stopped && pos < s.Length;
            }
        }

        public void Stop() => stopped = true;

        public char PeekCommand()
        {
            SkipSeparators();
            if (pos < s.Length && char.IsLetter(s[pos]) && char.ToUpperInvariant(s[pos]) != 'E')
            {
                return s[pos];
            }

            return '\0';
        }

        public char ReadCommand() => s[pos++];

        public bool TryReadPair(out double x, out double y)
        {
            y = 0;
            return TryReadNumber(out x) && TryReadNumber(out y);
        }

        public bool TryReadNumber(out double value)
        {
            value = 0;
            SkipSeparators();
            int start = pos;
            if (pos < s.Length && (s[pos] == '+' || s[pos] == '-'))
            {
                pos++;
            }

            bool dot = false, digits = false;
            while (pos < s.Length)
            {
                char c = s[pos];
                if (char.IsDigit(c))
                {
                    digits = true;
                    pos++;
                }
                else if (c == '.' && !dot)
                {
                    dot = true;
                    pos++;
                }
                else if ((c == 'e' || c == 'E') && digits)
                {
                    pos++;
                    if (pos < s.Length && (s[pos] == '+' || s[pos] == '-'))
                    {
                        pos++;
                    }
                }
                else
                {
                    break;
                }
            }

            if (!digits)
            {
                return false;
            }

            return double.TryParse(s.AsSpan(start, pos - start), NumberStyles.Float, CultureInfo.InvariantCulture, out value);
        }

        /// <summary>Reads an arc flag: a single '0' or '1' (which may abut the following number).</summary>
        public bool TryReadFlag(out bool flag)
        {
            flag = false;
            SkipSeparators();
            if (pos < s.Length && (s[pos] == '0' || s[pos] == '1'))
            {
                flag = s[pos] == '1';
                pos++;
                return true;
            }

            return false;
        }

        private void SkipSeparators()
        {
            while (pos < s.Length && (char.IsWhiteSpace(s[pos]) || s[pos] == ','))
            {
                pos++;
            }
        }
    }
}
