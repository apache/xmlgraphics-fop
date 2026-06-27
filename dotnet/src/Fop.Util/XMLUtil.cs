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

using System.Drawing;
using System.Globalization;
using System.Text;
using System.Xml.Linq;

namespace Fop.Util;

/// <summary>
/// A collection of utility methods for XML handling.
/// <para>
/// Port of <c>org.apache.fop.util.XMLUtil</c> (and the constants of its
/// <c>XMLConstants</c> interface). The Java original is coupled to SAX
/// (<c>org.xml.sax.Attributes</c> / <c>AttributesImpl</c>); the pure helpers are ported here
/// against <see cref="System.Xml.Linq"/> types, and the SAX-bound <c>addAttribute</c> overloads
/// are left for the renderer port (see the TODO below).
/// </para>
/// </summary>
public static class XMLUtil
{
    /// <summary>The "CDATA" attribute-type constant.</summary>
    public const string Cdata = "CDATA";

    /// <summary>The XML namespace prefix.</summary>
    public const string XmlPrefix = "xml";

    /// <summary>The XML namespace URI.</summary>
    public const string XmlNamespace = "http://www.w3.org/XML/1998/namespace";

    /// <summary>The XMLNS namespace prefix.</summary>
    public const string XmlnsPrefix = "xmlns";

    /// <summary>The XMLNS namespace URI.</summary>
    public const string XmlnsNamespaceUri = "http://www.w3.org/2000/xmlns/";

    /// <summary>The XLink namespace prefix.</summary>
    public const string XlinkPrefix = "xlink";

    /// <summary>The XLink namespace URI.</summary>
    public const string XlinkNamespace = "http://www.w3.org/1999/xlink";

    /// <summary>The <c>xml:space</c> attribute as a qualified name.</summary>
    public static QName XmlSpace { get; } = new QName(XmlNamespace, XmlPrefix, "space");

    /// <summary>The <c>xlink:href</c> attribute as a qualified name.</summary>
    public static QName XlinkHref { get; } = new QName(XlinkNamespace, XlinkPrefix, "href");

    /// <summary>
    /// Returns an attribute value as a boolean value. Parsing matches Java's
    /// <c>Boolean.valueOf</c>: the value is <c>true</c> only for the case-insensitive token
    /// "true", and any other non-null value yields <c>false</c>.
    /// </summary>
    /// <param name="element">the element carrying the attributes.</param>
    /// <param name="name">the name of the attribute.</param>
    /// <param name="defaultValue">the value to return if the attribute is not present.</param>
    public static bool GetAttributeAsBoolean(XElement element, string name, bool defaultValue)
    {
        string? s = GetValue(element, name);
        return s is null ? defaultValue : string.Equals(s, "true", StringComparison.OrdinalIgnoreCase);
    }

    /// <summary>
    /// Returns an attribute value as an int value, or <paramref name="defaultValue"/> if the
    /// attribute is not present.
    /// </summary>
    public static int GetAttributeAsInt(XElement element, string name, int defaultValue)
    {
        string? s = GetValue(element, name);
        return s is null ? defaultValue : int.Parse(s, CultureInfo.InvariantCulture);
    }

    /// <summary>
    /// Returns an attribute value as an int value.
    /// </summary>
    /// <exception cref="FormatException">if the attribute is missing.</exception>
    public static int GetAttributeAsInt(XElement element, string name)
    {
        string? s = GetValue(element, name);
        if (s is null)
        {
            // Java threw SAXException here; there is no SAX layer in this port.
            throw new FormatException($"Attribute '{name}' is missing");
        }

        return int.Parse(s, CultureInfo.InvariantCulture);
    }

    /// <summary>
    /// Returns an attribute value as a nullable int, or <c>null</c> if the attribute is missing.
    /// </summary>
    public static int? GetAttributeAsInteger(XElement element, string name)
    {
        string? s = GetValue(element, name);
        return s is null ? null : int.Parse(s, CultureInfo.InvariantCulture);
    }

    /// <summary>
    /// Returns an attribute value as a <see cref="RectangleF"/>. The string value is expected as
    /// 4 double-precision numbers separated by whitespace (x, y, width, height).
    /// </summary>
    public static RectangleF GetAttributeAsRectangle2D(XElement element, string name)
    {
        string s = (GetValue(element, name) ?? throw new ArgumentException(
            $"Attribute '{name}' is missing")).Trim();
        double[]? values = ConversionUtils.ToDoubleArray(s, "\\s");
        if (values is null || values.Length != 4)
        {
            throw new ArgumentException("Rectangle must consist of 4 double values!");
        }

        return new RectangleF((float)values[0], (float)values[1], (float)values[2], (float)values[3]);
    }

    /// <summary>
    /// Returns an attribute value as a <see cref="Rectangle"/>. The string value is expected as
    /// 4 integer numbers separated by whitespace (x, y, width, height). Returns <c>null</c> if
    /// the attribute is missing.
    /// </summary>
    public static Rectangle? GetAttributeAsRectangle(XElement element, string name)
    {
        string? s = GetValue(element, name);
        if (s is null)
        {
            return null;
        }

        int[]? values = ConversionUtils.ToIntArray(s.Trim(), "\\s");
        if (values is null || values.Length != 4)
        {
            throw new ArgumentException("Rectangle must consist of 4 int values!");
        }

        return new Rectangle(values[0], values[1], values[2], values[3]);
    }

    /// <summary>
    /// Returns an attribute value as an int array (whitespace-separated), or <c>null</c> if the
    /// attribute is missing.
    /// </summary>
    public static int[]? GetAttributeAsIntArray(XElement element, string name)
    {
        string? s = GetValue(element, name);
        return s is null ? null : ConversionUtils.ToIntArray(s.Trim(), "\\s");
    }

