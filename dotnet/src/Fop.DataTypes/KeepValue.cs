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

namespace Fop.DataTypes;

/// <summary>
/// Stores the different types of keeps in a single convenient format.
/// <para>Port of <c>org.apache.fop.datatypes.KeepValue</c>.</para>
/// </summary>
public class KeepValue
{
    /// <summary>Constant for keep-with-always.</summary>
    public const string KeepWithAlways = "KEEP_WITH_ALWAYS";

    /// <summary>Constant for automatic keep-with computation.</summary>
    public const string KeepWithAuto = "KEEP_WITH_AUTO";

    /// <summary>Constant for a user-settable keep-with value.</summary>
    public const string KeepWithValue = "KEEP_WITH_VALUE";

    /// <summary>Creates a new keep value.</summary>
    /// <param name="type">One of <see cref="KeepWithAlways"/>, <see cref="KeepWithAuto"/>, or <see cref="KeepWithValue"/>.</param>
    /// <param name="val">Keep-with value to use (used only by <see cref="KeepWithValue"/>).</param>
    public KeepValue(string type, int val)
    {
        Type = type;
        Value = val;
    }

    /// <summary>The keep-with value.</summary>
    public int Value { get; }

    /// <summary>The descriptive type.</summary>
    public string Type { get; }

    /// <summary>Returns a string representation of this keep value (its type).</summary>
    public override string ToString() => Type;
}
