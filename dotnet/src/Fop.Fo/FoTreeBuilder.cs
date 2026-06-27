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
using System.Xml;

namespace Fop.Fo;

/// <summary>
/// Builds a formatting-object tree from an XSL-FO document using a streaming
/// <see cref="XmlReader"/>. Port of the role of <c>org.apache.fop.fo.FOTreeBuilder</c>.
/// </summary>
public static class FoTreeBuilder
{
    /// <summary>The XSL-FO namespace URI.</summary>
    public const string FoNamespace = "http://www.w3.org/1999/XSL/Format";

    /// <summary>Parses an FO document from a stream and returns its <see cref="FoRoot"/>.</summary>
    public static FoRoot Parse(Stream input)
    {
        ArgumentNullException.ThrowIfNull(input);
        var settings = new XmlReaderSettings
        {
            DtdProcessing = DtdProcessing.Prohibit,
            IgnoreComments = true,
            IgnoreProcessingInstructions = true,
        };
        using var reader = XmlReader.Create(input, settings);
        return Parse(reader);
    }

    /// <summary>Parses an FO document from a string.</summary>
    public static FoRoot ParseString(string xml)
    {
        ArgumentNullException.ThrowIfNull(xml);
        using var stream = new MemoryStream(Encoding.UTF8.GetBytes(xml));
        return Parse(stream);
    }

    /// <summary>Parses an FO document from an <see cref="XmlReader"/>.</summary>
    public static FoRoot Parse(XmlReader reader)
    {
        ArgumentNullException.ThrowIfNull(reader);

        FObj? root = null;
        var stack = new Stack<FObj>();
        var textBuffer = new StringBuilder();

        void FlushText()
        {
            if (stack.Count == 0 || textBuffer.Length == 0)
            {
                textBuffer.Clear();
                return;
            }

            FObj current = stack.Peek();
            string raw = textBuffer.ToString();
            textBuffer.Clear();

            if (IsContentContainer(current))
            {
                string collapsed = CollapseWhitespace(raw);
                if (collapsed.Length > 0)
                {
                    current.AddChild(new FOText(collapsed));
                }
            }
            // Whitespace between structural elements (root/flow/etc.) is ignored.
        }

        while (reader.Read())
        {
            switch (reader.NodeType)
            {
                case XmlNodeType.Element:
                {
                    FlushText();
                    bool isEmpty = reader.IsEmptyElement;
                    if (reader.NamespaceURI == FoNamespace)
                    {
                        var attributes = ReadAttributes(reader);
                        FObj? parent = stack.Count > 0 ? stack.Peek() : null;
                        var properties = new PropertyList(attributes, parent?.Properties);
                        FObj obj = Create(reader.LocalName, properties);

                        parent?.AddChild(obj);
                        root ??= obj;

                        if (!isEmpty)
                        {
                            stack.Push(obj);
                        }
                    }
                    else if (!isEmpty)
                    {
                        // Non-FO element (e.g. SVG): push a generic placeholder to keep depth balanced.
                        FObj? parent = stack.Count > 0 ? stack.Peek() : null;
                        var properties = new PropertyList(ReadAttributes(reader), parent?.Properties);
                        var obj = new FoGeneric(properties, reader.LocalName);
                        parent?.AddChild(obj);
                        root ??= obj;
                        stack.Push(obj);
                    }

                    break;
                }

                case XmlNodeType.EndElement:
                    FlushText();
                    if (stack.Count > 0)
                    {
                        stack.Pop();
                    }

                    break;

                case XmlNodeType.Text:
                case XmlNodeType.CDATA:
                case XmlNodeType.SignificantWhitespace:
                case XmlNodeType.Whitespace:
                    textBuffer.Append(reader.Value);
                    break;
            }
        }

        return root as FoRoot
            ?? throw new InvalidOperationException("Document does not contain an fo:root element.");
    }

    private static Dictionary<string, string> ReadAttributes(XmlReader reader)
    {
        var attributes = new Dictionary<string, string>(StringComparer.Ordinal);
        if (reader.MoveToFirstAttribute())
        {
            do
            {
                if (reader.Prefix == "xmlns" || reader.Name == "xmlns")
                {
                    continue;
                }

                attributes[reader.LocalName] = reader.Value;
            }
            while (reader.MoveToNextAttribute());

            reader.MoveToElement();
        }

        return attributes;
    }

    private static FObj Create(string localName, PropertyList properties) => localName switch
    {
        "root" => new FoRoot(properties),
        "layout-master-set" => new FoLayoutMasterSet(properties),
        "simple-page-master" => new FoSimplePageMaster(properties),
        "region-body" => new FoRegionBody(properties),
        "region-before" => new FoRegionBefore(properties),
        "region-after" => new FoRegionAfter(properties),
        "region-start" => new FoRegionStart(properties),
        "region-end" => new FoRegionEnd(properties),
        "page-sequence" => new FoPageSequence(properties),
        "flow" => new FoFlow(properties),
        "static-content" => new FoStaticContent(properties),
        "block" => new FoBlock(properties),
        "inline" => new FoInline(properties),
        "basic-link" => new FoBasicLink(properties),
        "leader" => new FoLeader(properties),
        "marker" => new FoMarker(properties),
        "retrieve-marker" => new FoRetrieveMarker(properties),
        "page-number" => new FoPageNumber(properties),
        "page-number-citation" => new FoPageNumberCitation(properties),
        "page-number-citation-last" => new FoPageNumberCitationLast(properties),
        "footnote" => new FoFootnote(properties),
        "footnote-body" => new FoFootnoteBody(properties),
        "external-graphic" => new FoExternalGraphic(properties),
        "table" => new FoTable(properties),
        "table-column" => new FoTableColumn(properties),
        "table-header" => new FoTableHeader(properties),
        "table-body" => new FoTableBody(properties),
        "table-footer" => new FoTableFooter(properties),
        "table-row" => new FoTableRow(properties),
        "table-cell" => new FoTableCell(properties),
        "list-block" => new FoListBlock(properties),
        "list-item" => new FoListItem(properties),
        "list-item-label" => new FoListItemLabel(properties),
        "list-item-body" => new FoListItemBody(properties),
        _ => new FoGeneric(properties, localName),
    };

    private static bool IsContentContainer(FObj obj) =>
        obj is FoBlock or FoInline or FoBasicLink or FoGeneric or FoMarker;

    /// <summary>Collapses runs of XSL-FO whitespace to a single space (the default behaviour).</summary>
    public static string CollapseWhitespace(string text)
    {
        var sb = new StringBuilder(text.Length);
        bool lastWasSpace = false;
        foreach (char c in text)
        {
            bool isSpace = c is ' ' or '\t' or '\r' or '\n';
            if (isSpace)
            {
                if (!lastWasSpace)
                {
                    sb.Append(' ');
                }

                lastWasSpace = true;
            }
            else
            {
                sb.Append(c);
                lastWasSpace = false;
            }
        }

        return sb.ToString();
    }
}
