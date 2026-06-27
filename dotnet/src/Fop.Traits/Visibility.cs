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
/// The visibility trait.
/// <para>
/// Port of <c>org.apache.fop.traits.Visibility</c>. The Java typesafe-enum singleton becomes a C#
/// <c>enum</c> whose underlying integer value is exactly the corresponding
/// <c>org.apache.fop.fo.Constants.EN_*</c> constant. Names live in
/// <see cref="VisibilityExtensions"/>.
/// </para>
/// </summary>
public enum Visibility
{
    /// <summary>visibility: visible (<c>EN_VISIBLE</c>).</summary>
    Visible = 159,

    /// <summary>visibility: hidden (<c>EN_HIDDEN</c>).</summary>
    Hidden = 57,

    /// <summary>visibility: collapse (<c>EN_COLLAPSE</c>).</summary>
    Collapse = 26,
}

/// <summary>
/// Helpers mirroring the Java <c>Visibility</c> static methods and name contract.
/// </summary>
/// <remarks>
/// The Java class only defines <c>valueOf(String)</c> (no integer overload), so this port
/// faithfully omits an integer <c>ValueOf</c> too.
/// </remarks>
public static class VisibilityExtensions
{
    // Iteration order matches the Java VISIBILITIES array {VISIBLE, HIDDEN, COLLAPSE}.
    private static readonly Visibility[] Visibilities =
        [Visibility.Visible, Visibility.Hidden, Visibility.Collapse];

    /// <summary>Returns the canonical (lower-case) name of the visibility value.</summary>
    public static string GetName(this Visibility visibility) => visibility switch
    {
        Visibility.Visible => "visible",
        Visibility.Hidden => "hidden",
        Visibility.Collapse => "collapse",
        _ => throw new ArgumentOutOfRangeException(nameof(visibility)),
    };

    /// <summary>Returns the enumeration value (one of the <c>EN_*</c> integers).</summary>
    public static int GetEnumValue(this Visibility visibility) => (int)visibility;

    /// <summary>
    /// Returns the enumeration object based on its name (case-insensitive).
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a legal visibility value.</exception>
    public static Visibility ValueOf(string name)
    {
        foreach (Visibility v in Visibilities)
        {
            if (string.Equals(v.GetName(), name, StringComparison.OrdinalIgnoreCase))
            {
                return v;
            }
        }

        throw new ArgumentException("Illegal visibility value: " + name);
    }
}
