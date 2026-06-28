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
/// Maps a <see cref="FontKey"/> to one of the 14 standard PDF fonts (which every viewer ships, so no
/// embedding is needed). The family is classified as serif / sans-serif / monospace and combined with
/// the bold/italic flags to pick a Times / Helvetica / Courier face. This is metric-consistent with
/// the layout pass, which measures using the Liberation faces (Liberation Serif/Sans/Mono are
/// metrically compatible with Times/Helvetica/Courier).
/// </summary>
internal static class Base14Fonts
{
    /// <summary>Returns the PDF <c>BaseFont</c> name for <paramref name="font"/> (e.g. "Helvetica-Bold").</summary>
    public static string BaseFontName(FontKey font)
    {
        FontClass cls = Classify(font.Family);
        return cls switch
        {
            FontClass.Serif => (font.IsBold, font.IsItalic) switch
            {
                (true, true) => "Times-BoldItalic",
                (true, false) => "Times-Bold",
                (false, true) => "Times-Italic",
                _ => "Times-Roman",
            },
            FontClass.Mono => (font.IsBold, font.IsItalic) switch
            {
                (true, true) => "Courier-BoldOblique",
                (true, false) => "Courier-Bold",
                (false, true) => "Courier-Oblique",
                _ => "Courier",
            },
            _ => (font.IsBold, font.IsItalic) switch
            {
                (true, true) => "Helvetica-BoldOblique",
                (true, false) => "Helvetica-Bold",
                (false, true) => "Helvetica-Oblique",
                _ => "Helvetica",
            },
        };
    }

    private enum FontClass
    {
        Sans,
        Serif,
        Mono,
    }

    private static FontClass Classify(string family)
    {
        string f = family.Trim().ToLowerInvariant();
        if (f.Contains("mono") || f.Contains("courier") || f.Contains("consol"))
        {
            return FontClass.Mono;
        }

        if (f.Contains("serif") && !f.Contains("sans"))
        {
            return FontClass.Serif;
        }

        if (f.Contains("times") || f.Contains("georgia") || f.Contains("garamond") || f.Contains("roman")
            || f.Contains("minion") || f.Contains("book"))
        {
            return FontClass.Serif;
        }

        // sans-serif, Helvetica, Arial, Liberation Sans, and unknown families default to sans.
        return FontClass.Sans;
    }
}
