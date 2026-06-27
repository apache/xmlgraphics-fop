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

namespace Fop.Traits;

/// <summary>
/// Rule styles.
/// <para>
/// Port of <c>org.apache.fop.traits.RuleStyle</c>. The Java typesafe-enum singleton becomes a C#
/// <c>enum</c> whose underlying integer value is exactly the corresponding
/// <c>org.apache.fop.fo.Constants.EN_*</c> constant. Names and the
/// <c>"RuleStyle:" + name</c> string form live in <see cref="RuleStyleExtensions"/>.
/// </para>
/// </summary>
public enum RuleStyle
{
    /// <summary>rule-style: none (<c>EN_NONE</c>).</summary>
    None = 95,

    /// <summary>rule-style: dotted (<c>EN_DOTTED</c>).</summary>
    Dotted = 36,

    /// <summary>rule-style: dashed (<c>EN_DASHED</c>).</summary>
    Dashed = 31,

    /// <summary>rule-style: solid (<c>EN_SOLID</c>).</summary>
    Solid = 133,

    /// <summary>rule-style: double (<c>EN_DOUBLE</c>).</summary>
    Double = 37,

    /// <summary>rule-style: groove (<c>EN_GROOVE</c>).</summary>
    Groove = 55,

    /// <summary>rule-style: ridge (<c>EN_RIDGE</c>).</summary>
    Ridge = 119,
}

/// <summary>
/// Helpers mirroring the Java <c>RuleStyle</c> static methods and name/toString contract.
/// </summary>
public static class RuleStyleExtensions
{
    // Iteration order matches the Java STYLES array.
    private static readonly RuleStyle[] Styles =
    [
        RuleStyle.None, RuleStyle.Dotted, RuleStyle.Dashed,
        RuleStyle.Solid, RuleStyle.Double, RuleStyle.Groove, RuleStyle.Ridge,
    ];

    /// <summary>Returns the canonical (lower-case) name of the rule style.</summary>
    public static string GetName(this RuleStyle style) => style switch
    {
        RuleStyle.None => "none",
        RuleStyle.Dotted => "dotted",
        RuleStyle.Dashed => "dashed",
        RuleStyle.Solid => "solid",
        RuleStyle.Double => "double",
        RuleStyle.Groove => "groove",
        RuleStyle.Ridge => "ridge",
        _ => throw new ArgumentOutOfRangeException(nameof(style)),
    };

    /// <summary>Returns the enumeration value (one of the <c>EN_*</c> integers).</summary>
    public static int GetEnumValue(this RuleStyle style) => (int)style;

    /// <summary>
    /// Returns the Java-compatible string form, <c>"RuleStyle:" + name</c>.
    /// </summary>
    public static string ToDisplayString(this RuleStyle style) => "RuleStyle:" + style.GetName();

    /// <summary>
    /// Returns the enumeration object based on its name (case-insensitive).
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a legal rule style.</exception>
    public static RuleStyle ValueOf(string name)
    {
        foreach (RuleStyle style in Styles)
        {
            if (string.Equals(style.GetName(), name, StringComparison.OrdinalIgnoreCase))
            {
                return style;
            }
        }

        throw new ArgumentException("Illegal rule style: " + name);
    }

    /// <summary>
    /// Returns the enumeration object based on its enumeration (<c>EN_*</c>) value.
    /// </summary>
    /// <exception cref="ArgumentException">if the value is not a legal rule style.</exception>
    public static RuleStyle ValueOf(int enumValue)
    {
        foreach (RuleStyle style in Styles)
        {
            if (style.GetEnumValue() == enumValue)
            {
                return style;
            }
        }

        throw new ArgumentException("Illegal rule style: " + enumValue);
    }
}
