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

using System.IO;
using System.IO.Compression;
using System.Text;
using System.Text.RegularExpressions;

using Fop.Render.Pdf;

using PdfSharp.Pdf.IO;

using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Tests that the native (PdfSharp-free) renderer paints transformed groups (rotated
/// <c>fo:block-container</c>s) under a content-stream CTM and surfaces their links as page annotations,
/// matching the PdfSharp renderer.
/// </summary>
public class NativeGroupRenderingTests
{
    private const string RotatedLinkFo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block-container absolute-position="absolute" left="120mm" top="120mm"
                  width="80mm" reference-orientation="90">
                <fo:block><fo:basic-link external-destination="url(https://example.com/)">Rotated link</fo:basic-link></fo:block>
              </fo:block-container>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void RotatedGroupIsPaintedUnderACtm()
    {
        byte[] pdf = new FopProcessor().ConvertNative(RotatedLinkFo);
        string content = InflatedStreams(pdf);

        // The group is wrapped in q/Q and concatenates a 90-degree clockwise CTM (0 -1 1 0 ...).
        Assert.Contains(" cm", content);
        Assert.Contains("0 -1 1 0 ", content);
        // The group's text is drawn inside the transform (a BT/Tj text-showing operator follows the cm).
        Assert.Contains("BT", content);
    }

    [Fact]
    public void RotatedGroupLinkBecomesAPageAnnotation()
    {
        byte[] pdf = new FopProcessor().ConvertNative(RotatedLinkFo);
        using var stream = new MemoryStream(pdf);
        using var doc = PdfReader.Open(stream, PdfDocumentOpenMode.Import);

        Assert.Equal(1, doc.PageCount);
        Assert.True(doc.Pages[0].Annotations.Count >= 1,
            "Expected a link annotation mapped from the rotated container.");
    }

    private static string InflatedStreams(byte[] pdf)
    {
        string latin1 = Encoding.Latin1.GetString(pdf);
        var sb = new StringBuilder();
        foreach (Match m in Regex.Matches(latin1, "stream\r?\n", RegexOptions.None))
        {
            int start = m.Index + m.Length;
            int end = latin1.IndexOf("endstream", start, System.StringComparison.Ordinal);
            if (end < 0)
            {
                continue;
            }

            byte[] raw = pdf[start..end];
            try
            {
                using var zin = new ZLibStream(new MemoryStream(raw), CompressionMode.Decompress);
                using var outMs = new MemoryStream();
                zin.CopyTo(outMs);
                sb.Append(Encoding.Latin1.GetString(outMs.ToArray()));
            }
            catch
            {
                // Not a zlib stream (e.g. raw JPEG/DCTDecode); skip.
            }
        }

        return sb.ToString();
    }
}
