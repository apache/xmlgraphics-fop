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

namespace Fop.Fonts;

/// <summary>
/// Enumerates all supported font types.
/// <para>
/// Port of <c>org.apache.fop.fonts.FontType</c>. The Java typesafe-enum singleton (each constant is a
/// <c>new FontType(name, value)</c>) becomes a C# <c>enum</c> whose underlying integer is exactly the
/// Java <c>value</c>. The canonical names (e.g. "MMType1", "CIDFontType0") and the <c>byName</c>/
/// <c>byValue</c> lookups live in <see cref="FontTypeExtensions"/>.
/// </para>
/// </summary>
public enum FontType
{
    /// <summary>Collective identifier for "other" font types (value 0).</summary>
    Other = 0,

    /// <summary>Adobe Type 0 fonts (composite font) (value 1).</summary>
    Type0 = 1,

    /// <summary>Adobe Type 1 fonts (value 2).</summary>
    Type1 = 2,

    /// <summary>Adobe Multiple Master Type 1 fonts (value 3).</summary>
    MMType1 = 3,

    /// <summary>Adobe Type 3 fonts ("user-defined" fonts) (value 4).</summary>
    Type3 = 4,

    /// <summary>TrueType fonts (value 5).</summary>
    TrueType = 5,

    /// <summary>Type 1C (CFF) fonts (value 6).</summary>
    Type1C = 6,

    /// <summary>CID-keyed Type 0 fonts (value 7).</summary>
    CIDType0 = 7,
}

/// <summary>
/// Helpers mirroring the Java <c>FontType</c> instance/static methods.
/// </summary>
public static class FontTypeExtensions
{
    /// <summary>
    /// Returns the canonical name of the font type (matches the Java constructor <c>name</c>
    /// argument and Java <c>getName()</c>/<c>toString()</c>).
    /// </summary>
    public static string GetName(this FontType fontType) => fontType switch
    {
        FontType.Other => "Other",
        FontType.Type0 => "Type0",
        FontType.Type1 => "Type1",
        FontType.MMType1 => "MMType1",
        FontType.Type3 => "Type3",
        FontType.TrueType => "TrueType",
        FontType.Type1C => "Type1C",
        FontType.CIDType0 => "CIDFontType0",
        _ => throw new ArgumentOutOfRangeException(nameof(fontType)),
    };

    /// <summary>Returns the integer value of the font type (Java <c>getValue()</c>).</summary>
    public static int GetValue(this FontType fontType) => (int)fontType;

    /// <summary>
    /// Returns the font type by name (case-insensitive).
    /// <para>
    /// Behaviour matches Java <c>FontType.byName</c>: only Other, Type0, Type1, MMType1, Type3 and
    /// TrueType are recognised. The Type1C and CIDFontType0 constants exist but are intentionally
    /// not looked up by this method, so passing their names throws (preserving the Java quirk).
    /// </para>
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a recognised font type.</exception>
    public static FontType ByName(string name) => name switch
    {
        _ when EqualsIgnoreCase(name, FontType.Other.GetName()) => FontType.Other,
        _ when EqualsIgnoreCase(name, FontType.Type0.GetName()) => FontType.Type0,
        _ when EqualsIgnoreCase(name, FontType.Type1.GetName()) => FontType.Type1,
        _ when EqualsIgnoreCase(name, FontType.MMType1.GetName()) => FontType.MMType1,
        _ when EqualsIgnoreCase(name, FontType.Type3.GetName()) => FontType.Type3,
        _ when EqualsIgnoreCase(name, FontType.TrueType.GetName()) => FontType.TrueType,
        _ => throw new ArgumentException("Invalid font type: " + name),
    };

    /// <summary>
    /// Returns the font type by value.
    /// <para>
    /// Behaviour matches Java <c>FontType.byValue</c>: only values 0..5 (Other..TrueType) are
    /// recognised; values 6 (Type1C) and 7 (CIDFontType0) throw, preserving the Java quirk.
    /// </para>
    /// </summary>
    /// <exception cref="ArgumentException">if the value is not a recognised font type.</exception>
    public static FontType ByValue(int value) => value switch
    {
        (int)FontType.Other => FontType.Other,
        (int)FontType.Type0 => FontType.Type0,
        (int)FontType.Type1 => FontType.Type1,
        (int)FontType.MMType1 => FontType.MMType1,
        (int)FontType.Type3 => FontType.Type3,
        (int)FontType.TrueType => FontType.TrueType,
        _ => throw new ArgumentException("Invalid font type: " + value),
    };

    private static bool EqualsIgnoreCase(string a, string b) =>
        string.Equals(a, b, StringComparison.OrdinalIgnoreCase);
}
