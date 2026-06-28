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

using Fop.Layout;

namespace Fop.Layout.Tests;

/// <summary>
/// A deterministic <see cref="IFontMeasurer"/> for tests: every glyph (including the space) is
/// 500 units wide at a 1000mpt font size, the ascender is 800 units and the descender 200 units,
/// each scaled linearly by the font size. This makes layout fully predictable without real fonts:
/// a string of <c>n</c> characters at size <c>s</c> advances <c>n * 500 * s / 1000</c> mpt.
/// </summary>
public sealed class FakeFontMeasurer : IFontMeasurer
{
    private const double GlyphUnits = 500.0;
    private const double AscenderUnits = 800.0;
    private const double DescenderUnits = 200.0;
    private const double CapHeightUnits = 700.0;
    private const double ReferenceSizeMpt = 1000.0;

    /// <summary>Advance width: 500 units per character, scaled by font size.</summary>
    public double MeasureWidthMpt(string text, FontKey font) =>
        text.Length * GlyphUnits * (font.SizeMpt / ReferenceSizeMpt);

    /// <summary>Ascender: 800 units scaled by font size.</summary>
    public double AscenderMpt(FontKey font) => AscenderUnits * (font.SizeMpt / ReferenceSizeMpt);

    /// <summary>Descender: 200 units scaled by font size.</summary>
    public double DescenderMpt(FontKey font) => DescenderUnits * (font.SizeMpt / ReferenceSizeMpt);

    /// <summary>Cap height: 700 units scaled by font size.</summary>
    public double CapHeightMpt(FontKey font) => CapHeightUnits * (font.SizeMpt / ReferenceSizeMpt);
}
