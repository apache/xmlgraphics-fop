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
/// Border styles.
/// <para>
/// Port of <c>org.apache.fop.traits.BorderStyle</c>. The Java typesafe-enum singleton becomes a C#
/// <c>enum</c> whose underlying integer value is exactly the corresponding
/// <c>org.apache.fop.fo.Constants.EN_*</c> constant. Names and the
/// <c>"BorderStyle:" + name</c> string form live in <see cref="BorderStyleExtensions"/>.
/// </para>
/// </summary>
public enum BorderStyle
{
    /// <summary>border-style: none (<c>EN_NONE</c>).</summary>
    None = 95,

    /// <summary>border-style: hidden (<c>EN_HIDDEN</c>).</summary>
    Hidden = 57,

    /// <summary>border-style: dotted (<c>EN_DOTTED</c>).</summary>
    Dotted = 36,

    /// <summary>border-style: dashed (<c>EN_DASHED</c>).</summary>
    Dashed = 31,

    /// <summary>border-style: solid (<c>EN_SOLID</c>).</summary>
    Solid = 133,

    /// <summary>border-style: double (<c>EN_DOUBLE</c>).</summary>
    Double = 37,

    /// <summary>border-style: groove (<c>EN_GROOVE</c>).</summary>
    Groove = 55,

    /// <summary>border-style: ridge (<c>EN_RIDGE</c>).</summary>
    Ridge = 119,

    /// <summary>border-style: inset (<c>EN_INSET</c>).</summary>
    Inset = 67,

    /// <summary>border-style: outset (<c>EN_OUTSET</c>).</summary>
    Outset = 101,
}

/// <summary>
/// Helpers mirroring the Java <c>BorderStyle</c> static methods and name/toString contract.
/// </summary>
public static class BorderStyleExtensions
{
    // Iteration order matches the Java STYLES array.
    private static readonly BorderStyle[] Styles =
    [
        BorderStyle.None, BorderStyle.Hidden, BorderStyle.Dotted, BorderStyle.Dashed,
        BorderStyle.Solid, BorderStyle.Double, BorderStyle.Groove, BorderStyle.Ridge,
        BorderStyle.Inset, BorderStyle.Outset,
    ];

    /// <summary>Returns the canonical (lower-case) name of the border style.</summary>
    public static string GetName(this BorderStyle style) => style switch
    {
        BorderStyle.None => "none",
        BorderStyle.Hidden => "hidden",
        BorderStyle.Dotted => "dotted",
        BorderStyle.Dashed => "dashed",
        BorderStyle.Solid => "solid",
        BorderStyle.Double => "double",
        BorderStyle.Groove => "groove",
        BorderStyle.Ridge => "ridge",
        BorderStyle.Inset => "inset",
        BorderStyle.Outset => "outset",
        _ => throw new ArgumentOutOfRangeException(nameof(style)),
    };

    /// <summary>Returns the enumeration value (one of the <c>EN_*</c> integers).</summary>
    public static int GetEnumValue(this BorderStyle style) => (int)style;

    /// <summary>
    /// Returns the Java-compatible string form, <c>"BorderStyle:" + name</c>.
    /// </summary>
    public static string ToDisplayString(this BorderStyle style) => "BorderStyle:" + style.GetName();

    /// <summary>
    /// Returns the enumeration object based on its name (case-insensitive).
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a legal border style.</exception>
    public static BorderStyle ValueOf(string name)
    {
        foreach (BorderStyle style in Styles)
        {
            if (string.Equals(style.GetName(), name, StringComparison.OrdinalIgnoreCase))
            {
                return style;
            }
        }

        throw new ArgumentException("Illegal border style: " + name);
    }

    /// <summary>
    /// Returns the enumeration object based on its enumeration (<c>EN_*</c>) value.
    /// </summary>
    /// <exception cref="ArgumentException">if the value is not a legal border style.</exception>
    public static BorderStyle ValueOf(int enumValue)
    {
        foreach (BorderStyle style in Styles)
        {
            if (style.GetEnumValue() == enumValue)
            {
                return style;
            }
        }

        throw new ArgumentException("Illegal border style: " + enumValue);
    }
}
