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

using System.Text;
using Fop.Layout;
using Fop.Render.Pdf;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Exercises custom-font registration: a real TTF from the repo is registered under a family name and
/// used by a document; the registry resolves that family to the registered face (not Liberation);
/// without registration the family falls back to Liberation; and the measurer and resolver agree on
/// the face for a given <see cref="FontKey"/>.
/// </summary>
public class CustomFontTests
{
    private const string FontFamily = "MyDejaVuSerif";

    private static string FontsDir => FindFontsDir();
    private static string SerifTtf => Path.Combine(FontsDir, "DejaVuLGCSerif.ttf");

    private static string FoUsing(string family) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="{family}" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block>Custom font rendering smoke test, with enough text to lay out a line.</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void RegisteredFamilyRendersValidPdf()
    {
        var processor = new FopProcessor();
        string face = processor.RegisterFont(SerifTtf, FontFamily);

        Assert.StartsWith("FopFont_", face);

        byte[] pdf = processor.Convert(FoUsing(FontFamily));
        Assert.True(pdf.Length > 1000, $"PDF unexpectedly small: {pdf.Length} bytes");
        Assert.Equal("%PDF-", Encoding.ASCII.GetString(pdf, 0, 5));
    }

    [Fact]
    public void RegistryResolvesRegisteredFamilyToRegisteredFace()
    {
        var registry = new FontRegistry();
        string face = registry.RegisterFont(SerifTtf, FontFamily);

        // Resolves to the registered face, case/whitespace-insensitively, not to Liberation.
        Assert.Equal(face, registry.Resolve(FontFamily, bold: false, italic: false));
        Assert.Equal(face, registry.Resolve("  mydejavuserif  ", bold: false, italic: false));
        Assert.True(registry.IsRegistryFace(face));
        Assert.NotNull(registry.GetFaceBytes(face));
    }

    [Fact]
    public void UnregisteredFamilyFallsBackToLiberation()
    {
        var registry = new FontRegistry();
        // Nothing registered: no custom resolution, so the resolver uses Liberation.
        Assert.True(registry.IsEmpty);
        Assert.Null(registry.Resolve(FontFamily, bold: false, italic: false));

        // A different family stays unresolved even after registering FontFamily.
        registry.RegisterFont(SerifTtf, FontFamily);
        Assert.Null(registry.Resolve("Helvetica", bold: false, italic: false));
    }

    [Fact]
    public void NearestStyleFallbackWithinSameFamily()
    {
        var registry = new FontRegistry();
        string regular = registry.RegisterFont(SerifTtf, FontFamily, bold: false, italic: false);

        // Only the regular face is registered, so a bold/italic request falls back to it.
        Assert.Equal(regular, registry.Resolve(FontFamily, bold: true, italic: false));
        Assert.Equal(regular, registry.Resolve(FontFamily, bold: false, italic: true));
        Assert.Equal(regular, registry.Resolve(FontFamily, bold: true, italic: true));

        // A distinct bold face is preferred once registered.
        string bold = registry.RegisterFont(SerifTtf, FontFamily, bold: true, italic: false);
        Assert.Equal(bold, registry.Resolve(FontFamily, bold: true, italic: false));
        Assert.NotEqual(regular, bold);
    }

    [Fact]
    public void DirectoryScanRegistersAtLeastOneFont()
    {
        var registry = new FontRegistry();
        IReadOnlyList<string> registered = registry.RegisterFontsDirectory(FontsDir);

        Assert.NotEmpty(registered);
        Assert.False(registry.IsEmpty);
        // The DejaVu serif face's name table reports the "DejaVu LGC Serif" family.
        Assert.NotNull(registry.Resolve("DejaVu LGC Serif", bold: false, italic: false));
    }

    [Fact]
    public void NameTableDerivesFamilyAndStyle()
    {
        byte[] data = File.ReadAllBytes(SerifTtf);
        (string family, bool bold, bool italic) = FontRegistry.DeriveFromFontFile(data, SerifTtf);

        Assert.Contains("Serif", family, StringComparison.OrdinalIgnoreCase);
        Assert.False(bold);
        Assert.False(italic);
    }

    [Fact]
    public void MeasurerAndResolverAgreeOnRegisteredFace()
    {
        var processor = new FopProcessor();
        string face = processor.RegisterFont(SerifTtf, FontFamily);

        var measurer = new PdfSharpFontMeasurer();
        var key = new FontKey(FontFamily, 12000, 400, global::Fop.Fo.FontStyle.Normal);

        // The measurer resolves the same registered face the resolver does (shared registry).
        Assert.Equal(face, measurer.ResolveFaceName(key));
        Assert.Equal(face, FopFontResolver.Shared.ResolveTypeface(FontFamily, false, false)!.FaceName);

        // And it can actually measure with that font.
        Assert.True(measurer.MeasureWidthMpt("hello", key) > 0);
    }

    [Fact]
    public void UnregisteredFamilyMeasuresViaLiberation()
    {
        var measurer = new PdfSharpFontMeasurer();
        var helvetica = new FontKey("Helvetica", 12000, 400, global::Fop.Fo.FontStyle.Normal);
        var times = new FontKey("Times", 12000, 400, global::Fop.Fo.FontStyle.Normal);
        var courier = new FontKey("Courier", 12000, 400, global::Fop.Fo.FontStyle.Normal);

        Assert.Equal("LiberationSans-Regular", measurer.ResolveFaceName(helvetica));
        Assert.Equal("LiberationSerif-Regular", measurer.ResolveFaceName(times));
        Assert.Equal("LiberationMono-Regular", measurer.ResolveFaceName(courier));

        // Bold/italic still map through Liberation faces.
        var helvBoldItalic = new FontKey("Helvetica", 12000, 700, global::Fop.Fo.FontStyle.Italic);
        Assert.Equal("LiberationSans-BoldItalic", measurer.ResolveFaceName(helvBoldItalic));
    }

    /// <summary>Walks up from the test assembly to locate the repo's bundled TTF font directory.</summary>
    private static string FindFontsDir()
    {
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir is not null)
        {
            string candidate = Path.Combine(dir.FullName, "fop", "test", "resources", "fonts", "ttf");
            if (Directory.Exists(candidate))
            {
                return candidate;
            }

            dir = dir.Parent;
        }

        throw new DirectoryNotFoundException("Could not locate fop/test/resources/fonts/ttf from the test base directory.");
    }
}
