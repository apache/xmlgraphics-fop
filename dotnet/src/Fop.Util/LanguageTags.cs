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

namespace Fop.Util;

/// <summary>
/// Provides utility methods for manipulating language tags compliant with the RFC 3066
/// specification (http://www.ietf.org/rfc/rfc3066.txt). A typical language tag is a 2-letter
/// language code sometimes followed by a country code, e.g. <c>en</c>, <c>en-US</c>.
/// <para>Port of <c>org.apache.fop.util.LanguageTags</c>.</para>
/// </summary>
public static class LanguageTags
{
    /// <summary>
    /// Converts the given locale to an RFC 3066 compliant language tag.
    /// </summary>
    /// <param name="locale">a locale.</param>
    /// <returns>the corresponding language tag.</returns>
    public static string ToLanguageTag(Locale locale)
    {
        ArgumentNullException.ThrowIfNull(locale);
        string country = locale.Country;
        return country.Length > 0 ? $"{locale.Language}-{country}" : locale.Language;
    }

    /// <summary>
    /// Converts an RFC 3066 compliant language tag to a locale.
    /// </summary>
    /// <param name="languageTag">the language tag to convert.</param>
    /// <returns>the corresponding locale.</returns>
    public static Locale ToLocale(string languageTag)
    {
        ArgumentNullException.ThrowIfNull(languageTag);
        string[] parts = languageTag.Split('-');
        return parts.Length == 1 ? new Locale(parts[0]) : new Locale(parts[0], parts[1]);
    }
}
