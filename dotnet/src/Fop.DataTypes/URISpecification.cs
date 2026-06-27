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

using System.Text;

namespace Fop.DataTypes;

/// <summary>
/// Methods to deal with the &lt;uri-specification&gt; datatype from XSL-FO.
/// <para>Port of <c>org.apache.fop.datatypes.URISpecification</c>.</para>
/// </summary>
public static class URISpecification
{
    private const string Punct = ",;:$&+=";
    private const string Reserved = Punct + "?/[]@";

    private static readonly char[] HexDigits =
    {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    };

    /// <summary>Gets the URL string from a wrapped URL.</summary>
    /// <param name="href">The input wrapped URL.</param>
    /// <returns>The raw URL.</returns>
    public static string GetURL(string href)
    {
        // According to section 5.11 a <uri-specification> is: "url(" + URI + ")"
        // according to 7.28.7 a <uri-specification> is: URI. So handle both.
        href = href.Trim();
        if (href.StartsWith("url(", StringComparison.Ordinal) && href.IndexOf(')') != -1)
        {
            href = href[4..href.LastIndexOf(')')].Trim();
            if (href.StartsWith('\'') && href.EndsWith('\''))
            {
                href = href[1..^1];
            }
            else if (href.StartsWith('"') && href.EndsWith('"'))
            {
                href = href[1..^1];
            }
        }
        else
        {
            // warn
        }

        return href;
    }

    /// <summary>
    /// Escapes any illegal URI character in a given URI; for example, it escapes a space to "%20".
    /// <para>
    /// Note: this method does not "parse" the URI and therefore does not treat the individual
    /// components (user-info, path, query, etc.) individually.
    /// </para>
    /// </summary>
    /// <param name="uri">The URI to inspect.</param>
    /// <returns>The escaped URI.</returns>
    public static string EscapeURI(string uri)
    {
        uri = GetURL(uri);
        var sb = new StringBuilder();
        for (int i = 0, c = uri.Length; i < c; i++)
        {
            char ch = uri[i];
            if (ch == '%')
            {
                if (i < c - 3 && IsHexDigit(uri[i + 1]) && IsHexDigit(uri[i + 2]))
                {
                    sb.Append(ch);
                    continue;
                }
            }

            if (IsReserved(ch) || IsUnreserved(ch))
            {
                // Note: this may not be accurate for some very special cases.
                sb.Append(ch);
            }
            else
            {
                byte[] utf8 = Encoding.UTF8.GetBytes(ch.ToString());
                foreach (byte anUtf8 in utf8)
                {
                    AppendEscape(sb, anUtf8);
                }
            }
        }

        return sb.ToString();
    }

    private static bool IsDigit(char ch) => ch is >= '0' and <= '9';

    private static bool IsAlpha(char ch) => ch is (>= 'A' and <= 'Z') or (>= 'a' and <= 'z');

    private static bool IsHexDigit(char ch) =>
        ch is (>= '0' and <= '9') or (>= 'A' and <= 'F') or (>= 'a' and <= 'f');

    private static bool IsReserved(char ch) =>
        // '#' is not a reserved character but is used for the fragment.
        Reserved.IndexOf(ch) >= 0 || ch == '#';

    private static bool IsUnreserved(char ch) =>
        // The set after digits/letters are the remaining unreserved characters.
        IsDigit(ch) || IsAlpha(ch) || "_-!.~'()*".IndexOf(ch) >= 0;

    private static void AppendEscape(StringBuilder sb, byte b) =>
        sb.Append('%').Append(HexDigits[(b >> 4) & 0x0f]).Append(HexDigits[b & 0x0f]);
}
