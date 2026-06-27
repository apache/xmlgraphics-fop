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
/// Base interface for compound datatypes.
/// <para>
/// Port of <c>org.apache.fop.datatypes.CompoundDatatype</c>. The Java interface extends the
/// <c>Constants</c> marker interface (a holder for property-id constants) and uses the
/// <c>Property</c> type; neither is ported yet. The <c>Constants</c> inheritance carries no
/// behaviour and is dropped, and component values are typed as <see cref="object"/> as a stand-in.
/// </para>
/// <para>TODO: replace the <see cref="object"/> component type with <c>Property</c> when Fop.Fo.Properties is ported.</para>
/// </summary>
public interface ICompoundDatatype
{
    /// <summary>Sets a component of the compound datatype.</summary>
    /// <param name="cmpId">ID of the component.</param>
    /// <param name="cmpnValue">Value of the component.</param>
    /// <param name="isDefault">Indicates if it is the default value.</param>
    void SetComponent(int cmpId, object? cmpnValue, bool isDefault);

    /// <summary>Returns a component of the compound datatype.</summary>
    /// <param name="cmpId">ID of the component.</param>
    /// <returns>The value of the component.</returns>
    object? GetComponent(int cmpId);
}
