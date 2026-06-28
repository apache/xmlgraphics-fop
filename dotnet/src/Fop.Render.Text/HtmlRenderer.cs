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

namespace Fop.Render.Text;

/// <summary>
/// Renders an XSL-FO document's logical content to a semantic HTML5 document: headings, paragraphs
/// with <c>&lt;strong&gt;</c>/<c>&lt;em&gt;</c>/<c>&lt;a&gt;</c>, <c>&lt;ul&gt;</c> lists,
/// <c>&lt;table&gt;</c>s (header rows as <c>&lt;th&gt;</c>) and <c>&lt;img&gt;</c>s. All text is
/// HTML-escaped.
/// </summary>
public sealed class HtmlRenderer
{
    /// <summary>Converts an FO document string to an HTML document.</summary>
    public string Convert(string foXml)
    {
        ArgumentNullException.ThrowIfNull(foXml);
        return Render(FoTreeBuilder.ParseString(foXml));
    }

    /// <summary>Converts an FO document stream to HTML, written (UTF-8) to <paramref name="output"/>.</summary>
    public void Convert(Stream foInput, Stream output)
    {
        ArgumentNullException.ThrowIfNull(foInput);
        ArgumentNullException.ThrowIfNull(output);
        string html = Render(FoTreeBuilder.Parse(foInput));
        using var writer = new StreamWriter(output, new UTF8Encoding(false), leaveOpen: true);
        writer.Write(html);
    }

    /// <summary>Renders an already-parsed FO tree to an HTML document.</summary>
    public string Render(FoRoot root)
    {
        ArgumentNullException.ThrowIfNull(root);
        var sb = new StringBuilder();
        sb.Append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n</head>\n<body>\n");
        WriteBlocks(sb, DocExtractor.Extract(root), indent: "  ");
        sb.Append("</body>\n</html>\n");
        return sb.ToString();
    }

    private void WriteBlocks(StringBuilder sb, IReadOnlyList<DocBlock> blocks, string indent)
    {
        foreach (DocBlock block in blocks)
        {
            switch (block)
            {
                case DocParagraph p:
                    bool heading = p.HeadingLevel is > 0 and <= 6;
                    // A heading element is already emphasised; don't also wrap its text in <strong>.
                    string content = Inline(p.Inlines, suppressBold: heading);
                    if (content.Length == 0)
                    {
                        break;
                    }

                    string tag = heading ? "h" + p.HeadingLevel : "p";
                    sb.Append(indent).Append('<').Append(tag).Append('>').Append(content)
                        .Append("</").Append(tag).Append(">\n");
                    break;

                case DocList list:
                    sb.Append(indent).Append("<ul>\n");
                    foreach (DocListItem item in list.Items)
                    {
                        sb.Append(indent).Append("  <li>");
                        WriteInlineOrBlocks(sb, item.Body, indent + "  ");
                        sb.Append("</li>\n");
                    }

                    sb.Append(indent).Append("</ul>\n");
                    break;

                case DocTable table:
                    WriteTable(sb, table, indent);
                    break;

                case DocImage image:
                    sb.Append(indent).Append("<img src=\"").Append(AttrEscape(image.Source))
                        .Append("\" alt=\"").Append(AttrEscape(image.Alt)).Append("\">\n");
                    break;
            }
        }
    }

    private void WriteTable(StringBuilder sb, DocTable table, string indent)
    {
        sb.Append(indent).Append("<table>\n");
        foreach (DocTableRow row in table.Rows)
        {
            sb.Append(indent).Append("  <tr>");
            string cellTag = row.IsHeader ? "th" : "td";
            foreach (DocTableCell cell in row.Cells)
            {
                sb.Append('<').Append(cellTag);
                if (cell.ColumnSpan > 1)
                {
                    sb.Append(" colspan=\"").Append(cell.ColumnSpan).Append('"');
                }

                sb.Append('>');
                WriteInlineOrBlocks(sb, cell.Body, indent);
                sb.Append("</").Append(cellTag).Append('>');
            }

            sb.Append("</tr>\n");
        }

        sb.Append(indent).Append("</table>\n");
    }

    /// <summary>
    /// Writes a cell/list-item body: a single paragraph emits just its inline content, while richer
    /// content (nested lists, multiple paragraphs, images) is emitted as full block markup.
    /// </summary>
    private void WriteInlineOrBlocks(StringBuilder sb, IReadOnlyList<DocBlock> body, string indent)
    {
        if (body is [DocParagraph only])
        {
            sb.Append(Inline(only.Inlines));
            return;
        }

        sb.Append('\n');
        WriteBlocks(sb, body, indent + "  ");
        sb.Append(indent);
    }

    private string Inline(IReadOnlyList<DocInline> inlines, bool suppressBold = false)
    {
        var sb = new StringBuilder();
        foreach (DocInline run in inlines)
        {
            string text = Escape(run.Text);
            if (run.Bold && !suppressBold)
            {
                text = "<strong>" + text + "</strong>";
            }

            if (run.Italic)
            {
                text = "<em>" + text + "</em>";
            }

            if (run.Uri is { Length: > 0 } uri)
            {
                text = "<a href=\"" + AttrEscape(uri) + "\">" + text + "</a>";
            }

            sb.Append(text);
        }

        return sb.ToString().Trim();
    }

    private static string Escape(string text) => text
        .Replace("&", "&amp;").Replace("<", "&lt;").Replace(">", "&gt;");

    private static string AttrEscape(string text) => Escape(text).Replace("\"", "&quot;");
}
