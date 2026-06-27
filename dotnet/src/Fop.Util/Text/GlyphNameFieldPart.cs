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

using System.Text;

namespace Fop.Util.Text;

/// <summary>
/// Formats a character to a glyph name.
/// <para>
/// Port of <c>org.apache.fop.util.text.GlyphNameFieldPart</c>. The Java version delegates to
/// <c>org.apache.xmlgraphics.fonts.Glyphs.charToGlyphName</c>. That class has not been ported into
/// <c>Fop.Util</c> (which must stay dependency-free), so the glyph-name lookup is provided through a
/// pluggable <see cref="GlyphNameResolver"/>. A consumer wires in a resolver before using the
/// <c>glyph-name</c> format; until then the lookup throws.
/// </para>
/// </summary>
public sealed class GlyphNameFieldPart(string fieldName) : IPart
{
    /// <summary>
    /// Resolves a character to its glyph name (returns an empty string when the character has no
    /// glyph name). Defaults to a resolver that reports the lookup is not configured.
    /// </summary>
    public static Func<char, string> GlyphNameResolver { get; set; } = static _ =>
        throw new InvalidOperationException(
            "No glyph-name resolver is configured for AdvancedMessageFormat. "
            + "Set GlyphNameFieldPart.GlyphNameResolver to enable the 'glyph-name' format.");

    private readonly string fieldName = fieldName;

    /// <inheritdoc/>
    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(fieldName, out var obj);
        return obj != null && GetGlyphName(obj).Length > 0;
    }

    private static string GetGlyphName(object? obj)
    {
        if (obj is char c)
        {
            return GlyphNameResolver(c);
        }
        throw new ArgumentException(
            "Value for glyph name part must be a Character but was: "
            + (obj?.GetType().FullName ?? "null"));
    }

    /// <inheritdoc/>
    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        if (!parameters.ContainsKey(fieldName))
        {
            throw new ArgumentException(
                "Message pattern contains unsupported field name: " + fieldName);
        }
        parameters.TryGetValue(fieldName, out var obj);
        sb.Append(GetGlyphName(obj));
    }

    /// <inheritdoc/>
    public override string ToString() => "{" + fieldName + ",glyph-name}";

    /// <summary>Factory for <see cref="GlyphNameFieldPart"/>.</summary>
    public sealed class Factory : IPartFactory
    {
        /// <inheritdoc/>
        public IPart NewPart(string fieldName, string? values) => new GlyphNameFieldPart(fieldName);

        /// <inheritdoc/>
        public string Format => "glyph-name";
    }
}
