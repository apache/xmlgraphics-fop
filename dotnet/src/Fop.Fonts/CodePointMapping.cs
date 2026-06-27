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

using Fop.Util;

namespace Fop.Fonts;

/// <summary>
/// Character-to-glyph code point mapping for the built-in single-byte encodings (1-byte character
/// encodings with 256 characters).
/// <para>
/// This single C# type combines two Java types:
/// <list type="bullet">
/// <item><description>
/// <c>org.apache.fop.fonts.AbstractCodePointMapping</c> -- the lookup logic
/// (<see cref="MapChar"/>, <see cref="GetUnicodeForIndex"/>, the binary-searched
/// non-Latin-1 table and the character-name map).</description></item>
/// <item><description>
/// <c>org.apache.fop.fonts.CodePointMapping</c> -- the (code-generated) concrete subclass that
/// carries the per-encoding data tables and the static <c>getMapping(String)</c> registry. Its data
/// lives in the generated partial <c>CodePointMapping.Data.cs</c>; the registry is
/// <see cref="GetMapping"/> here.</description></item>
/// </list>
/// </para>
/// <para>
/// Observable behaviour preserved from Java:
/// <list type="bullet">
/// <item><description>
/// <see cref="GetMapping"/> caches built mappings in a synchronized map (Java
/// <c>Collections.synchronizedMap</c>); an unknown encoding throws
/// <see cref="NotSupportedException"/> (Java <c>UnsupportedOperationException</c>).</description></item>
/// <item><description>
/// <see cref="BuildFromTable"/> reproduces the Java insertion-sort that orders the non-Latin-1
/// glyphs by Unicode value so <see cref="MapChar"/> can binary-search them, and keeps the
/// "first mapping wins" rule for both the Latin-1 fast path and the code-point/Unicode maps.</description></item>
/// <item><description>
/// the <c>charNameMap</c> is normalised to 256 entries with <c>.notdef</c> substituted for nulls
/// (Java used <c>Glyphs.NOTDEF</c>).</description></item>
/// </list>
/// </para>
/// </summary>
public sealed partial class CodePointMapping : ISingleByteEncoding
{
    /// <summary>The ".notdef" glyph name (Java <c>Glyphs.NOTDEF</c>).</summary>
    public const string NotDef = ".notdef";

    // Java used Collections.synchronizedMap(new HashMap()); a guarded dictionary matches that.
    private static readonly Dictionary<string, CodePointMapping> Mappings = [];
    private static readonly Lock MappingsLock = new();

    private readonly string name;
    private char[] latin1Map = null!;
    private char[] characters = null!;
    private char[] codepoints = null!;
    private char[] unicodeMap = null!; // code point to Unicode char
    private string[]? charNameMap; // all character names in the encoding

    /// <summary>
    /// Main constructor.
    /// </summary>
    /// <param name="name">the name of the encoding.</param>
    /// <param name="table">the table (<c>[code point, unicode scalar value]+</c>) with the mapping.</param>
    public CodePointMapping(string name, int[] table)
        : this(name, table, null)
    {
    }

    /// <summary>
    /// Extended constructor.
    /// </summary>
    /// <param name="name">the name of the encoding.</param>
    /// <param name="table">the table (<c>[code point, unicode scalar value]+</c>) with the mapping.</param>
    /// <param name="charNameMap">all character names in the encoding (a value of <c>null</c> will be
    /// converted to ".notdef").</param>
    public CodePointMapping(string name, int[] table, string?[]? charNameMap)
    {
        this.name = name;
        BuildFromTable(table);
        if (charNameMap is not null)
        {
            this.charNameMap = new string[256];
            for (int i = 0; i < 256; i++)
            {
                string? charName = i < charNameMap.Length ? charNameMap[i] : null;
                this.charNameMap[i] = charName ?? NotDef;
            }
        }
    }

    /// <inheritdoc/>
    public string Name => this.name;

    /// <summary>
    /// Builds the internal lookup structures based on a given table.
    /// </summary>
    /// <param name="table">the table (<c>[code point, unicode scalar value]+</c>) with the mapping.</param>
    private void BuildFromTable(int[] table)
    {
        int nonLatin1 = 0;
        latin1Map = new char[256];
        unicodeMap = new char[256];
        Array.Fill(unicodeMap, CharUtilities.NotACharacter);
        for (int i = 0; i < table.Length; i += 2)
        {
            char unicode = (char)table[i + 1];
            if (unicode < 256)
            {
                if (latin1Map[unicode] == 0)
                {
                    latin1Map[unicode] = (char)table[i];
                }
            }
            else
            {
                ++nonLatin1;
            }

            if (unicodeMap[table[i]] == CharUtilities.NotACharacter)
            {
                unicodeMap[table[i]] = unicode;
            }
        }

        characters = new char[nonLatin1];
        codepoints = new char[nonLatin1];
        int top = 0;
        for (int i = 0; i < table.Length; i += 2)
        {
            char c = (char)table[i + 1];
            if (c >= 256)
            {
                ++top;

                // Insertion sort by Unicode value (verbatim port of the Java loop) so MapChar can
                // binary-search the non-Latin-1 glyphs.
                for (int j = top - 1; j >= 0; --j)
                {
                    if (j > 0 && characters[j - 1] >= c)
                    {
                        characters[j] = characters[j - 1];
                        codepoints[j] = codepoints[j - 1];
                    }
                    else
                    {
                        characters[j] = c;
                        codepoints[j] = (char)table[i];
                        break;
                    }
                }
            }
        }
    }

