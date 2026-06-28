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
/// Renders an XSL-FO document's logical content to plain UTF-8 text: paragraphs separated by blank
/// lines, list items bulleted, table rows tab-separated, images shown as a bracketed placeholder.
/// Emphasis and links are dropped (plain text carries no styling).
/// </summary>
public sealed class PlainTextRenderer
{
    /// <summary>Converts an FO document string to plain text.</summary>
    public string Convert(string foXml)
    {
        ArgumentNullException.ThrowIfNull(foXml);
        return Render(FoTreeBuilder.ParseString(foXml));
    }

    /// <summary>Converts an FO document stream to plain text, written (UTF-8) to <paramref name="output"/>.</summary>
    public void Convert(Stream foInput, Stream output)
    {
        ArgumentNullException.ThrowIfNull(foInput);
        ArgumentNullException.ThrowIfNull(output);
        string text = Render(FoTreeBuilder.Parse(foInput));
        using var writer = new StreamWriter(output, new UTF8Encoding(false), leaveOpen: true);
        writer.Write(text);
    }

    /// <summary>Renders an already-parsed FO tree to plain text.</summary>
    public string Render(FoRoot root)
    {
        ArgumentNullException.ThrowIfNull(root);
        var sb = new StringBuilder();
        WriteBlocks(sb, DocExtractor.Extract(root), indent: string.Empty);
        return sb.ToString().TrimEnd() + "\n";
    }

    private static void WriteBlocks(StringBuilder sb, IReadOnlyList<DocBlock> blocks, string indent)
    {
        foreach (DocBlock block in blocks)
        {
            switch (block)
            {
                case DocParagraph p:
                    string text = InlineText(p.Inlines);
                    if (text.Length > 0)
                    {
                        sb.Append(indent).Append(text).Append("\n\n");
                    }

                    break;

                case DocList list:
                    foreach (DocListItem item in list.Items)
                    {
                        string body = FlattenBlocks(item.Body);
                        sb.Append(indent).Append("  - ").Append(body).Append('\n');
                    }

                    sb.Append('\n');
                    break;

                case DocTable table:
                    foreach (DocTableRow row in table.Rows)
                    {
                        sb.Append(indent)
                            .Append(string.Join('\t', row.Cells.Select(c => FlattenBlocks(c.Body))))
                            .Append('\n');
                    }

                    sb.Append('\n');
                    break;

                case DocImage image:
                    sb.Append(indent).Append('[').Append(image.Source.Length > 0 ? "image: " + image.Source : image.Alt)
                        .Append("]\n\n");
                    break;
            }
        }
    }

    private static string InlineText(IReadOnlyList<DocInline> inlines) =>
        string.Concat(inlines.Select(i => i.Text)).Trim();

    /// <summary>Flattens block content to a single line (for a list label / table cell).</summary>
    private static string FlattenBlocks(IReadOnlyList<DocBlock> blocks)
    {
        var parts = new List<string>();
        foreach (DocBlock block in blocks)
        {
            switch (block)
            {
                case DocParagraph p:
                    string t = InlineText(p.Inlines);
                    if (t.Length > 0)
                    {
                        parts.Add(t);
                    }

                    break;
                case DocList list:
                    parts.AddRange(list.Items.Select(i => FlattenBlocks(i.Body)));
                    break;
                case DocImage image:
                    parts.Add(image.Source.Length > 0 ? "[image: " + image.Source + "]" : "[" + image.Alt + "]");
                    break;
            }
        }

        return string.Join(' ', parts);
    }
}
