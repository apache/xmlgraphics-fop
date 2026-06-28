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

using Fop.Render.Text;
using Xunit;

namespace Fop.Render.Text.Tests;

/// <summary>Tests for the plain-text, Markdown and HTML back-ends.</summary>
public class TextBackendTests
{
    private const string Doc = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="210mm" page-height="297mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              <fo:block font-size="24pt" font-weight="bold">Title</fo:block>
              <fo:block>Plain with <fo:inline font-weight="bold">B</fo:inline> and
                <fo:inline font-style="italic">I</fo:inline> and
                <fo:basic-link external-destination="https://x.org">L</fo:basic-link>.</fo:block>
              <fo:list-block>
                <fo:list-item><fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                  <fo:list-item-body><fo:block>One</fo:block></fo:list-item-body></fo:list-item>
              </fo:list-block>
              <fo:table>
                <fo:table-column column-width="80pt"/><fo:table-column column-width="80pt"/>
                <fo:table-header><fo:table-row>
                  <fo:table-cell><fo:block>H1</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>H2</fo:block></fo:table-cell></fo:table-row></fo:table-header>
                <fo:table-body><fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell></fo:table-row></fo:table-body>
              </fo:table>
              <fo:block><fo:external-graphic src="pic.png"/></fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void MarkdownFormatsAllConstructs()
    {
        string md = new MarkdownRenderer().Convert(Doc);
        Assert.Contains("# Title", md);
        Assert.DoesNotContain("# **Title**", md);   // heading must not be double-emphasised
        Assert.Contains("**B**", md);
        Assert.Contains("_I_", md);
        Assert.Contains("[L](https://x.org)", md);
        Assert.Contains("- One", md);
        Assert.Contains("| H1 | H2 |", md);
        Assert.Contains("| --- | --- |", md);
        Assert.Contains("| a | b |", md);
        Assert.Contains("![image](pic.png)", md);
    }

    [Fact]
    public void HtmlFormatsAllConstructs()
    {
        string html = new HtmlRenderer().Convert(Doc);
        Assert.Contains("<!DOCTYPE html>", html);
        Assert.Contains("<h1>Title</h1>", html);
        Assert.Contains("<strong>B</strong>", html);
        Assert.Contains("<em>I</em>", html);
        Assert.Contains("<a href=\"https://x.org\">L</a>", html);
        Assert.Contains("<ul>", html);
        Assert.Contains("<li>One</li>", html);
        Assert.Contains("<th>H1</th>", html);
        Assert.Contains("<td>a</td>", html);
        Assert.Contains("<img src=\"pic.png\"", html);
    }

    [Fact]
    public void PlainTextDropsStylingAndKeepsContent()
    {
        string text = new PlainTextRenderer().Convert(Doc);
        Assert.Contains("Title", text);
        Assert.Contains("Plain with B and I and L.", text);
        Assert.DoesNotContain("**", text);
        Assert.DoesNotContain("<", text);
        Assert.Contains("- One", text);
        Assert.Contains("H1\tH2", text);     // tab-separated cells
        Assert.Contains("[image: pic.png]", text);
    }

    [Fact]
    public void HtmlEscapesSpecialCharacters()
    {
        string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="100pt" page-height="100pt">
                  <fo:region-body/></fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body">
                <fo:block>a &lt; b &amp; c &gt; d</fo:block>
              </fo:flow></fo:page-sequence>
            </fo:root>
            """;
        string html = new HtmlRenderer().Convert(fo);
        Assert.Contains("a &lt; b &amp; c &gt; d", html);
    }

    [Fact]
    public void EmptyDocumentProducesMinimalOutput()
    {
        string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="100pt" page-height="100pt">
                  <fo:region-body/></fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body">
                <fo:block></fo:block>
              </fo:flow></fo:page-sequence>
            </fo:root>
            """;
        Assert.Contains("<body>", new HtmlRenderer().Convert(fo));
        Assert.Equal("\n", new PlainTextRenderer().Convert(fo));
    }
}
