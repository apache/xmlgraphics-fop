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
using Fop.Render.Pdf;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// End-to-end PDF tests for embedded SVG (<c>fo:instream-foreign-object</c>), text-decoration and
/// letter-spacing: the full parse-&gt;layout-&gt;render pipeline must produce a well-formed PDF.
/// </summary>
public class SvgAndDecorationPdfTests
{
    private const string FoXml = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block text-decoration="underline" letter-spacing="2pt">Decorated &amp; tracked heading</fo:block>
              <fo:block text-decoration="line-through">Struck through.</fo:block>
              <fo:block>
                <fo:instream-foreign-object content-width="100pt" content-height="100pt">
                  <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
                    <rect x="0" y="0" width="100" height="100" fill="#eef" stroke="navy" stroke-width="2"/>
                    <circle cx="50" cy="50" r="40" fill="orange"/>
                    <path d="M20 50 L50 20 L80 50 L50 80 Z" fill="green" stroke="black" stroke-width="2"/>
                    <text x="50" y="55" font-size="12" text-anchor="middle" fill="black">SVG</text>
                  </svg>
                </fo:instream-foreign-object>
              </fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void SvgDecorationAndLetterSpacingConvertToValidPdf()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(FoXml);

        Assert.True(pdf.Length > 1000, $"PDF unexpectedly small: {pdf.Length} bytes");
        Assert.Equal("%PDF-", Encoding.ASCII.GetString(pdf, 0, 5));
        string tail = Encoding.ASCII.GetString(pdf, Math.Max(0, pdf.Length - 8), Math.Min(8, pdf.Length));
        Assert.Contains("EOF", tail);
    }
}
