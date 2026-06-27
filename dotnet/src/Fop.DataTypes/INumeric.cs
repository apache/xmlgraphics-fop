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
/// An interface for classes that can participate in numeric operations. All numeric operations
/// (+, -, *, ...) are expressed in terms of this interface. Numerics have a value and a dimension,
/// and can be either absolute or relative; relative numerics must be resolved against a base value
/// before the value can be used.
/// <para>
/// To support relative numerics internally in the expression parser and during evaluation,
/// <see cref="IsAbsolute"/> returns <c>true</c> for absolute numerics and <c>false</c> for relative
/// ones.
/// </para>
/// <para>Port of <c>org.apache.fop.datatypes.Numeric</c>.</para>
/// </summary>
public interface INumeric
{
    /// <summary>Returns the computed value of this numeric.</summary>
    double GetNumericValue();

    /// <summary>Returns the computed value of this numeric.</summary>
    /// <param name="context">The context for the length calculation (for percentage-based lengths).</param>
    double GetNumericValue(IPercentBaseContext? context);

    /// <summary>
    /// Returns the dimension of this numeric. Plain numbers have a dimension of 0 and lengths have a
    /// dimension of 1. Other dimensions can occur as a result of multiplications and divisions.
    /// </summary>
    int Dimension { get; }

    /// <summary>
    /// Returns <c>true</c> if the numeric is an absolute value. Relative values are percentages and
    /// table-column-units; all other numerics are absolute.
    /// </summary>
    bool IsAbsolute { get; }

    /// <summary>Returns the value of this numeric as an int.</summary>
    int GetValue();

    /// <summary>Returns the value of this numeric as an int.</summary>
    /// <param name="context">The context for the length calculation (for percentage-based lengths).</param>
    int GetValue(IPercentBaseContext? context);

    /// <summary>Returns the enum value that is stored in this numeric.</summary>
    int Enum { get; }
}
