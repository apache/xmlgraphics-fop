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
/// Common contract for the trait enumerations (<see cref="Direction"/>,
/// <see cref="WritingMode"/>, <see cref="BorderStyle"/>, <see cref="RuleStyle"/>,
/// <see cref="Visibility"/>).
/// <para>
/// The Java original (<c>org.apache.fop.traits.TraitEnum</c>) is an abstract base class for
/// "typesafe enum" singletons, carrying a <c>name</c> string and an <c>enumValue</c> (one of the
/// <c>org.apache.fop.fo.Constants.EN_*</c> integers). In this port the trait types are real C#
/// <c>enum</c>s whose underlying integer value is exactly the corresponding <c>EN_*</c> constant,
/// so <see cref="GetEnumValue"/> is simply a cast. The lower-case canonical name (used by the FO
/// property machinery) is provided by each enum's <c>GetName</c> extension. This interface lets the
/// per-enum extension types expose those two members uniformly.
/// </para>
/// </summary>
public interface ITraitEnum
{
    /// <summary>Gets the canonical (lower-case) name of the enumeration value.</summary>
    string GetName();

    /// <summary>
    /// Gets the enumeration value (one of the <c>org.apache.fop.fo.Constants.EN_*</c> integers).
    /// </summary>
    int GetEnumValue();
}
