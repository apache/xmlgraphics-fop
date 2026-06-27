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

using System.Drawing;

namespace Fop.Fonts;

/// <summary>
/// Main interface for access to font metrics.
/// <para>
/// Port of the Java interface <c>org.apache.fop.fonts.FontMetrics</c>. The method surface is kept
/// faithful; Java types are mapped to their .NET counterparts:
/// <list type="bullet">
/// <item><description><c>java.net.URI</c> becomes <see cref="System.Uri"/>.</description></item>
/// <item><description><c>java.awt.Rectangle</c> becomes <see cref="System.Drawing.Rectangle"/>.</description></item>
/// <item><description><c>java.util.Set&lt;String&gt;</c> becomes <see cref="IReadOnlySet{T}"/> of
/// <see cref="string"/>.</description></item>
/// <item><description>the nested kerning <c>Map&lt;Integer, Map&lt;Integer, Integer&gt;&gt;</c>
/// becomes a read-only dictionary of read-only dictionaries.</description></item>
/// </list>
/// </para>
/// </summary>
public interface IFontMetrics
{
    /// <summary>Gets the URI of the font file from which these metrics were loaded.</summary>
    Uri FontUri { get; }

    /// <summary>Gets the "PostScript" font name (Example: "Helvetica-BoldOblique").</summary>
    string FontName { get; }

    /// <summary>Gets the font's full name (Example: "Helvetica Bold Oblique").</summary>
    string FullName { get; }

    /// <summary>Gets the font's family names as a set of strings (Example: "Helvetica").</summary>
    IReadOnlySet<string> FamilyNames { get; }

    /// <summary>
    /// Gets the font name for font embedding (may include a prefix, Example: "1E28bcArialMT").
    /// </summary>
    string EmbedFontName { get; }

    /// <summary>Gets the type of the font.</summary>
    FontType FontType { get; }

    /// <summary>
    /// Returns the maximum ascent of the font. Note: this is not the same as
    /// <see cref="GetAscender"/>.
    /// </summary>
    /// <param name="size">font size.</param>
    /// <returns>ascent in millipoints.</returns>
    int GetMaxAscent(int size);

    /// <summary>Returns the nominal ascent of the font within the em box.</summary>
    /// <param name="size">font size.</param>
    /// <returns>ascent in millipoints.</returns>
    int GetAscender(int size);

    /// <summary>Returns the size of a capital letter measured from the font's baseline.</summary>
    /// <param name="size">font size.</param>
    /// <returns>height of capital characters.</returns>
    int GetCapHeight(int size);

    /// <summary>Returns the descent of the font.</summary>
    /// <param name="size">font size.</param>
    /// <returns>descent in millipoints.</returns>
    int GetDescender(int size);

    /// <summary>Returns the typical font height.</summary>
    /// <param name="size">font size.</param>
    /// <returns>font height in millipoints.</returns>
    int GetXHeight(int size);

    /// <summary>Returns the width (in 1/1000ths of point size) of the character at code point i.</summary>
    /// <param name="i">code point index.</param>
    /// <param name="size">font size.</param>
    /// <returns>the width of the character.</returns>
    int GetWidth(int i, int size);

    /// <summary>
    /// Returns the array of widths. This is used to get an array for inserting in an output format;
    /// it should not be used for lookup.
    /// </summary>
    /// <returns>an array of widths.</returns>
    int[] GetWidths();

    /// <summary>Returns the bounding box of the glyph at the given index, for the given font size.</summary>
    /// <param name="glyphIndex">glyph index.</param>
    /// <param name="size">font size.</param>
    /// <returns>the bounding box scaled in 1/1000ths of the given size.</returns>
    Rectangle GetBoundingBox(int glyphIndex, int size);

    /// <summary>Indicates if the font has kerning information.</summary>
    bool HasKerningInfo { get; }

    /// <summary>Returns the kerning map for the font.</summary>
    /// <returns>the kerning map.</returns>
    IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerningInfo();

    /// <summary>
    /// Returns the distance from the baseline to the center of the underline (a negative value
    /// indicates below the baseline).
    /// </summary>
    /// <param name="size">font size.</param>
    /// <returns>the position in 1/1000ths of the font size.</returns>
    int GetUnderlinePosition(int size);

    /// <summary>Returns the thickness of the underline.</summary>
    /// <param name="size">font size.</param>
    /// <returns>the thickness in 1/1000ths of the font size.</returns>
    int GetUnderlineThickness(int size);

    /// <summary>
    /// Returns the distance from the baseline to the center of the strikeout line (a negative value
    /// indicates below the baseline).
    /// </summary>
    /// <param name="size">font size.</param>
    /// <returns>the position in 1/1000ths of the font size.</returns>
    int GetStrikeoutPosition(int size);

    /// <summary>Returns the thickness of the strikeout line.</summary>
    /// <param name="size">font size.</param>
    /// <returns>the thickness in 1/1000ths of the font size.</returns>
    int GetStrikeoutThickness(int size);

    /// <summary>
    /// Determines if metrics support a specific feature in a specified font table.
    /// </summary>
    /// <param name="tableType">type of table (GSUB, GPOS, ...).</param>
    /// <param name="script">script to qualify feature lookup.</param>
    /// <param name="language">language to qualify feature lookup.</param>
    /// <param name="feature">feature to test.</param>
    /// <returns><c>true</c> if the feature is supported (and has at least one lookup).</returns>
    bool HasFeature(int tableType, string script, string language, string feature);

    /// <summary>Determines whether the font is a multibyte font.</summary>
    bool IsMultiByte { get; }
}
