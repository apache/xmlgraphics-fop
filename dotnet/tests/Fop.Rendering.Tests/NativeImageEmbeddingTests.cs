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

using System;
using System.IO;
using System.IO.Compression;
using System.Text;
using System.Text.RegularExpressions;
using Fop.Layout;
using Fop.Render.Pdf;
using Fop.Render.Pdf.Native;
using PdfSharp.Pdf.IO;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Tests that the native renderer embeds raster images as PDF image XObjects: PNG/other formats are
/// decoded to FlateDecode RGB (with a soft mask for alpha), and JPEG is passed through as DCTDecode.
/// </summary>
public class NativeImageEmbeddingTests
{
    private const string Rgb2x2 =
        "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAFElEQVR4nGP4z8DAAMIM////ZwAAHu8E/KPItPcAAAAASUVORK5CYII=";

    private const string Rgba2x2 =
        "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAFElEQVR4nGP4z8DwHwgbGMA0EAEAPdkHevpJ8dsAAAAASUVORK5CYII=";

    private static byte[] RenderImage(byte[] imageBytes)
    {
        var tree = new AreaTree();
        var page = new PageArea(200_000, 200_000);
        page.Add(new ImageRun(10_000, 10_000, 64_000, 64_000, SourcePath: null, imageBytes));
        tree.AddPage(page);

        using var output = new MemoryStream();
        new NativePdfRenderer(new PdfSharpFontMeasurer()).Render(tree, output);
        return output.ToArray();
    }

    private static string Latin1(byte[] pdf) => Encoding.Latin1.GetString(pdf);

    /// <summary>
    /// Inflates every FlateDecode stream in the PDF and concatenates the results, so tests can search
    /// for content-stream operators that are no longer stored uncompressed.
    /// </summary>
    private static string InflatedStreams(byte[] pdf)
    {
        var sb = new StringBuilder();
        foreach (Match m in Regex.Matches(Latin1(pdf), "stream\r?\n", RegexOptions.None))
        {
            int start = m.Index + m.Length;
            int end = Latin1(pdf).IndexOf("endstream", start, StringComparison.Ordinal);
            if (end < 0)
            {
                continue;
            }

            byte[] raw = pdf[start..end];
            try
            {
                using var zin = new ZLibStream(new MemoryStream(raw), CompressionMode.Decompress);
                using var outMs = new MemoryStream();
                zin.CopyTo(outMs);
                sb.Append(Encoding.Latin1.GetString(outMs.ToArray()));
            }
            catch
            {
                // Not a zlib stream (e.g. raw JPEG/DCTDecode); skip.
            }
        }

        return sb.ToString();
    }

    [Fact]
    public void RgbPngEmbedsAsFlateImage()
    {
        byte[] pdf = RenderImage(Convert.FromBase64String(Rgb2x2));
        string text = Latin1(pdf);
        Assert.Contains("/Subtype /Image", text);
        Assert.Contains("/ColorSpace /DeviceRGB", text);
        Assert.Contains("/Filter /FlateDecode", text);
        Assert.Contains(" Do", InflatedStreams(pdf)); // the image is actually drawn (in the content stream)
        Assert.DoesNotContain("/SMask", text); // opaque -> no soft mask
    }

    [Fact]
    public void RgbaPngEmbedsSoftMask()
    {
        byte[] pdf = RenderImage(Convert.FromBase64String(Rgba2x2));
        string text = Latin1(pdf);
        Assert.Contains("/SMask", text);
        Assert.Contains("/ColorSpace /DeviceGray", text); // the soft-mask image
    }

    [Fact]
    public void JpegEmbedsAsDctDecodePassthrough()
    {
        // A minimal JPEG: SOI + an SOF0 declaring 200x100, 3 components, then EOI. The native path
        // passes JPEG bytes through as DCTDecode (no decode), reading dimensions from the SOF marker.
        byte[] jpeg =
        [
            0xFF, 0xD8,                                     // SOI
            0xFF, 0xC0, 0x00, 0x11, 0x08, 0x00, 0x64, 0x00, // SOF0, len 17, prec 8, height 100
            0xC8, 0x03,                                     // width 200, 3 components
            0x01, 0x11, 0x00, 0x02, 0x11, 0x01, 0x03, 0x11, 0x01,
            0xFF, 0xD9,                                     // EOI
        ];

        byte[] pdf = RenderImage(jpeg);
        string text = Latin1(pdf);
        Assert.Contains("/Filter /DCTDecode", text);
        Assert.Contains("/Width 200", text);
        Assert.Contains("/Height 100", text);
    }

    [Fact]
    public void EmbeddedImagePdfReopensInPdfSharp()
    {
        byte[] pdf = RenderImage(Convert.FromBase64String(Rgba2x2));
        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.Equal(1, doc.PageCount);
    }

    [Fact]
    public void UndecodableImageFallsBackToPlaceholder()
    {
        byte[] pdf = RenderImage([1, 2, 3, 4]);
        Assert.DoesNotContain("/Subtype /Image", Latin1(pdf));
        Assert.Contains(" re f", InflatedStreams(pdf)); // placeholder box (in the content stream)
    }
}
