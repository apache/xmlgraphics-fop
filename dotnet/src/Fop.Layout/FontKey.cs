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

using Fop.Fo;

namespace Fop.Layout;

/// <summary>
/// Identifies a concrete font instance for measurement and rendering: family, size (in
/// millipoints), numeric weight and style.
/// </summary>
/// <param name="Family">The font family (e.g. "Helvetica").</param>
/// <param name="SizeMpt">The font size in millipoints.</param>
/// <param name="Weight">The numeric font weight (100-900).</param>
/// <param name="Style">The font style.</param>
public readonly record struct FontKey(string Family, double SizeMpt, int Weight, FontStyle Style)
{
    /// <summary>The font size in points.</summary>
    public double SizePoints => SizeMpt / 1000.0;

    /// <summary>Whether this font is bold (weight &gt;= 700).</summary>
    public bool IsBold => Weight >= 700;

    /// <summary>Whether this font is italic or oblique.</summary>
    public bool IsItalic => Style is FontStyle.Italic or FontStyle.Oblique;
}

/// <summary>
/// Measures text for a given font. Implemented by the rendering back-end (so that layout and
/// rendering agree on metrics); injected into the <see cref="LayoutEngine"/>.
/// </summary>
public interface IFontMeasurer
{
    /// <summary>Returns the advance width of <paramref name="text"/> in millipoints.</summary>
    double MeasureWidthMpt(string text, FontKey font);

    /// <summary>Returns the ascent (top of glyph box above the baseline) in millipoints.</summary>
    double AscenderMpt(FontKey font);

    /// <summary>Returns the descent (below the baseline, as a positive value) in millipoints.</summary>
    double DescenderMpt(FontKey font);
}
