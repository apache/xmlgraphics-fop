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
/// A base context used during validation when the actual base values are still unknown but should
/// already be checked. The actual value returned is not important; what matters is that zero and
/// non-zero values can be distinguished.
/// <para>
/// Example: a table with the collapsing border model has no padding. The Table FO should be able to
/// check whether non-zero values (even percentages) have been specified.
/// </para>
/// <para>Port of <c>org.apache.fop.datatypes.ValidationPercentBaseContext</c>.</para>
/// </summary>
public sealed class ValidationPercentBaseContext : IPercentBaseContext
{
    private static readonly IPercentBaseContext PseudoContextForValidation = new ValidationPercentBaseContext();

    private ValidationPercentBaseContext()
    {
    }

    /// <summary>A base context for validation purposes. See the type description.</summary>
    public static IPercentBaseContext GetPseudoContext() => PseudoContextForValidation;

    /// <summary>Returns the value for the given length base.</summary>
    public int GetBaseLength(int lengthBase, object? fobj) =>
        // Simply return a dummy value which produces a non-zero value when a non-zero percentage
        // was specified.
        100000;
}
