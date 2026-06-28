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
using Fop.Render.Pdf.Native;
using PdfSharp.Fonts;

namespace Fop.Render.Pdf;

/// <summary>
/// An <see cref="INativeFontProvider"/> backed by the shared <see cref="FopFontResolver"/>: it resolves
/// a <see cref="FontKey"/> to a face (a registered custom font, else the embedded Liberation fallback)
/// and returns that face's raw font-program bytes for the native renderer to embed. This is the bridge
/// that lets the PdfSharp-free native renderer embed the same faces the PdfSharp path uses.
/// </summary>
public sealed class ResolverFontProvider(FopFontResolver resolver) : INativeFontProvider
{
    /// <inheritdoc/>
    public byte[]? GetFontProgram(FontKey font)
    {
        FontResolverInfo? info = resolver.ResolveTypeface(font.Family, font.IsBold, font.IsItalic);
        return info is null ? null : resolver.GetFont(info.FaceName);
    }
}
