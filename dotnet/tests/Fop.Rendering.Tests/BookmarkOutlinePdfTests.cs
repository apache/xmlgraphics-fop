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
using System.Text;

using Fop.Render.Pdf;

using PdfSharp.Pdf;
using PdfSharp.Pdf.IO;

using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// End-to-end tests that a document with an <c>fo:bookmark-tree</c> converts to a valid PDF whose
/// document outline exposes the expected number of top-level (and nested) bookmark entries.
/// </summary>
public sealed class BookmarkOutlinePdfTests
{
    private const string BookmarkFo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block id="intro">Introduction</fo:block>
              <fo:block break-before="page" id="chap1">Chapter One</fo:block>
            </fo:flow>
          </fo:page-sequence>
          <fo:bookmark-tree>
            <fo:bookmark internal-destination="intro">
              <fo:bookmark-title>Introduction</fo:bookmark-title>
            </fo:bookmark>
            <fo:bookmark internal-destination="chap1" starting-state="hide">
              <fo:bookmark-title>Chapter One</fo:bookmark-title>
              <fo:bookmark internal-destination="chap1">
                <fo:bookmark-title>Section 1.1</fo:bookmark-title>
              </fo:bookmark>
            </fo:bookmark>
          </fo:bookmark-tree>
        </fo:root>
        """;

    [Fact]
    public void ConvertsToValidPdf()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(BookmarkFo);

        Assert.True(pdf.Length > 1000, $"PDF unexpectedly small: {pdf.Length} bytes");
        Assert.Equal("%PDF-", Encoding.ASCII.GetString(pdf, 0, 5));
    }

    [Fact]
    public void PdfExposesExpectedTopLevelOutlineEntries()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(BookmarkFo);

        using var stream = new MemoryStream(pdf);
        using PdfDocument document = PdfReader.Open(stream, PdfDocumentOpenMode.Import);

        // Two top-level bookmarks (Introduction, Chapter One); Chapter One nests one child.
        int topLevel = document.Outlines.Count;
        Assert.Equal(2, topLevel);
        int nested = document.Outlines[1].Outlines.Count;
        Assert.Equal(1, nested);
        Assert.Equal("Introduction", document.Outlines[0].Title);
        Assert.Equal("Chapter One", document.Outlines[1].Title);
        Assert.Equal("Section 1.1", document.Outlines[1].Outlines[0].Title);
    }

    [Fact]
    public void DocumentWithoutBookmarksHasNoOutline()
    {
        const string noBookmarks = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="12pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block>No bookmarks here</fo:block>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;

        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(noBookmarks);

        // No outline is emitted: the produced PDF is valid and carries no document outline. (Reopening
        // the doc and reading Outlines would throw in PdfSharp when the catalog has no /Outlines entry,
        // so we assert on the produced bytes instead: a valid PDF with no outline dictionary.)
        Assert.Equal("%PDF-", Encoding.ASCII.GetString(pdf, 0, 5));
        Assert.DoesNotContain("/Outlines", Encoding.Latin1.GetString(pdf));
    }
}
