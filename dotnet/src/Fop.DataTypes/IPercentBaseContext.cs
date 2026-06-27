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
/// Used by the layout managers to provide relevant information back to the property
/// percentage resolution logic: the percentage-based property <c>GetValue()</c> functions
/// expect an object implementing this interface as an argument.
/// <para>Port of <c>org.apache.fop.datatypes.PercentBaseContext</c>.</para>
/// </summary>
public interface IPercentBaseContext
{
    /// <summary>
    /// Returns the base length for the given length base. The length base should be one of the
    /// constants defined in <see cref="LengthBase"/>.
    /// </summary>
    /// <param name="lengthBase">Indicates which type of base length value is to be returned.</param>
    /// <param name="fobj">
    /// The FO object against which the percentage should be evaluated.
    /// TODO: replace with the real <c>FObj</c> type when Fop.Fo is ported.
    /// </param>
    /// <returns>The base length value of the given kind.</returns>
    int GetBaseLength(int lengthBase, object? fobj);
}
