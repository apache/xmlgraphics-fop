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

namespace Fop.Render.Pdf.Native;

/// <summary>
/// Supplies the raw TrueType/OpenType font program (the <c>.ttf</c>/<c>.otf</c> bytes) for a resolved
/// <see cref="FontKey"/>, so the native renderer can embed the actual face. Implemented by the hosting
/// renderer, which owns font resolution (family/style → face → bytes). Returning <c>null</c> lets the
/// renderer fall back to a metric-compatible standard-14 font.
/// </summary>
public interface INativeFontProvider
{
    /// <summary>The font-program bytes for <paramref name="font"/>, or <c>null</c> to use a standard font.</summary>
    byte[]? GetFontProgram(FontKey font);
}
