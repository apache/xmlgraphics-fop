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
/// Defines a 1-byte character encoding (with 256 characters).
/// <para>Port of the Java interface <c>org.apache.fop.fonts.SingleByteEncoding</c>.</para>
/// </summary>
public interface ISingleByteEncoding
{
    /// <summary>Gets the encoding's name.</summary>
    string Name { get; }

    /// <summary>
    /// Maps a Unicode character to a code point in the encoding.
    /// </summary>
    /// <param name="c">the Unicode character to map.</param>
    /// <returns>the code point in the encoding or 0 (=.notdef) if not found.</returns>
    char MapChar(char c);

    /// <summary>
    /// Returns the array of character names for this encoding (unmapped code points are
    /// represented by a ".notdef" value).
    /// </summary>
    string[] GetCharNameMap();

    /// <summary>
    /// Returns a character array with Unicode scalar values which can be used to map encoding
    /// code points to Unicode values. Note that this does not return all possible Unicode values
    /// that the encoding maps.
    /// </summary>
    char[] GetUnicodeCharMap();
}

/// <summary>
/// Shared constants for <see cref="ISingleByteEncoding"/>.
/// </summary>
public static class SingleByteEncoding
{
    /// <summary>
    /// Code point that is used if no code point for a specific character has been found
    /// (Java <c>SingleByteEncoding.NOT_FOUND_CODE_POINT</c>, value <c>'\0'</c>).
    /// </summary>
    public const char NotFoundCodePoint = '\0';
}
