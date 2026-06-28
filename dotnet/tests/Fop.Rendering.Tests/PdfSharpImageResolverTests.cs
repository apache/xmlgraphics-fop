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
using Fop.Layout;
using Fop.Render.Pdf;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>Tests for <see cref="PdfSharpImageResolver"/> reading intrinsic raster-image sizes.</summary>
public class PdfSharpImageResolverTests
{
    // A 4x2 px RGB PNG with no resolution chunk. PdfSharp assumes 96 dpi for such an image, so the
    // intrinsic size is 4px*72/96 = 3pt wide and 2px*72/96 = 1.5pt tall.
    private const string Png4x2Base64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAQAAAACCAIAAADwyuo0AAAAEElEQVR4nGP4z8AARwzIHABvqgf5gNwAKAAAAABJRU5ErkJggg==";

    [Fact]
    public void ReadsIntrinsicSizeFromBytes()
    {
        byte[] png = Convert.FromBase64String(Png4x2Base64);
        ImageIntrinsics? size = new PdfSharpImageResolver().Resolve(path: null, png);
        Assert.NotNull(size);
        // 96 dpi assumed: 4px -> 3pt (3000mpt), 2px -> 1.5pt (1500mpt). The 2:1 aspect is preserved.
        Assert.Equal(3_000, size!.Value.WidthMpt, 0);
        Assert.Equal(1_500, size!.Value.HeightMpt, 0);
        Assert.Equal(2.0, size!.Value.WidthMpt / size!.Value.HeightMpt, 3);
    }

    [Fact]
    public void MissingFileResolvesToNull()
    {
        Assert.Null(new PdfSharpImageResolver().Resolve("/no/such/file.png", bytes: null));
    }

    [Fact]
    public void GarbageBytesResolveToNull()
    {
        Assert.Null(new PdfSharpImageResolver().Resolve(path: null, new byte[] { 1, 2, 3, 4 }));
    }
}
