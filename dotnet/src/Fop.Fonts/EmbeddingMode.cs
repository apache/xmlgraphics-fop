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

namespace Fop.Fonts;

/// <summary>
/// Enumerates the embedding mode of fonts.
/// <para>
/// Port of <c>org.apache.fop.fonts.EmbeddingMode</c>. <see cref="Auto"/> defaults to
/// <see cref="Full"/> for Type 1 fonts and <see cref="Subset"/> for TrueType fonts. The Java
/// <c>getName()</c>/<c>getValue(String)</c> helpers live in <see cref="EmbeddingModeExtensions"/>.
/// </para>
/// </summary>
public enum EmbeddingMode
{
    /// <summary>Default option: assumes FULL for Type 1 fonts and SUBSET for TrueType fonts.</summary>
    Auto,

    /// <summary>Full font embedding: the whole of the font is written to file.</summary>
    Full,

    /// <summary>
    /// Subset font embedding: only the mandatory tables and a subset of glyphs are written to file.
    /// </summary>
    Subset,
}

/// <summary>
/// Helpers mirroring the Java <c>EmbeddingMode</c> instance/static methods.
/// </summary>
public static class EmbeddingModeExtensions
{
    /// <summary>
    /// Returns the name of this embedding mode in lower case (Java <c>getName()</c>, which used
    /// <c>toString().toLowerCase(Locale.ENGLISH)</c>).
    /// </summary>
    public static string GetName(this EmbeddingMode mode) => mode switch
    {
        EmbeddingMode.Auto => "auto",
        EmbeddingMode.Full => "full",
        EmbeddingMode.Subset => "subset",
        _ => throw new ArgumentOutOfRangeException(nameof(mode)),
    };

    /// <summary>
    /// Returns the embedding mode corresponding to the given name (not case sensitive). The Java
    /// comparison is against the enum constant name (AUTO/FULL/SUBSET), so it is case-insensitive.
    /// </summary>
    /// <param name="value">the name of an embedding mode.</param>
    /// <returns>the corresponding embedding mode.</returns>
    /// <exception cref="ArgumentException">if the name is not a valid embedding mode.</exception>
    public static EmbeddingMode GetValue(string value) => value switch
    {
        _ when Matches(value, EmbeddingMode.Auto) => EmbeddingMode.Auto,
        _ when Matches(value, EmbeddingMode.Full) => EmbeddingMode.Full,
        _ when Matches(value, EmbeddingMode.Subset) => EmbeddingMode.Subset,
        _ => throw new ArgumentException("Invalid embedding-mode: " + value),
    };

    private static bool Matches(string value, EmbeddingMode mode) =>
        string.Equals(value, mode.ToString(), StringComparison.OrdinalIgnoreCase);
}