    /// <inheritdoc/>
    public char MapChar(char c)
    {
        if (c < 256)
        {
            char latin1 = latin1Map[c];
            if (latin1 > 0)
            {
                return latin1;
            }
        }

        int bot = 0;
        int top = characters.Length - 1;
        while (top >= bot)
        {
            int mid = (int)(((uint)bot + (uint)top) >> 1);
            char mc = characters[mid];

            if (c == mc)
            {
                return codepoints[mid];
            }
            else if (c < mc)
            {
                top = mid - 1;
            }
            else
            {
                bot = mid + 1;
            }
        }

        return SingleByteEncoding.NotFoundCodePoint;
    }

    /// <summary>
    /// Returns the main Unicode value that is associated with the given code point in the encoding.
    /// Note that multiple Unicode values can theoretically be mapped to one code point in the
    /// encoding.
    /// </summary>
    /// <param name="idx">the code point in the encoding.</param>
    /// <returns>the Unicode value (or U+FFFF, "NOT A CHARACTER", if no Unicode value is at that
    /// point).</returns>
    public char GetUnicodeForIndex(int idx) => unicodeMap[idx];

    /// <inheritdoc/>
    public char[] GetUnicodeCharMap() => (char[])unicodeMap.Clone();

    /// <summary>
    /// Returns the index of a character/glyph with the given name. Note that this method is
    /// relatively slow and should only be used for fallback operations.
    /// </summary>
    /// <param name="charName">the character name.</param>
    /// <returns>the index of the character in the encoding or -1 if it doesn't exist.</returns>
    public short GetCodePointForGlyph(string charName)
    {
        string[] names = charNameMap ?? GetCharNameMap();
        for (short i = 0, c = (short)names.Length; i < c; i++)
        {
            if (names[i] == charName)
            {
                return i;
            }
        }

        return -1;
    }

    /// <summary>Returns the glyph name registered at the given encoding code point.</summary>
    /// <param name="idx">the code point in the encoding.</param>
    /// <returns>the glyph name (".notdef" if none).</returns>
    public string GetNameFromCodePoint(int idx) => GetCharNameMap()[idx];

    /// <inheritdoc/>
    public string[] GetCharNameMap()
    {
        if (charNameMap is not null)
        {
            return (string[])charNameMap.Clone();
        }

        // Note: this is suboptimal but will probably never be used.
        // The Java fallback derived names via Glyphs.charToGlyphName(c), which is not available in
        // this slice (Apache XML Graphics Commons). We therefore return ".notdef" for every slot.
        // TODO: derive glyph names from Unicode values once a Glyphs equivalent is ported.
        string[] derived = new string[256];
        Array.Fill(derived, NotDef);
        return derived;
    }

    /// <inheritdoc/>
    public override string ToString() => Name;

    /// <summary>
    /// Returns the (cached) code point mapping for a built-in encoding.
    /// <para>
    /// Port of the generated static <c>CodePointMapping.getMapping(String)</c>: mappings are built
    /// lazily on first request and cached; an unknown encoding throws.
    /// </para>
    /// </summary>
    /// <param name="encoding">the encoding name (e.g. "WinAnsiEncoding").</param>
    /// <returns>the code point mapping.</returns>
    /// <exception cref="NotSupportedException">if the encoding is unknown (Java
    /// <c>UnsupportedOperationException</c>).</exception>
    public static CodePointMapping GetMapping(string encoding)
    {
        lock (MappingsLock)
        {
            if (Mappings.TryGetValue(encoding, out CodePointMapping? cached))
            {
                return cached;
            }

            foreach ((string constant, int[] table, string?[] names) in AllEncodings())
            {
                if (encoding == constant)
                {
                    CodePointMapping mapping = new(constant, table, names);
                    Mappings[constant] = mapping;
                    return mapping;
                }
            }
        }

        throw new NotSupportedException("Unknown encoding: " + encoding);
    }
}
