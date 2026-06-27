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

using Fop.Colors;

namespace Fop.Layout;

/// <summary>
/// The laid-out document: an ordered list of pages, each carrying positioned primitives ready for a
/// renderer to paint. This is a pragmatic, flat "intermediate format" -- richer nested area trees
/// (block/line areas) can layer on later, but positioned runs are sufficient to render text.
/// <para>All coordinates are in millipoints, measured from the top-left of the page.</para>
/// </summary>
public sealed class AreaTree
{
    private readonly List<PageArea> pages = new();

    /// <summary>The pages, in order.</summary>
    public IReadOnlyList<PageArea> Pages => pages;

    /// <summary>Adds a page.</summary>
    public void AddPage(PageArea page)
    {
        ArgumentNullException.ThrowIfNull(page);
        pages.Add(page);
    }
}

/// <summary>A single laid-out page and its positioned content.</summary>
public sealed class PageArea
{
    private readonly List<TextRun> textRuns = new();
    private readonly List<RectFill> rectFills = new();
    private readonly List<ImageRun> images = new();

    /// <summary>Creates a page of the given size (millipoints).</summary>
    public PageArea(double widthMpt, double heightMpt)
    {
        WidthMpt = widthMpt;
        HeightMpt = heightMpt;
    }

    /// <summary>Page width in millipoints.</summary>
    public double WidthMpt { get; }

    /// <summary>Page height in millipoints.</summary>
    public double HeightMpt { get; }

    /// <summary>Positioned text runs on this page.</summary>
    public IReadOnlyList<TextRun> TextRuns => textRuns;

    /// <summary>Filled rectangles (e.g. backgrounds, rules) on this page.</summary>
    public IReadOnlyList<RectFill> RectFills => rectFills;

    /// <summary>Positioned images on this page.</summary>
    public IReadOnlyList<ImageRun> Images => images;

    /// <summary>Adds a text run.</summary>
    public void Add(TextRun run)
    {
        ArgumentNullException.ThrowIfNull(run);
        textRuns.Add(run);
    }

    /// <summary>Adds a filled rectangle.</summary>
    public void Add(RectFill rect) => rectFills.Add(rect);

    /// <summary>Adds a positioned image.</summary>
    public void Add(ImageRun image)
    {
        ArgumentNullException.ThrowIfNull(image);
        images.Add(image);
    }
}

/// <summary>
/// A run of text positioned on a page. <see cref="XMpt"/> is the left edge of the run and
/// <see cref="BaselineYMpt"/> is the text baseline, both in millipoints from the page top-left.
/// </summary>
/// <param name="XMpt">Left edge of the run, in millipoints from the page left.</param>
/// <param name="BaselineYMpt">Text baseline, in millipoints from the page top.</param>
/// <param name="Text">The run's text.</param>
/// <param name="Font">The font to render with.</param>
/// <param name="Color">The fill colour.</param>
public sealed record TextRun(double XMpt, double BaselineYMpt, string Text, FontKey Font, FopColor Color);

/// <summary>A filled rectangle on a page (top-left origin, millipoints).</summary>
/// <param name="XMpt">Left edge.</param>
/// <param name="YMpt">Top edge.</param>
/// <param name="WidthMpt">Width.</param>
/// <param name="HeightMpt">Height.</param>
/// <param name="Color">Fill colour.</param>
public sealed record RectFill(double XMpt, double YMpt, double WidthMpt, double HeightMpt, FopColor Color);

/// <summary>
/// A positioned image on a page (top-left origin, millipoints). The source is resolved either as a
/// filesystem path (<see cref="SourcePath"/>) or as already-loaded bytes (<see cref="SourceBytes"/>);
/// at least one is set.
/// </summary>
/// <param name="XMpt">Left edge.</param>
/// <param name="YMpt">Top edge.</param>
/// <param name="WidthMpt">Drawn width.</param>
/// <param name="HeightMpt">Drawn height.</param>
/// <param name="SourcePath">The resolved filesystem path of the image, or <c>null</c> when bytes are supplied.</param>
/// <param name="SourceBytes">The image bytes, or <c>null</c> when a path is supplied.</param>
public sealed record ImageRun(
    double XMpt,
    double YMpt,
    double WidthMpt,
    double HeightMpt,
    string? SourcePath,
    byte[]? SourceBytes);
