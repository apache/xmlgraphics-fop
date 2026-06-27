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
using Fop.Fo;
using Fop.Layout;

namespace Fop.Render.Pdf;

/// <summary>
/// High-level facade for the FO-to-PDF pipeline: parse an XSL-FO document, lay it out, and render
/// the result to PDF. This is the modern equivalent of the <c>org.apache.fop.apps.Fop</c> entry
/// point, wired to the PdfSharp renderer.
/// </summary>
public sealed class FopProcessor
{
    private readonly PdfSharpFontMeasurer measurer;
    private readonly LayoutEngine layoutEngine;
    private readonly PdfRenderer renderer;

    /// <summary>Creates a processor with the default PdfSharp font handling.</summary>
    public FopProcessor()
    {
        measurer = new PdfSharpFontMeasurer();
        layoutEngine = new LayoutEngine(measurer);
        renderer = new PdfRenderer(measurer);
    }

    /// <summary>Lays out an already-parsed FO tree (useful for inspection/testing).</summary>
    public AreaTree LayOut(FoRoot root) => layoutEngine.LayOut(root);

    /// <summary>Converts an FO document stream to PDF, written to <paramref name="pdfOutput"/>.</summary>
    public void Convert(Stream foInput, Stream pdfOutput)
    {
        ArgumentNullException.ThrowIfNull(foInput);
        ArgumentNullException.ThrowIfNull(pdfOutput);
        FoRoot root = FoTreeBuilder.Parse(foInput);
        Render(root, pdfOutput);
    }

    /// <summary>Converts an FO document string to PDF bytes.</summary>
    public byte[] Convert(string foXml)
    {
        ArgumentNullException.ThrowIfNull(foXml);
        FoRoot root = FoTreeBuilder.ParseString(foXml);
        using var buffer = new MemoryStream();
        Render(root, buffer);
        return buffer.ToArray();
    }

    /// <summary>Converts an FO file on disk to a PDF file on disk.</summary>
    public void ConvertFile(string foPath, string pdfPath)
    {
        ArgumentException.ThrowIfNullOrEmpty(foPath);
        ArgumentException.ThrowIfNullOrEmpty(pdfPath);
        using FileStream input = File.OpenRead(foPath);
        using FileStream output = File.Create(pdfPath);
        Convert(input, output);
    }

    private void Render(FoRoot root, Stream output)
    {
        AreaTree tree = layoutEngine.LayOut(root);
        renderer.Render(tree, output);
    }
}