    // TODO: The Java AddAttribute(AttributesImpl, ...) overloads mutate a SAX attribute list.
    // They will be ported alongside the intermediate-format/renderer SAX handling; with the
    // System.Xml.Linq model used here, a caller would instead add an XAttribute to an XElement.

    /// <summary>
    /// Encodes a glyph position-adjustments array as a string, where the encoded value adheres to
    /// the syntax: <c>count ( 'Z' repeat | number )</c>, tokens separated by whitespace, except
    /// that <c>'Z' repeat</c> is a single token. <c>'Z' repeat</c> encodes repeated zeroes.
    /// </summary>
    /// <param name="dp">the adjustments array.</param>
    /// <param name="paCount">the number of entries to encode from the adjustments array.</param>
    public static string EncodePositionAdjustments(int[]?[] dp, int paCount)
    {
        ArgumentNullException.ThrowIfNull(dp);
        var sb = new StringBuilder();
        int na = paCount;
        int nz = 0;
        sb.Append(na);
        for (int i = 0; i < na; i++)
        {
            int[]? pa = dp[i];
            if (pa is not null)
            {
                for (int k = 0; k < 4; k++)
                {
                    int a = pa[k];
                    if (a != 0)
                    {
                        EncodeNextAdjustment(sb, nz, a);
                        nz = 0;
                    }
                    else
                    {
                        nz++;
                    }
                }
            }
            else
            {
                nz += 4;
            }
        }

        EncodeNextAdjustment(sb, nz, 0);
        return sb.ToString();
    }

    /// <summary>
    /// Encodes a glyph position-adjustments array as a string (all entries).
    /// </summary>
    /// <param name="dp">the adjustments array.</param>
    public static string EncodePositionAdjustments(int[]?[] dp)
    {
        ArgumentNullException.ThrowIfNull(dp);
        return EncodePositionAdjustments(dp, dp.Length);
    }

    /// <summary>
    /// Decodes a string as a glyph position-adjustments array, where the string adheres to the
    /// syntax produced by <see cref="EncodePositionAdjustments(int[][], int)"/>.
    /// </summary>
    /// <param name="value">the encoded value (may be <c>null</c>).</param>
    /// <returns>the position-adjustments array, or <c>null</c> if <paramref name="value"/> is
    /// <c>null</c>.</returns>
    public static int[][]? DecodePositionAdjustments(string? value)
    {
        int[][]? dp = null;
        if (value is not null)
        {
            string[] sa = System.Text.RegularExpressions.Regex.Split(value, "\\s");
            if (sa.Length > 0)
            {
                int na = int.Parse(sa[0], CultureInfo.InvariantCulture);
                dp = new int[na][];
                for (int j = 0; j < na; j++)
                {
                    dp[j] = new int[4];
                }

                for (int i = 1, n = sa.Length, k = 0; i < n; i++)
                {
                    string s = sa[i];
                    if (s[0] == 'Z')
                    {
                        int nz = int.Parse(s[1..], CultureInfo.InvariantCulture);
                        k += nz;
                    }
                    else
                    {
                        dp[k / 4][k % 4] = int.Parse(s, CultureInfo.InvariantCulture);
                        k += 1;
                    }
                }
            }
        }

        return dp;
    }

    /// <summary>
    /// Returns an attribute value as a glyph position-adjustments array, or <c>null</c> if the
    /// attribute is missing.
    /// </summary>
    public static int[][]? GetAttributeAsPositionAdjustments(XElement element, string name)
    {
        string? s = GetValue(element, name);
        return s is null ? null : DecodePositionAdjustments(s.Trim());
    }

    /// <summary>
    /// Escapes '&lt;', '&gt;' and '&amp;' using numeric character references (lower-case hex,
    /// matching Java's <c>Integer.toString(c, 16)</c>).
    /// </summary>
    /// <param name="unescaped">the string to escape.</param>
    /// <returns>the escaped string.</returns>
    public static string Escape(string unescaped)
    {
        ArgumentNullException.ThrowIfNull(unescaped);
        int needsEscape = 0;
        foreach (char c in unescaped)
        {
            if (c is '<' or '>' or '&')
            {
                ++needsEscape;
            }
        }

        if (needsEscape > 0)
        {
            var sb = new StringBuilder(unescaped.Length + (6 * needsEscape));
            foreach (char c in unescaped)
            {
                if (c is '<' or '>' or '&')
                {
                    sb.Append("&#x");
                    sb.Append(Convert.ToString(c, 16));
                    sb.Append(';');
                }
                else
                {
                    sb.Append(c);
                }
            }

            return sb.ToString();
        }

        return unescaped;
    }

    private static string? GetValue(XElement element, string name)
    {
        ArgumentNullException.ThrowIfNull(element);
        ArgumentNullException.ThrowIfNull(name);
        return element.Attribute(name)?.Value;
    }

    private static void EncodeNextAdjustment(StringBuilder sb, int nz, int a)
    {
        EncodeZeroes(sb, nz);
        EncodeAdjustment(sb, a);
    }

    private static void EncodeZeroes(StringBuilder sb, int nz)
    {
        if (nz > 0)
        {
            sb.Append(' ');
            if (nz == 1)
            {
                sb.Append('0');
            }
            else
            {
                sb.Append('Z');
                sb.Append(nz);
            }
        }
    }

    private static void EncodeAdjustment(StringBuilder sb, int a)
    {
        if (a != 0)
        {
            sb.Append(' ');
            sb.Append(a);
        }
    }
}
