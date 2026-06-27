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

namespace Fop.Hyphenation;

/// <summary>
/// Represents a hyphen. A 'full' hyphen is made of 3 parts: the pre-break text,
/// post-break text and no-break. If no line-break is generated at this position,
/// the no-break text is used, otherwise pre-break and post-break are used.
/// Typically pre-break is equal to the hyphen character and the others are empty.
/// However, this general scheme allows support for cases in some languages where
/// words change spelling if they are split across lines, like German's 'backen'
/// which hyphenates 'bak-ken'. (This comes from TeX.)
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.Hyphen</c>. The Java fields were
/// public and mutable; they are preserved as get/set auto-properties here.
/// </para>
/// </summary>
public class Hyphen
{
    /// <summary>Construct a hyphen with pre/no/post break strings.</summary>
    /// <param name="preBreak">pre break string.</param>
    /// <param name="noBreak">no break string.</param>
    /// <param name="postBreak">post break string.</param>
    public Hyphen(string? preBreak, string? noBreak, string? postBreak)
    {
        PreBreak = preBreak;
        NoBreak = noBreak;
        PostBreak = postBreak;
    }

    /// <summary>Construct a hyphen with only a pre break string.</summary>
    /// <param name="preBreak">pre break string.</param>
    public Hyphen(string? preBreak)
    {
        PreBreak = preBreak;
        NoBreak = null;
        PostBreak = null;
    }

    /// <summary>The pre-break string.</summary>
    public string? PreBreak { get; set; }

    /// <summary>The no-break string.</summary>
    public string? NoBreak { get; set; }

    /// <summary>The post-break string.</summary>
    public string? PostBreak { get; set; }

    /// <inheritdoc/>
    public override string ToString()
    {
        if (NoBreak == null
            && PostBreak == null
            && PreBreak != null
            && PreBreak == "-")
        {
            return "-";
        }

        var res = new StringBuilder("{");
        res.Append(PreBreak);
        res.Append("}{");
        res.Append(PostBreak);
        res.Append("}{");
        res.Append(NoBreak);
        res.Append('}');
        return res.ToString();
    }
}
