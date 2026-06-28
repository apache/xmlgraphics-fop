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

namespace Fop.Layout;

/// <summary>
/// The intrinsic (natural) size of a raster image, in millipoints, derived from its pixel dimensions
/// and resolution (DPI). This is the size an <c>fo:external-graphic</c> takes when its
/// <c>content-width</c>/<c>content-height</c> are <c>auto</c>.
/// </summary>
/// <param name="WidthMpt">Intrinsic width in millipoints.</param>
/// <param name="HeightMpt">Intrinsic height in millipoints.</param>
public readonly record struct ImageIntrinsics(double WidthMpt, double HeightMpt);

/// <summary>
/// Resolves the intrinsic size of a raster image so the layout engine can size an
/// <c>fo:external-graphic</c> whose dimensions are <c>auto</c>. Implemented by the rendering back-end
/// (which owns image decoding), so layout and rendering agree on the same source — mirroring how
/// <see cref="IFontMeasurer"/> is injected for text. When no resolver is supplied (or it returns
/// <c>null</c>), the engine falls back to a default placeholder size.
/// </summary>
public interface IImageResolver
{
    /// <summary>
    /// Returns the intrinsic size of the image identified by <paramref name="path"/> or
    /// <paramref name="bytes"/> (exactly one is non-null), or <c>null</c> when the image cannot be
    /// resolved (missing file, unsupported codec, decode error). Implementations must not throw.
    /// </summary>
    ImageIntrinsics? Resolve(string? path, byte[]? bytes);
}
