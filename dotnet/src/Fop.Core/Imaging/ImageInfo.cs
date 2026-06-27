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

namespace Fop.Imaging;

/// <summary>
/// Lightweight description of a raster image: pixel dimensions and resolution.
/// <para>
/// In the original FOP the image pipeline is built on Apache XML Graphics Commons and Java AWT
/// (<c>java.awt.image</c>). This port replaces that stack with
/// <see href="https://github.com/SixLabors/ImageSharp">SixLabors.ImageSharp</see>; this type is
/// the first, self-contained step of that migration.
/// </para>
/// </summary>
/// <param name="Width">Width in pixels.</param>
/// <param name="Height">Height in pixels.</param>
/// <param name="HorizontalResolution">Horizontal resolution in DPI (0 if unknown).</param>
/// <param name="VerticalResolution">Vertical resolution in DPI (0 if unknown).</param>
public readonly record struct ImageDimensions(
    int Width,
    int Height,
    double HorizontalResolution,
    double VerticalResolution)
{
    /// <summary>
    /// Reads the dimensions and resolution of an image from a stream without decoding all pixels.
    /// </summary>
    public static async Task<ImageDimensions> ReadAsync(Stream stream, CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(stream);
        ImageInfo info = await Image.IdentifyAsync(stream, cancellationToken).ConfigureAwait(false);
        return FromImageInfo(info);
    }

    /// <summary>
    /// Reads the dimensions and resolution of an image file without decoding all pixels.
    /// </summary>
    public static async Task<ImageDimensions> ReadAsync(string path, CancellationToken cancellationToken = default)
    {
        ArgumentException.ThrowIfNullOrEmpty(path);
        ImageInfo info = await Image.IdentifyAsync(path, cancellationToken).ConfigureAwait(false);
        return FromImageInfo(info);
    }

    private static ImageDimensions FromImageInfo(ImageInfo info)
    {
        var meta = info.Metadata;
        double horizontal = 0;
        double vertical = 0;
        if (meta.ResolutionUnits != SixLabors.ImageSharp.Metadata.PixelResolutionUnit.AspectRatio)
        {
            horizontal = meta.HorizontalResolution;
            vertical = meta.VerticalResolution;
        }

        return new ImageDimensions(info.Width, info.Height, horizontal, vertical);
    }
}
