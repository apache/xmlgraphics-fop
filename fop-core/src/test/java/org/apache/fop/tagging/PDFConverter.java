package org.apache.fop.tagging;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.*;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.TextPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFConverter {

    private final PDDocument pdf;
    private final TransformerFactory transformerFactory;
    private Document doc;

    private final Map<PDPage, Map<Integer, ArrayList<Node>>> contentPageMap = new HashMap<>();

    protected PDFConverter(PDDocument pdf) {
        this.pdf = pdf;
        this.transformerFactory = TransformerFactory.newInstance();
    }

    public static PDFConverter newInstance(PDDocument pdf) {
        return new PDFConverter(pdf);
    }

    /**
     * Build a DOM Document that represents the PDF's structure tree.
     *
     * @return the DOM Document
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public Document asDom() throws ParserConfigurationException, IOException {
        if (doc != null) {
            return doc;
        }
        DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
        this.doc = dbfact.newDocumentBuilder().newDocument();

        PDStructureTreeRoot structureTreeRoot = pdf.getDocumentCatalog().getStructureTreeRoot();
        if (structureTreeRoot == null) {
            Element untagged = create("UntaggedDocument");
            doc.appendChild(untagged);
            return doc;
        }

        Element root = create("PDFTagging");
        doc.appendChild(root);

        List<?> kids = structureTreeRoot.getKids();
        if (kids != null) {
            for (Object kid : kids) {
                if (kid instanceof PDStructureNode) {
                    appendStructureNode(root, (PDStructureNode) kid);
                } else if (kid instanceof PDObjectReference) {
                    appendChild(root, (PDObjectReference) kid);
                } else {
                    Element unknown = create("UnknownKid");
                    unknown.setAttribute("class", kid == null ? "null" : kid.getClass().getName());
                    root.appendChild(unknown);
                }
            }
        }
        return doc;
    }

    private void appendStructureNode(Element parent, PDStructureNode node) throws IOException {
        if (node == null) return;

        String tag = getNodeTagName(node);
        if (tag == null || tag.isEmpty()) tag = "Undefined";
        Element el = create(tag);

        if (node instanceof PDStructureElement) {
            setAttributes(el, (PDStructureElement) node);
        }
        parent.appendChild(el);

        List<?> kids = node.getKids();
        if (kids == null) return;
        for (Object kid : kids) {
            if (kid instanceof PDStructureElement) {
                appendStructureNode(el, (PDStructureElement) kid);
            } else if (kid instanceof PDObjectReference) {
                appendChild(el, (PDObjectReference) kid);
            } else if (kid instanceof PDStructureNode) {
                appendStructureNode(el, (PDStructureNode) kid);
            } else if (kid instanceof PDMarkedContentReference) {
                appendChild(el, (PDMarkedContentReference) kid);
            } else if (kid instanceof COSObjectable) {
                appendChild(el, (COSObjectable) kid);
            } else if (kid instanceof Integer) {
                appendAtomic(el, kid);
            } else {
                Element unknown = create("UnknownChild");
                unknown.setAttribute("class", kid == null ? "null" : kid.getClass().getName());
                el.appendChild(unknown);
            }
        }
    }


    private void appendChild(Element parent, PDMarkedContentReference ref) throws IOException {
        Element contentEl = create("content");
        List<Node> nodes = getContent(ref);
        if (nodes != null) {
            for (Node n : nodes) contentEl.appendChild(n);
        } else {
            contentEl.setAttribute("empty", "true");
        }
        parent.appendChild(contentEl);
    }

    private void appendChild(Element parent, COSObjectable obj) {
        Element el = create("Object");
        el.setAttribute("class", obj.getClass().getName());
        parent.appendChild(el);
    }

    /**
     * Handle PDObjectReference: resolve referenced object and produce semantic XML node(s).
     */
    private void appendChild(Element parent, PDObjectReference ref) {
        Object referenced;
        try {
            referenced = ref.getReferencedObject();
        } catch (Exception e) {
            appendError(parent, "ref-resolution", e);
            return;
        }

        if (referenced == null) {
            parent.appendChild(simple("ObjectRef", "status", "unresolved"));
            return;
        }

        // High-level PDFBox wrappers
        if (referenced instanceof COSObjectable) {
            if (referenced instanceof PDImageXObject) {
                PDImageXObject img = (PDImageXObject) referenced;
                parent.appendChild(imageNode(img));
                return;
            }
            if (referenced instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) referenced;
                parent.appendChild(formNode(form));
                return;
            }
            if (referenced instanceof PDAnnotation) {
                PDAnnotation annot = (PDAnnotation) referenced;
                parent.appendChild(annotationNode(annot));
                return;
            }
            // fallback for other COSObjectable types
            parent.appendChild(simple("ObjectRef", "class", referenced.getClass().getName()));
            return;
        }

        // Low-level COS objects
        if (referenced instanceof COSBase) {
            COSBase base = (COSBase) referenced;
            if (base instanceof COSDictionary) {
                parent.appendChild(cosDictionaryNode((COSDictionary) base));
                return;
            }
            parent.appendChild(simple("ObjectRef", "cosClass", base.getClass().getSimpleName()));
            return;
        }

        // final fallback
        parent.appendChild(simple("ObjectRef", "type", referenced.getClass().getName()));
    }

    /* -------------------------
       Helpers: create nodes
       ------------------------- */

    private Element create(String name) {
        return doc.createElement(name);
    }

    private Element simple(String name, String attr, String val) {
        Element e = create(name);
        e.setAttribute(attr, val == null ? "" : val);
        return e;
    }

    private Element imageNode(PDImageXObject img) {
        Element el = create("ImageRef");
        el.setAttribute("width", Integer.toString(img.getWidth()));
        el.setAttribute("height", Integer.toString(img.getHeight()));
        if (img.getSuffix() != null) el.setAttribute("format", img.getSuffix());
        return el;
    }

    private Element formNode(PDFormXObject form) {
        Element el = create("FormRef");
        el.setAttribute("hasResources", Boolean.toString(form.getResources() != null));
        return el;
    }

    /* -------------------------
       Annotation handling (Link: URI + GoTo)
       ------------------------- */

    private Element annotationNode(PDAnnotation annot) {
        Element el = create("AnnotationRef");
        el.setAttribute("subtype", annot.getSubtype() == null ? "Unknown" : annot.getSubtype());

        if (annot instanceof PDAnnotationLink) {
            PDAnnotationLink link = (PDAnnotationLink) annot;

            // external URI
            try {
                if (link.getAction() instanceof PDActionURI) {
                    PDActionURI a = (PDActionURI) link.getAction();
                    if (a.getURI() != null) el.setAttribute("uri", a.getURI());
                }
            } catch (Exception ignored) {}

            // internal GoTo
            try {
                if (link.getAction() instanceof PDActionGoTo) {
                    PDActionGoTo go = (PDActionGoTo) link.getAction();
                    Element destEl = safeDestinationNode(go);
                    el.appendChild(destEl);
                }
            } catch (Exception e) {
                appendError(el, "action-destination", e);
            }
        }

        return el;
    }

    /* -------------------------
       Destination handling
       ------------------------- */

    private Element safeDestinationNode(PDActionGoTo go) {
        Element root = create("Destination");
        try {
            PDDestination dest = go.getDestination();
            if (dest == null) {
                root.setAttribute("status", "missing");
                return root;
            }
            Element resolved = destinationNode(dest);
            if (resolved != null) return resolved;
            root.setAttribute("status", "unhandled");
            return root;
        } catch (IOException io) {
            root.setAttribute("error", "IOException");
            root.setTextContent(io.getMessage());
            return root;
        } catch (Exception e) {
            root.setAttribute("error", e.getClass().getSimpleName());
            root.setTextContent(e.getMessage());
            return root;
        }
    }

    /**
     * Interpret PDDestination into XML. Handles named destinations and PDPageDestination (including XYZ).
     */
    private Element destinationNode(PDDestination dest) throws IOException {
        if (dest == null) return null;

        // Named destination
        if (dest instanceof PDNamedDestination) {
            PDNamedDestination nd = (PDNamedDestination) dest;
            Element el = create("Destination");
            el.setAttribute("type", "Named");
            if (nd.getNamedDestination() != null) el.setAttribute("name", nd.getNamedDestination());
            return el;
        }

        // Page destinations
        if (dest instanceof PDPageDestination) {
            PDPageDestination pd = (PDPageDestination) dest;
            Element el = create("Destination");
            el.setAttribute("type", pd.getClass().getSimpleName());

            int pageIndex = pd.retrievePageNumber();
            if (pageIndex >= 0) {
                el.setAttribute("page", Integer.toString(pageIndex + 1));
            } else {
                // fallback if retrievePageNumber() fails
                PDPage page = pd.getPage();
                int idx = findPageIndex(page);
                if (idx > 0) el.setAttribute("page", Integer.toString(idx));
            }
            
            if (pd instanceof PDPageXYZDestination) {
                PDPageXYZDestination xyz = (PDPageXYZDestination) pd;
                if (xyz.getLeft() != -1) el.setAttribute("left", Float.toString(safeFloat(xyz.getLeft())));
                if (xyz.getTop() != -1) el.setAttribute("top", Float.toString(safeFloat(xyz.getTop())));
                if (xyz.getZoom() != -1) el.setAttribute("zoom", Float.toString(safeFloat(xyz.getZoom())));
            }
            return el;
        }

        // fallback: unknown destination type
        Element el = create("Destination");
        el.setAttribute("type", "Unknown");
        el.setAttribute("class", dest.getClass().getName());
        return el;
    }

    // Try to find page index (1-based) for a PDPage
    private int findPageIndex(PDPage page) {
        if (page == null) return -1;
        int idx = 0;
        for (PDPage p : pdf.getPages()) {
            if (p == page) return idx + 1;
            idx++;
        }
        return -1;
    }

    /* -------------------------
       COSDictionary node creation
       ------------------------- */

    private Element cosDictionaryNode(COSDictionary dict) {
        Element el = create("ObjectRef");
        el.setAttribute("cosType", "COSDictionary");
        String subtype = dict.getNameAsString(COSName.SUBTYPE);
        if (subtype != null) el.setAttribute("subtype", subtype);

        COSBase action = dict.getItem(COSName.A);
        if (action instanceof COSDictionary) {
            COSDictionary aDict = (COSDictionary) action;
            String uri = aDict.getString(COSName.URI);
            if (uri != null) el.setAttribute("uri", uri);
            String s = aDict.getNameAsString(COSName.S);
            if ("GoTo".equals(s)) el.setAttribute("internal", "true");
        }

        StringBuilder sb = new StringBuilder();
        try {
            for (COSName k : dict.keySet()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(k.getName());
            }
        } catch (Exception ignored) {}
        el.setAttribute("keys", sb.toString());
        return el;
    }

    private void appendError(Element parent, String where, Exception e) {
        Element err = create("ObjectRef");
        err.setAttribute("error", where);
        if (e != null && e.getMessage() != null) err.setTextContent(e.getMessage());
        parent.appendChild(err);
    }

    /* -------------------------
       Marked content extraction
       ------------------------- */

    private ArrayList<Node> getContent(PDMarkedContentReference ref) throws IOException {
        PDPage page = ref.getPage();
        if (page == null) {
            return null;
        }
        Map<Integer, ArrayList<Node>> pageMap = contentPageMap.get(page);
        if (pageMap == null) {
            PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
            extractor.processPage(page);
            List<PDMarkedContent> contents = extractor.getMarkedContents();
            pageMap = new HashMap<>();
            for (PDMarkedContent c : contents) {
                ArrayList<Node> nodes = markedContentToNodes(c);
                pageMap.put(c.getMCID(), nodes);
            }
            contentPageMap.put(page, pageMap);
        }
        return pageMap.get(ref.getMCID());
    }

    private ArrayList<Node> markedContentToNodes(PDMarkedContent content) {
        ArrayList<Node> nodes = new ArrayList<>();
        StringBuilder textBuf = new StringBuilder();
        for (Object o : content.getContents()) {
            if (o instanceof TextPosition) {
                TextPosition tp = (TextPosition) o;
                textBuf.append(tp);
            } else if (o instanceof PDMarkedContent) {
                nodes.addAll(markedContentToNodes((PDMarkedContent) o));
            } else if (o instanceof PDFormXObject) {
                if (textBuf.length() > 0) {
                    nodes.add(doc.createTextNode(textBuf.toString()));
                    textBuf.setLength(0);
                }
                nodes.add(create("form"));
            } else if (o instanceof PDImageXObject) {
                if (textBuf.length() > 0) {
                    nodes.add(doc.createTextNode(textBuf.toString()));
                    textBuf.setLength(0);
                }
                PDImageXObject im = (PDImageXObject) o;
                Element image = create("image");
                image.setAttribute("w", Integer.toString(im.getWidth()));
                image.setAttribute("h", Integer.toString(im.getHeight()));
                if (im.getSuffix() != null) image.setAttribute("format", im.getSuffix());
                nodes.add(image);
            } else {
                if (textBuf.length() > 0) {
                    nodes.add(doc.createTextNode(textBuf.toString()));
                    textBuf.setLength(0);
                }
                Element unknown = create("contentObject");
                unknown.setAttribute("class", o == null ? "null" : o.getClass().getName());
                nodes.add(unknown);
            }
        }
        if (textBuf.length() > 0) {
            nodes.add(doc.createTextNode(textBuf.toString()));
        }
        return nodes;
    }

    // small helper for atomic MCID ints
    private void appendAtomic(Element parent, Object atomic) {
        Element el = create("atomic");
        el.setAttribute("type", atomic == null ? "null" : atomic.getClass().getName());
        el.setTextContent(String.valueOf(atomic));
        parent.appendChild(el);
    }

    // set attributes from PDStructureElement onto XML element
    private void setAttributes(Element el, PDStructureElement pdsElement) {
        try {
            setAttribute(el, "elementIdentifier", pdsElement.getElementIdentifier());
            setAttribute(el, "language", pdsElement.getLanguage());
            setAttribute(el, "actualText", pdsElement.getActualText());
            setAttribute(el, "alternateDescription", pdsElement.getAlternateDescription());
            setAttribute(el, "expandedForm", pdsElement.getExpandedForm());
            setAttribute(el, "title", pdsElement.getTitle());

            String standardST = pdsElement.getStandardStructureType();
            String structType = pdsElement.getStructureType();

            if (standardST != null && !standardST.equals(el.getNodeName())) {
                setAttribute(el, "StandardStructureType", standardST);
            }
            if (structType != null && !structType.equals(standardST)) {
                setAttribute(el, "StructureType", structType);
            }

            Revisions<PDAttributeObject> tagAttr = pdsElement.getAttributes();
            for (int i = 0; i < tagAttr.size(); i++) {
                PDAttributeObject tAttr = tagAttr.getObject(i);
                if (tAttr instanceof PDTableAttributeObject) {
                    PDTableAttributeObject tableAttr = (PDTableAttributeObject) tAttr;
                    setAttribute(el, "ColSpan", tableAttr.getColSpan() > 1 ? Integer.toString(tableAttr.getColSpan()) : null);
                    setAttribute(el, "RowSpan", tableAttr.getRowSpan() > 1 ? Integer.toString(tableAttr.getRowSpan()) : null);
                    setAttribute(el, "Scope", tableAttr.getScope());
                }
            }
        } catch (Exception e) {
            el.setAttribute("attrError", e.getClass().getSimpleName());
        }
    }

    private void setAttribute(Element el, String name, String value) {
        if (value != null) {
            el.setAttribute(name, value);
        }
    }

    // Normalize tag name: ensure safe characters for XML element names
    private String normalizeTagName(String raw) {
        if (raw == null) return "undefined";
        String s = raw.trim();
        if (s.isEmpty()) return "undefined";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    // Try to extract a tag name for PDStructureNode (S entry in COS)
    private String getNodeTagName(PDStructureNode node) {
        try {
            if (node == null) return null;
            COSDictionary cos = node.getCOSObject();
            if (cos == null) return null;
            String s = cos.getNameAsString(COSName.S);
            if (s == null) {
                if (node instanceof PDStructureElement) {
                    PDStructureElement se = (PDStructureElement) node;
                    String std = se.getStandardStructureType();
                    if (std != null) return normalizeTagName(std);
                }
                return null;
            }
            return normalizeTagName(s);
        } catch (Exception e) {
            return "undefined";
        }
    }

    private float safeFloat(float f) {
        return Float.isNaN(f) ? 0f : f;
    }

    /**
     * Write the DOM to a file.
     */
    public void dropDom(File file) throws TransformerException, ParserConfigurationException, IOException {
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        Document d = this.asDom();
        Result output = new StreamResult(file);
        transformer.transform(new DOMSource(d), output);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: PDFConverter <pdf-resource-or-file>");
            System.exit(1);
        }
        String source = args[0];
        PDDocument pdf;

        InputStream in = PDFConverter.class.getResourceAsStream("/" + source);
        if (in == null) {
            System.err.println("Resource not found: " + source);
            return;
        }
        pdf = PDDocument.load(in);


        PDFConverter pc = PDFConverter.newInstance(pdf);
        File out = new File(Paths.get(source).getFileName() + ".xml");
        pc.dropDom(out);
        pdf.close();
        System.out.println("Wrote: " + out.getAbsolutePath());
    }


}