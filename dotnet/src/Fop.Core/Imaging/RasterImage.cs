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

using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Fop.Imaging;

/// <summary>How an <see cref="EmbeddableImage"/>'s sample data is encoded.</summary>
public enum ImageEncoding
{
    /// <summary>The original JPEG byte stream (embed directly as <c>DCTDecode</c>).</summary>
    Jpeg,

    /// <summary>Interleaved 8-bit RGB samples (embed as raw, e.g. <c>FlateDecode</c>-compressed).</summary>
    Rgb,
}

/// <summary>
/// A raster image prepared for embedding in a page-description format. JPEG sources are kept verbatim
/// (so they can be embedded losslessly as <c>DCTDecode</c>); every other format is decoded to 8-bit
/// RGB samples, with an optional 8-bit grayscale <see cref="Alpha"/> channel (a soft mask) when the
/// source has transparency.
/// </summary>
/// <param name="Width">Width in pixels.</param>
/// <param name="Height">Height in pixels.</param>
/// <param name="Encoding">How <see cref="Data"/> is encoded.</param>
/// <param name="Data">The JPEG stream, or interleaved RGB samples (row-major, 3 bytes/pixel).</param>
/// <param name="Components">Colour components in a JPEG (1=gray, 3=RGB, 4=CMYK); 3 for decoded RGB.</param>
/// <param name="Alpha">An 8-bit grayscale alpha plane (1 byte/pixel), or <c>null</c> when fully opaque.</param>
public sealed record EmbeddableImage(
    int Width,
    int Height,
    ImageEncoding Encoding,
    byte[] Data,
    int Components,
    byte[]? Alpha);

/// <summary>
/// Loads a raster image into an <see cref="EmbeddableImage"/> for a renderer to embed. JPEG inputs are
/// passed through unchanged; all other formats are decoded with
/// <see href="https://github.com/SixLabors/ImageSharp">SixLabors.ImageSharp</see> to RGB (+ alpha).
/// </summary>
public static class RasterImage
{
    /// <summary>Loads from a file path or raw bytes (exactly one non-null), or <c>null</c> on failure.</summary>
    public static EmbeddableImage? Load(string? path, byte[]? bytes)
    {
        try
        {
            byte[]? data = bytes is { Length: > 0 }
                ? bytes
                : !string.IsNullOrEmpty(path) && File.Exists(path) ? File.ReadAllBytes(path) : null;
            if (data is null)
            {
                return null;
            }

            if (IsJpeg(data) && TryReadJpegInfo(data, out int jw, out int jh, out int comps))
            {
                return new EmbeddableImage(jw, jh, ImageEncoding.Jpeg, data, comps, Alpha: null);
            }

            return DecodeRgb(data);
        }
        catch (Exception)
        {
            return null;
        }
    }

    private static EmbeddableImage DecodeRgb(byte[] data)
    {
        using Image<Rgba32> image = Image.Load<Rgba32>(data);
        int w = image.Width;
        int h = image.Height;
        var rgb = new byte[w * h * 3];
        byte[]? alpha = null;
        bool hasAlpha = false;

        int rgbIndex = 0;
        var alphaBuffer = new byte[w * h];
        int alphaIndex = 0;
        image.ProcessPixelRows(accessor =>
        {
            for (int y = 0; y < accessor.Height; y++)
            {
                Span<Rgba32> row = accessor.GetRowSpan(y);
                foreach (Rgba32 px in row)
                {
                    rgb[rgbIndex++] = px.R;
                    rgb[rgbIndex++] = px.G;
                    rgb[rgbIndex++] = px.B;
                    alphaBuffer[alphaIndex++] = px.A;
                    if (px.A != 255)
                    {
                        hasAlpha = true;
                    }
                }
            }
        });

        if (hasAlpha)
        {
            alpha = alphaBuffer;
        }

        return new EmbeddableImage(w, h, ImageEncoding.Rgb, rgb, Components: 3, alpha);
    }

    private static bool IsJpeg(byte[] data) => data.Length >= 3 && data[0] == 0xFF && data[1] == 0xD8 && data[2] == 0xFF;

    /// <summary>
    /// Reads a JPEG's pixel dimensions and component count from its first Start-Of-Frame marker,
    /// skipping other segments. Returns <c>false</c> if no SOF is found.
    /// </summary>
    private static bool TryReadJpegInfo(byte[] d, out int width, out int height, out int components)
    {
        width = height = components = 0;
        int i = 2; // past SOI (FF D8)
        while (i + 1 < d.Length)
        {
            if (d[i] != 0xFF)
            {
                i++;
                continue;
            }

            byte marker = d[i + 1];
            i += 2;

            // Standalone markers without a length payload.
            if (marker is 0xD8 or 0xD9 || (marker >= 0xD0 && marker <= 0xD7))
            {
                continue;
            }

            if (i + 1 >= d.Length)
            {
                break;
            }

            int segLength = (d[i] << 8) | d[i + 1];
            // SOF0..SOF15 (except DHT=C4, JPG=C8, DAC=CC) carry frame dimensions.
            bool isSof = marker is >= 0xC0 and <= 0xCF && marker is not (0xC4 or 0xC8 or 0xCC);
            if (isSof && i + 7 < d.Length)
            {
                height = (d[i + 3] << 8) | d[i + 4];
                width = (d[i + 5] << 8) | d[i + 6];
                components = d[i + 7];
                return width > 0 && height > 0;
            }

            i += segLength;
        }

        return false;
    }
}
