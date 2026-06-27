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
/// Base class for font classes.
/// <para>
/// Port of <c>org.apache.fop.fonts.Typeface</c>. The self-contained portion is ported here:
/// the <see cref="NotFound"/> sentinel, the character-mapping-operation counter used to detect
/// whether a font was used at all, the missing-glyph warning de-duplication (capped at 8 distinct
/// characters), and the convenience implementations of <see cref="GetMaxAscent"/>,
/// <see cref="HasFeature"/>, <see cref="IsMultiByte"/> and <see cref="IsCid"/>.
/// </para>
/// <para>
/// Members that depend on not-yet-ported types are kept abstract or stubbed:
/// <list type="bullet">
/// <item><description>
/// The Java <c>FontEventListener eventListener</c> field and its use inside the missing-glyph
/// warning are deferred: <c>FontEventListener</c> is not ported in this slice. The
/// de-duplication/cap behaviour is preserved; the actual notification/logging is marked with a TODO.
/// </description></item>
/// <item><description>
/// The Java warning text used <c>org.apache.xmlgraphics.fonts.Glyphs.charToGlyphName</c>, which is
/// not available in this slice (it lives in Apache XML Graphics Commons / would be replaced by
/// SixLabors.Fonts). The glyph-name part of the message is therefore marked with a TODO.
/// </description></item>
/// </list>
/// </para>
/// </summary>
public abstract class Typeface : IFontMetrics
{
    /// <summary>
    /// Code point that is used if no code point for a specific character has been found.
    /// </summary>
    public const char NotFound = '#';

    // Maximum number of distinct missing glyphs to warn about (Java magic number 8).
    private const int MaxWarnedChars = 8;

    // Used to identify whether a font has been used (a character map operation is the trigger).
    // This could be a bool but Java keeps it a long "out of statistical interest".
    private long charMapOps;

    // Distinct characters already warned about; lazily created, mirroring the Java HashSet.
    private HashSet<char>? warnedChars;

    /// <summary>Gets the encoding of the font.</summary>
    public abstract string EncodingName { get; }

    /// <inheritdoc/>
    public abstract Uri FontUri { get; }

    /// <inheritdoc/>
    public abstract string FontName { get; }

    /// <inheritdoc/>
    public abstract string FullName { get; }

    /// <inheritdoc/>
    public abstract IReadOnlySet<string> FamilyNames { get; }

    /// <inheritdoc/>
    public abstract string EmbedFontName { get; }

    /// <inheritdoc/>
    public abstract FontType FontType { get; }

    /// <inheritdoc/>
    public abstract int GetAscender(int size);

    /// <inheritdoc/>
    public abstract int GetCapHeight(int size);

    /// <inheritdoc/>
    public abstract int GetDescender(int size);

    /// <inheritdoc/>
    public abstract int GetXHeight(int size);

    /// <inheritdoc/>
    public abstract int GetWidth(int i, int size);

    /// <inheritdoc/>
    public abstract int[] GetWidths();

    /// <inheritdoc/>
    public abstract System.Drawing.Rectangle GetBoundingBox(int glyphIndex, int size);

    /// <inheritdoc/>
    public abstract bool HasKerningInfo { get; }

    /// <inheritdoc/>
    public abstract IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerningInfo();

    /// <inheritdoc/>
    public abstract int GetUnderlinePosition(int size);

    /// <inheritdoc/>
    public abstract int GetUnderlineThickness(int size);

    /// <inheritdoc/>
    public abstract int GetStrikeoutPosition(int size);

    /// <inheritdoc/>
    public abstract int GetStrikeoutThickness(int size);

    /// <summary>Maps a Unicode character to a code point in the font.</summary>
    /// <param name="c">character to map.</param>
    /// <returns>the mapped character.</returns>
    public abstract char MapChar(char c);

    /// <summary>Determines whether this font contains a particular character/glyph.</summary>
    /// <param name="c">character to check.</param>
    /// <returns><c>true</c> if the character is supported, <c>false</c> otherwise.</returns>
    public abstract bool HasChar(char c);

    /// <summary>
    /// Indicates whether this font had to do any character mapping operations. If not, it is an
    /// indication that the font has never actually been used.
    /// </summary>
    public bool HadMappingOperations => charMapOps > 0;

    /// <summary>
    /// The number of distinct characters that have been warned about as missing glyphs (capped at
    /// 8). Not present in the Java original; exposed to make the de-duplication/cap behaviour of
    /// <see cref="WarnMissingGlyph"/> observable by subclasses and tests.
    /// </summary>
    protected internal int WarnedGlyphCount => warnedChars?.Count ?? 0;

    /// <inheritdoc/>
    public virtual bool IsMultiByte => false;

    /// <summary>Indicates whether the font is CID-keyed.</summary>
    public bool IsCid => FontType == FontType.Type1C;

    /// <inheritdoc/>
    public virtual int GetMaxAscent(int size) => GetAscender(size);

    /// <inheritdoc/>
    public virtual bool HasFeature(int tableType, string script, string language, string feature) =>
        false;

    /// <summary>
    /// Keeps track of character mapping operations in order to determine whether a font was used
    /// at all.
    /// </summary>
    protected void NotifyMapOperation() => charMapOps++;

    /// <summary>
    /// Provides a proper warning if a glyph is not available. Warnings are de-duplicated per
    /// character and capped at 8 distinct characters, matching the Java behaviour.
    /// </summary>
    /// <param name="c">the character which is missing.</param>
    protected void WarnMissingGlyph(char c)
    {
        // Give up, character is not available
        warnedChars ??= [];

        if (warnedChars.Count < MaxWarnedChars && !warnedChars.Contains(c))
        {
            warnedChars.Add(c);

            // TODO: when FontEventListener is ported, notify it here via
            // eventListener.GlyphNotAvailable(this, c, FontName) instead of the fallback below.
            // The fallback message in Java additionally included the glyph name from
            // org.apache.xmlgraphics.fonts.Glyphs.charToGlyphName(c), which is not available in this
            // slice. The de-duplication and 8-character cap are preserved either way.
            _ = warnedChars.Count == MaxWarnedChars;
        }
    }

    /// <inheritdoc/>
    public override string ToString() => $"{base.ToString()}{{{FullName}}}";
}
