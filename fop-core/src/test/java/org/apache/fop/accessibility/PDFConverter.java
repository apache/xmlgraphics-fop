package org.apache.fop.accessibility;

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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
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
import java.util.*;

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
            // create minimal wrapper if PDF is untagged
            Element untagged = doc.createElement("UntaggedDocument");
            doc.appendChild(untagged);
            return doc;
        }

        String rootTag = getNodeTagName(structureTreeRoot);
        if (rootTag == null || rootTag.isEmpty()) {
            rootTag = "PDFStructure";
        }
        Element root = doc.createElement(rootTag);
        doc.appendChild(root);

        for (Object kid : structureTreeRoot.getKids()) {
            if (kid instanceof PDStructureElement || kid instanceof PDStructureNode) {
                appendChild(root, (PDStructureNode) kid);
            } else if (kid instanceof PDObjectReference) {
                appendChild(root, (PDObjectReference) kid);
            } else {
                Element unknown = doc.createElement("UnknownKid");
                unknown.setAttribute("class", kid == null ? "null" : kid.getClass().getName());
                root.appendChild(unknown);
            }
        }

        return doc;
    }

    private void appendChild(Element parent, PDStructureNode node) throws IOException {
        if (node == null) {
            return;
        }
        String tag = getNodeTagName(node);
        if (tag == null || tag.isEmpty()) {
            tag = "Undefined";
        }
        Element el = doc.createElement(tag);

        // If node is element, carry attributes
        if (node instanceof PDStructureElement) {
            setAttributes(el, (PDStructureElement) node);
        }
        parent.appendChild(el);

        for (Object kid : node.getKids()) {
            if (kid instanceof PDStructureElement) {
                appendChild(el, (PDStructureElement) kid);
            } else if (kid instanceof PDObjectReference) {
                appendChild(el, (PDObjectReference) kid);
            } else if (kid instanceof PDStructureNode) {
                appendChild(el, (PDStructureNode) kid);
            } else if (kid instanceof PDMarkedContentReference) {
                appendChild(el, (PDMarkedContentReference) kid);
            } else if (kid instanceof COSObjectable) {
                appendChild(el, (COSObjectable) kid);
            } else if (kid instanceof Integer) {
                appendAtomic(el, kid);
            } else {
                Element unknown = doc.createElement("UnknownChild");
                unknown.setAttribute("class", kid == null ? "null" : kid.getClass().getName());
                el.appendChild(unknown);
            }
        }
    }

    private void appendChild(Element parent, PDMarkedContentReference ref) throws IOException {
        Element contentEl = doc.createElement("content");
        List<Node> contentNodes = getContent(ref);
        if (contentNodes != null) {
            for (Node n : contentNodes) {
                contentEl.appendChild(n);
            }
        } else {
            contentEl.setAttribute("empty", "true");
        }
        parent.appendChild(contentEl);
    }

    private void appendChild(Element parent, COSObjectable obj) {
        Element el = doc.createElement("Object");
        el.setAttribute("class", obj.getClass().getName());
        parent.appendChild(el);
    }

    private void appendChild(Element parent, PDObjectReference ref) {
        try {
            Object referenced = ref.getReferencedObject();

            if (referenced == null) {
                Element missing = doc.createElement("ObjectRef");
                missing.setAttribute("status", "unresolved");
                parent.appendChild(missing);
                return;
            }

            if (referenced instanceof COSObjectable) {

                // check common types
                if (referenced instanceof PDImageXObject) {
                    PDImageXObject img = (PDImageXObject) referenced;
                    Element imgEl = doc.createElement("ImageRef");
                    imgEl.setAttribute("w", Integer.toString(img.getWidth()));
                    imgEl.setAttribute("h", Integer.toString(img.getHeight()));
                    // if possible add suffix/format
                    String suffix = img.getSuffix();
                    if (suffix != null) {
                        imgEl.setAttribute("format", suffix);
                    }
                    parent.appendChild(imgEl);
                    return;
                } else if (referenced instanceof PDFormXObject) {
                    PDFormXObject form = (PDFormXObject) referenced;
                    Element formEl = doc.createElement("FormRef");
                    formEl.setAttribute("resources", "" + (form.getResources() != null));
                    parent.appendChild(formEl);
                    return;
                } else if (referenced instanceof PDAnnotation) {
                    // Annotation (likely Link)
                    PDAnnotation annot = (PDAnnotation) referenced;
                    Element annotEl = doc.createElement("AnnotationRef");
                    annotEl.setAttribute("subtype", annot.getSubtype());
                    // If link, try to read action/URI
                    if (annot instanceof PDAnnotationLink) {
                        PDAnnotationLink link = (PDAnnotationLink) annot;
                        try {
                            if (link.getAction() != null && link.getAction().getCOSObject() instanceof COSDictionary) {
                                COSDictionary a = link.getAction().getCOSObject();
                                String uri = a.getString(COSName.URI);
                                if (uri != null) {
                                    annotEl.setAttribute("uri", uri);
                                }
                            }
                        } catch (Exception e) {
                            // ignore failures reading action
                        }
                    }
                    parent.appendChild(annotEl);
                    return;
                } else {
                    Element o = doc.createElement("ObjectRef");
                    o.setAttribute("class", ref.getClass().getName());
                    parent.appendChild(o);
                    return;
                }
            } else if (referenced instanceof COSBase) {
                COSBase base = (COSBase) referenced;
                if (base instanceof COSDictionary) {
                    COSDictionary dict = (COSDictionary) base;
                    // try to detect annotation via /Subtype
                    String subtype = dict.getNameAsString(COSName.SUBTYPE);
                    if (subtype != null) {
                        Element ann = doc.createElement("AnnotationRef");
                        ann.setAttribute("subtype", subtype);
                        // attempt to find action -> URI
                        COSBase action = dict.getItem(COSName.A);
                        if (action instanceof COSDictionary) {
                            String uri = ((COSDictionary) action).getString(COSName.URI);
                            if (uri != null) {
                                ann.setAttribute("uri", uri);
                            }
                        }
                        parent.appendChild(ann);
                        return;
                    }

                    // fallback: output info about dict
                    Element dictEl = doc.createElement("ObjectRef");
                    dictEl.setAttribute("cosType", "COSDictionary");
                    try {
                        Set<COSName> keys = dict.keySet();
                        if (!keys.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (COSName k : keys) {
                                if (sb.length() > 0) sb.append(' ');
                                sb.append(k.getName());
                            }
                            dictEl.setAttribute("keys", sb.toString());
                        }
                    } catch (Throwable t) {
                        // ignore
                    }
                    parent.appendChild(dictEl);
                } else {
                    Element baseEl = doc.createElement("ObjectRef");
                    baseEl.setAttribute("cosClass", base.getClass().getSimpleName());
                    parent.appendChild(baseEl);
                }
                return;
            }

            // final fallback
            Element fallback = doc.createElement("ObjectRef");
            fallback.setAttribute("type", referenced.getClass().getName());
            parent.appendChild(fallback);
        } catch (Exception e) {
            Element error = doc.createElement("ObjectRef");
            error.setAttribute("error", e.getClass().getSimpleName());
            error.setTextContent(e.getMessage());
            parent.appendChild(error);
        }
    }

    // get content for a PDMarkedContentReference
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
            for (PDMarkedContent content : contents) {
                ArrayList<Node> nodes = markedContentToNodes(content);
                pageMap.put(content.getMCID(), nodes);
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
                Element form = doc.createElement("form");
                nodes.add(form);
            } else if (o instanceof PDImageXObject) {
                if (textBuf.length() > 0) {
                    nodes.add(doc.createTextNode(textBuf.toString()));
                    textBuf.setLength(0);
                }
                Element image = doc.createElement("image");
                PDImageXObject im = (PDImageXObject) o;
                image.setAttribute("w", Integer.toString(im.getWidth()));
                image.setAttribute("h", Integer.toString(im.getHeight()));
                String suffix = im.getSuffix();
                if (suffix != null) image.setAttribute("format", suffix);
                nodes.add(image);
            } else {
                // unknown content type -> annotate class
                if (textBuf.length() > 0) {
                    nodes.add(doc.createTextNode(textBuf.toString()));
                    textBuf.setLength(0);
                }
                Element unknown = doc.createElement("contentObject");
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
        Element el = doc.createElement("atomic");
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
            // attribute read should not break whole conversion
            el.setAttribute("attrError", e.getClass().getSimpleName());
        }
    }

    private void setAttribute(Element el, String name, String value) {
        if (value != null) {
            el.setAttribute(name, value);
        }
    }

    // Normalize tag name: ensure starts with letter, replace invalid chars with '_'
    private String normalizeTagName(String raw) {
        if (raw == null) return "undefined";
        String s = raw.trim();
        if (s.isEmpty()) return "undefined";
        // make simple xml-name-safe: letters, digits, underscore, hyphen
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
                // fallback to StandardStructureType if available
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
        File out = new File("pdf-structure.xml");
        pc.dropDom(out);
        pdf.close();
        System.out.println("Wrote: " + out.getAbsolutePath());
    }
}