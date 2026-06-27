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

namespace Fop.Fonts;

/// <summary>
/// Font style/weight/priority constants.
/// <para>
/// These mirror the <c>public static final</c> fields of <c>org.apache.fop.fonts.Font</c> that the
/// foundation types in this slice depend on (style names, the 100..900 weight scale and the default
/// triplet priority). The full <c>Font</c> class is out of scope for this slice, so the constants it
/// shares with <see cref="FontTriplet"/> and <see cref="FontUtil"/> are inlined here to keep
/// <c>Fop.Fonts</c> standalone. Values are copied verbatim from the Java source.
/// </para>
/// </summary>
public static class FontConstants
{
    /// <summary>Extra bold font weight (<c>Font.WEIGHT_EXTRA_BOLD</c>).</summary>
    public const int WeightExtraBold = 800;

    /// <summary>Bold font weight (<c>Font.WEIGHT_BOLD</c>).</summary>
    public const int WeightBold = 700;

    /// <summary>Normal font weight (<c>Font.WEIGHT_NORMAL</c>).</summary>
    public const int WeightNormal = 400;

    /// <summary>Light font weight (<c>Font.WEIGHT_LIGHT</c>).</summary>
    public const int WeightLight = 200;

    /// <summary>Normal font style (<c>Font.STYLE_NORMAL</c>).</summary>
    public const string StyleNormal = "normal";

    /// <summary>Italic font style (<c>Font.STYLE_ITALIC</c>).</summary>
    public const string StyleItalic = "italic";

    /// <summary>Oblique font style (<c>Font.STYLE_OBLIQUE</c>).</summary>
    public const string StyleOblique = "oblique";

    /// <summary>Inclined font style (<c>Font.STYLE_INCLINED</c>).</summary>
    public const string StyleInclined = "inclined";

    /// <summary>The default priority of a font triplet/font mapping (<c>Font.PRIORITY_DEFAULT</c>).</summary>
    public const int PriorityDefault = 0;
}
