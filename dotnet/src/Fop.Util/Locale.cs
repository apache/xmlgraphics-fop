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

using System.Globalization;

namespace Fop.Util;

/// <summary>
/// A minimal stand-in for Java's <c>java.util.Locale</c>, carrying just the language and
/// country fields that the FOP utilities require.
/// <para>
/// .NET's <see cref="CultureInfo"/> rejects unknown/empty tags and normalises differently, so
/// the port models the locale directly to preserve Java's observable behaviour: the language is
/// normalised to lower case and the country to upper case, and equality is value-based and
/// case-insensitive on the original inputs (as Java's normalisation makes it).
/// </para>
/// </summary>
public sealed class Locale : IEquatable<Locale>
{
    /// <summary>
    /// Initialises a new locale from a language code only (the country is empty).
    /// </summary>
    /// <param name="language">an ISO 639 language code, or the empty string.</param>
    public Locale(string language)
        : this(language, string.Empty)
    {
    }

    /// <summary>
    /// Initialises a new locale from a language and country code.
    /// </summary>
    /// <param name="language">an ISO 639 language code, or the empty string.</param>
    /// <param name="country">an ISO 3166 country code, or the empty string.</param>
    public Locale(string language, string country)
    {
        ArgumentNullException.ThrowIfNull(language);
        ArgumentNullException.ThrowIfNull(country);

        // Java's Locale lower-cases the language and upper-cases the country.
        Language = language.ToLowerInvariant();
        Country = country.ToUpperInvariant();
    }

    /// <summary>Gets the language code, normalised to lower case (may be empty).</summary>
    public string Language { get; }

    /// <summary>Gets the country code, normalised to upper case (may be empty).</summary>
    public string Country { get; }

    /// <inheritdoc/>
    public bool Equals(Locale? other) =>
        other is not null
        && string.Equals(Language, other.Language, StringComparison.Ordinal)
        && string.Equals(Country, other.Country, StringComparison.Ordinal);

    /// <inheritdoc/>
    public override bool Equals(object? obj) => Equals(obj as Locale);

    /// <inheritdoc/>
    public override int GetHashCode() => HashCode.Combine(Language, Country);

    /// <inheritdoc/>
    public override string ToString() =>
        Country.Length > 0 ? $"{Language}_{Country}" : Language;
}
