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
using Fop.Layout;
using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Tests for <c>fo:external-graphic</c> intrinsic sizing: the layout engine consults an injected
/// <see cref="IImageResolver"/> for the natural size and applies <c>content-width</c>/
/// <c>content-height</c>/<c>scaling</c> over it (<see cref="FoExternalGraphic.ResolveContentSize"/>).
/// </summary>
public sealed class ExternalGraphicSizingTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    /// <summary>A resolver that reports a fixed intrinsic size for any image (200pt x 100pt).</summary>
    private sealed class FixedImageResolver(double wMpt, double hMpt) : IImageResolver
    {
        public ImageIntrinsics? Resolve(string? path, byte[]? bytes) => new(wMpt, hMpt);
    }

    private static AreaTree LayOut(string ifoAttrs, IImageResolver? resolver)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="1000pt" page-height="1000pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block><fo:external-graphic src="x.png" {ifoAttrs}/></fo:block>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        return new LayoutEngine(Measurer, resolver).LayOut(FoTreeBuilder.ParseString(fo));
    }

    private static ImageRun Image(AreaTree tree) => Assert.Single(tree.Pages[0].Images);

    [Fact]
    public void IntrinsicSizeUsedWhenContentSizeAuto()
    {
        ImageRun img = Image(LayOut(string.Empty, new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(200_000, img.WidthMpt, 1);
        Assert.Equal(100_000, img.HeightMpt, 1);
    }

    [Fact]
    public void DefaultSquareWhenNoResolver()
    {
        // No resolver: the engine falls back to a 72pt square placeholder.
        ImageRun img = Image(LayOut(string.Empty, resolver: null));
        Assert.Equal(72_000, img.WidthMpt, 1);
        Assert.Equal(72_000, img.HeightMpt, 1);
    }

    [Fact]
    public void WidthGivenScalesHeightUniformly()
    {
        // Intrinsic 200x100 (2:1); content-width=100pt -> height 50pt (aspect preserved).
        ImageRun img = Image(LayOut("content-width=\"100pt\"", new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(100_000, img.WidthMpt, 1);
        Assert.Equal(50_000, img.HeightMpt, 1);
    }

    [Fact]
    public void HeightGivenScalesWidthUniformly()
    {
        ImageRun img = Image(LayOut("content-height=\"50pt\"", new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(100_000, img.WidthMpt, 1);
        Assert.Equal(50_000, img.HeightMpt, 1);
    }

    [Fact]
    public void NonUniformScalingKeepsIntrinsicOtherDimension()
    {
        ImageRun img = Image(LayOut("content-width=\"100pt\" scaling=\"non-uniform\"",
            new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(100_000, img.WidthMpt, 1);
        Assert.Equal(100_000, img.HeightMpt, 1); // height stays intrinsic
    }

    [Fact]
    public void BothGivenAreHonoured()
    {
        ImageRun img = Image(LayOut("content-width=\"40pt\" content-height=\"90pt\"",
            new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(40_000, img.WidthMpt, 1);
        Assert.Equal(90_000, img.HeightMpt, 1);
    }

    [Fact]
    public void PercentageContentWidthIsRelativeToIntrinsic()
    {
        // content-width=50% of intrinsic 200pt = 100pt; uniform -> height 50pt.
        ImageRun img = Image(LayOut("content-width=\"50%\"", new FixedImageResolver(200_000, 100_000)));
        Assert.Equal(100_000, img.WidthMpt, 1);
        Assert.Equal(50_000, img.HeightMpt, 1);
    }
}
