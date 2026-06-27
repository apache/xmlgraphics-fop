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

using Fop.Fonts;
using Fop.Util;
using Xunit;

namespace Fop.Fonts.Tests;

public class CodePointMappingTests
{
    [Fact]
    public void GetMapping_UnknownEncoding_Throws()
    {
        Assert.Throws<NotSupportedException>(() => CodePointMapping.GetMapping("NoSuchEncoding"));
    }

    [Fact]
    public void GetMapping_IsCachedAndReturnsSameInstance()
    {
        CodePointMapping a = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        CodePointMapping b = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Same(a, b);
        Assert.Equal("WinAnsiEncoding", a.Name);
    }

    [Theory]
    // ASCII characters map to their own code point in WinAnsi.
    [InlineData('A', 0x41)]
    [InlineData('a', 0x61)]
    [InlineData('0', 0x30)]
    [InlineData(' ', 0x20)]
    public void WinAnsi_MapsAsciiToItself(char c, int expected)
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Equal((char)expected, mapping.MapChar(c));
    }

    [Fact]
    public void WinAnsi_MapsEuroSignToCodePoint0x80()
    {
        // The Euro sign (U+20AC) is a non-Latin-1 glyph reached via binary search; in WinAnsi it
        // lives at code point 0x80.
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Equal((char)0x80, mapping.MapChar('\u20AC'));
    }

    [Fact]
    public void WinAnsi_UnmappedCharReturnsNotFound()
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        // A CJK ideograph is not present in WinAnsi.
        Assert.Equal(SingleByteEncoding.NotFoundCodePoint, mapping.MapChar('\u4E00'));
    }

    [Fact]
    public void WinAnsi_GetUnicodeForIndexRoundTrips()
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Equal('A', mapping.GetUnicodeForIndex(0x41));
        Assert.Equal('\u20AC', mapping.GetUnicodeForIndex(0x80));
    }

    [Fact]
    public void GetUnicodeForIndex_UnmappedSlotIsNotACharacter()
    {
        // WinAnsi code point 0x81 is unmapped.
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Equal(CharUtilities.NotACharacter, mapping.GetUnicodeForIndex(0x81));
    }

    [Fact]
    public void GetUnicodeCharMap_Has256EntriesAndIsACopy()
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.STANDARD_ENCODING);
        char[] map = mapping.GetUnicodeCharMap();
        Assert.Equal(256, map.Length);
        // mutating the returned copy must not affect the mapping
        map[0x41] = 'Z';
        Assert.Equal('A', mapping.GetUnicodeForIndex(0x41));
    }

    [Fact]
    public void CharNameMap_HasGlyphNamesAndNotDefForUnmapped()
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        string[] names = mapping.GetCharNameMap();
        Assert.Equal(256, names.Length);
        Assert.Equal("A", names[0x41]);
        Assert.Equal("space", names[0x20]);
        Assert.Equal("Euro", names[0x80]);
        Assert.Equal(CodePointMapping.NotDef, names[0x81]);
    }

    [Fact]
    public void GetCodePointForGlyph_FindsAndMisses()
    {
        CodePointMapping mapping = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING);
        Assert.Equal((short)0x41, mapping.GetCodePointForGlyph("A"));
        Assert.Equal((short)-1, mapping.GetCodePointForGlyph("notARealGlyphName"));
    }

    [Fact]
    public void Constructor_WithoutNames_DerivesNotDefMap()
    {
        // [code point, unicode] pairs; with no charNameMap the names fall back to .notdef.
        CodePointMapping mapping = new("custom", [0x41, 0x0041, 0x42, 0x0042]);
        Assert.Equal('A', mapping.GetUnicodeForIndex(0x41));
        Assert.Equal((char)0x41, mapping.MapChar('A'));
        string[] names = mapping.GetCharNameMap();
        Assert.All(names, n => Assert.Equal(CodePointMapping.NotDef, n));
    }

    [Fact]
    public void AllBuiltInEncodings_AreResolvable()
    {
        foreach (string enc in new[]
        {
            CodePointMapping.STANDARD_ENCODING,
            CodePointMapping.ISOLATIN1_ENCODING,
            CodePointMapping.CE_ENCODING,
            CodePointMapping.MAC_ROMAN_ENCODING,
            CodePointMapping.WIN_ANSI_ENCODING,
            CodePointMapping.PDF_DOC_ENCODING,
            CodePointMapping.SYMBOL_ENCODING,
            CodePointMapping.ZAPF_DINGBATS_ENCODING,
        })
        {
            CodePointMapping mapping = CodePointMapping.GetMapping(enc);
            Assert.Equal(enc, mapping.Name);
            Assert.Equal(256, mapping.GetUnicodeCharMap().Length);
        }
    }
}
