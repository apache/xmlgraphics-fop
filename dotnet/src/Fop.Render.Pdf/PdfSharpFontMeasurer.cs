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

using System.Collections.Concurrent;
using Fop.Layout;
using PdfSharp.Drawing;
using PdfSharp.Pdf;

namespace Fop.Render.Pdf;

/// <summary>
/// An <see cref="IFontMeasurer"/> implemented with PdfSharp, so that layout measurement and PDF
/// rendering use exactly the same font metrics. Resolves fonts via the
/// <see cref="LiberationFontResolver"/>.
/// </summary>
public sealed class PdfSharpFontMeasurer : IFontMeasurer
{
    private readonly XGraphics measureSurface;
    private readonly ConcurrentDictionary<FontKey, XFont> fontCache = new();
    private readonly Lock measureLock = new();

    /// <summary>Creates the measurer and ensures the font resolver is installed.</summary>
    public PdfSharpFontMeasurer()
    {
        LiberationFontResolver.EnsureInstalled();
        // A scratch page provides an XGraphics for text measurement.
        var doc = new PdfDocument();
        PdfPage page = doc.AddPage();
        measureSurface = XGraphics.FromPdfPage(page);
    }

    /// <summary>Returns (creating and caching) the PdfSharp font for a <see cref="FontKey"/>.</summary>
    public XFont GetXFont(FontKey font) => fontCache.GetOrAdd(font, static key =>
    {
        XFontStyleEx style = (key.IsBold, key.IsItalic) switch
        {
            (true, true) => XFontStyleEx.BoldItalic,
            (true, false) => XFontStyleEx.Bold,
            (false, true) => XFontStyleEx.Italic,
            (false, false) => XFontStyleEx.Regular,
        };

        return new XFont(key.Family, key.SizePoints, style);
    });

    /// <inheritdoc/>
    public double MeasureWidthMpt(string text, FontKey font)
    {
        if (string.IsNullOrEmpty(text))
        {
            return 0;
        }

        XFont xFont = GetXFont(font);
        double widthPt;
        lock (measureLock)
        {
            widthPt = measureSurface.MeasureString(text, xFont).Width;
        }

        return widthPt * 1000.0;
    }

    /// <inheritdoc/>
    public double AscenderMpt(FontKey font)
    {
        XFont xFont = GetXFont(font);
        XFontMetrics metrics = xFont.Metrics;
        return (double)metrics.Ascent / metrics.UnitsPerEm * font.SizeMpt;
    }

    /// <inheritdoc/>
    public double DescenderMpt(FontKey font)
    {
        XFont xFont = GetXFont(font);
        XFontMetrics metrics = xFont.Metrics;
        return Math.Abs((double)metrics.Descent) / metrics.UnitsPerEm * font.SizeMpt;
    }
}
