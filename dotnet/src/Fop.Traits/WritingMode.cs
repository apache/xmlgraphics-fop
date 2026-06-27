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
/// The writing-mode trait.
/// <para>
/// Port of <c>org.apache.fop.traits.WritingMode</c>. The Java typesafe-enum singleton becomes a C#
/// <c>enum</c> whose underlying integer value is exactly the corresponding
/// <c>org.apache.fop.fo.Constants.EN_*</c> constant. Canonical names ("lr-tb", "rl-tb", "tb-lr",
/// "tb-rl") and behaviour live in <see cref="WritingModeExtensions"/>.
/// </para>
/// <para>
/// The Java method <c>assignWritingModeTraits(WritingModeTraitsSetter, boolean)</c> is intentionally
/// not ported here: it depends on <c>WritingModeTraitsSetter</c>, which is out of scope for this
/// slice. It can be added once that type is ported.
/// </para>
/// </summary>
public enum WritingMode
{
    /// <summary>writing mode: lr-tb (<c>EN_LR_TB</c>).</summary>
    LrTb = 79,

    /// <summary>writing mode: rl-tb (<c>EN_RL_TB</c>).</summary>
    RlTb = 121,

    /// <summary>writing mode: tb-lr (<c>EN_TB_LR</c>).</summary>
    TbLr = 203,

    /// <summary>writing mode: tb-rl (<c>EN_TB_RL</c>).</summary>
    TbRl = 140,
}

/// <summary>
/// Helpers mirroring the Java <c>WritingMode</c> instance/static methods.
/// </summary>
public static class WritingModeExtensions
{
    // Iteration order matches the Java WRITING_MODES array {LR_TB, RL_TB, TB_LR, TB_RL}.
    private static readonly WritingMode[] WritingModes =
        [WritingMode.LrTb, WritingMode.RlTb, WritingMode.TbLr, WritingMode.TbRl];

    /// <summary>Returns the canonical (lower-case) name of the writing mode.</summary>
    public static string GetName(this WritingMode writingMode) => writingMode switch
    {
        WritingMode.LrTb => "lr-tb",
        WritingMode.RlTb => "rl-tb",
        WritingMode.TbLr => "tb-lr",
        WritingMode.TbRl => "tb-rl",
        _ => throw new ArgumentOutOfRangeException(nameof(writingMode)),
    };

    /// <summary>Returns the enumeration value (one of the <c>EN_*</c> integers).</summary>
    public static int GetEnumValue(this WritingMode writingMode) => (int)writingMode;

    /// <summary>Determines whether the writing mode is horizontal.</summary>
    public static bool IsHorizontal(this WritingMode writingMode) => writingMode switch
    {
        WritingMode.LrTb or WritingMode.RlTb => true,
        WritingMode.TbLr or WritingMode.TbRl => false,
        _ => throw new ArgumentOutOfRangeException(nameof(writingMode)),
    };

    /// <summary>Determines whether the writing mode is vertical.</summary>
    public static bool IsVertical(this WritingMode writingMode) => !writingMode.IsHorizontal();

    /// <summary>
    /// Returns the enumeration object based on its name (case-insensitive).
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a legal writing mode.</exception>
    public static WritingMode ValueOf(string name)
    {
        foreach (WritingMode writingMode in WritingModes)
        {
            if (string.Equals(writingMode.GetName(), name, StringComparison.OrdinalIgnoreCase))
            {
                return writingMode;
            }
        }

        throw new ArgumentException("Illegal writing mode: " + name);
    }

    /// <summary>
    /// Returns the enumeration object based on its enumeration (<c>EN_*</c>) value.
    /// </summary>
    /// <exception cref="ArgumentException">if the value is not a legal writing mode.</exception>
    public static WritingMode ValueOf(int enumValue)
    {
        foreach (WritingMode writingMode in WritingModes)
        {
            if (writingMode.GetEnumValue() == enumValue)
            {
                return writingMode;
            }
        }

        throw new ArgumentException("Illegal writing mode: " + enumValue);
    }
}
