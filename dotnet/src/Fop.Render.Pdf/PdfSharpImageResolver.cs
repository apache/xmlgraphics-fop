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
using PdfSharp.Drawing;

namespace Fop.Render.Pdf;

/// <summary>
/// An <see cref="IImageResolver"/> that reads a raster image's intrinsic size with PdfSharp's
/// <see cref="XImage"/> -- the same decoder the renderer uses to draw it, so layout and rendering
/// agree on the source. The intrinsic size is the pixel dimensions converted to points using the
/// image's resolution (DPI), defaulting to 72 dpi when the image carries no resolution.
/// </summary>
public sealed class PdfSharpImageResolver : IImageResolver
{
    private const double MptPerPoint = 1000.0;
    private const double DefaultDpi = 72.0;

    /// <inheritdoc/>
    public ImageIntrinsics? Resolve(string? path, byte[]? bytes)
    {
        try
        {
            XImage? image = Load(path, bytes);
            if (image is null)
            {
                return null;
            }

            using (image)
            {
                double dpiX = image.HorizontalResolution > 0 ? image.HorizontalResolution : DefaultDpi;
                double dpiY = image.VerticalResolution > 0 ? image.VerticalResolution : DefaultDpi;
                double widthMpt = image.PixelWidth * 72.0 / dpiX * MptPerPoint;
                double heightMpt = image.PixelHeight * 72.0 / dpiY * MptPerPoint;
                if (widthMpt <= 0 || heightMpt <= 0)
                {
                    return null;
                }

                return new ImageIntrinsics(widthMpt, heightMpt);
            }
        }
        catch (Exception)
        {
            // Best-effort: an undecodable image falls back to the engine's default placeholder size.
            return null;
        }
    }

    private static XImage? Load(string? path, byte[]? bytes)
    {
        if (bytes is { Length: > 0 })
        {
            return XImage.FromStream(new MemoryStream(bytes, writable: false));
        }

        if (!string.IsNullOrEmpty(path) && File.Exists(path))
        {
            return XImage.FromFile(path);
        }

        return null;
    }
}
