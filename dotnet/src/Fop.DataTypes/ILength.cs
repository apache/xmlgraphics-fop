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
/// A length quantity in XSL. Values are expressed in 1/1000ths of a point (millipoints).
/// <para>Port of <c>org.apache.fop.datatypes.Length</c>.</para>
/// </summary>
public interface ILength : INumeric
{
    /// <summary>Returns the length in 1/1000ths of a point (millipoints).</summary>
    new int GetValue();

    /// <summary>Returns the length in 1/1000ths of a point (millipoints).</summary>
    /// <param name="context">The context for the length calculation (for percentage-based lengths).</param>
    new int GetValue(IPercentBaseContext? context);
}
