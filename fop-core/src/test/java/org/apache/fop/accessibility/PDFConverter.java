package org.apache.fop.accessibility;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.*;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.TextPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PDFConverter {

    private final PDDocument pdf;

    private final SAXTransformerFactory transformerFactory;
    private Document doc;

    protected PDFConverter(PDDocument pdf) {

        this.pdf = pdf;
        transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
    }

    public Document asDom() throws ParserConfigurationException, IOException {
        if (doc != null) {
            return doc;
        }
        DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
        doc = dbfact.newDocumentBuilder().newDocument();

        PDStructureTreeRoot pdStructureTreeRoot = pdf.getDocumentCatalog().getStructureTreeRoot();
        appendChild(pdStructureTreeRoot);

        return doc;
    }

    private void appendChild(PDStructureTreeRoot pdfRoot) throws IOException {

        String rootTag = pdfRoot.getCOSObject().getNameAsString(COSName.S);
        if (rootTag == null) {
            rootTag = "Document";
        }

        Element el = doc.createElement(rootTag);
        doc.appendChild(el);

        for (Object kid : pdfRoot.getKids()) {
            if (kid instanceof PDStructureElement) {
                appendChild(el, (PDStructureElement) kid);
            } else {
                throw new IllegalStateException("Bad class" + kid.getClass().getName());
            }
        }
    }


    private void appendChild(Element parent, PDStructureNode pdsElement) throws IOException {
        Element el = doc.createElement(getTag(pdsElement));
        if (pdsElement instanceof PDStructureElement) {
            setAttributes(el, (PDStructureElement) pdsElement);
        }
        parent.appendChild(el);

        for (Object kid :
                pdsElement.getKids()) {
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
                appendAttomic(el, kid);
            } else {
                throw new IllegalStateException("Bad class " + kid.getClass().getName());
            }
        }
    }

    private String getTag(PDStructureNode node) {
        String tagName = node.getCOSObject().getNameAsString(COSName.S);
        return tagName == null ? "undefined" : tagName;
    }

    private final HashMap<PDPage, HashMap<Integer, ArrayList<Node>>> contentPageMap = new HashMap<>();

    private ArrayList<Node> markedContentToString(PDMarkedContent content) {
        ArrayList<Node> nodes = new ArrayList<>();
        String text = "";
        for (Object cObj :
                content.getContents()) {
            if (cObj instanceof TextPosition) {
                TextPosition textPos = (TextPosition) cObj;
                text += textPos.toString();
            } else if (cObj instanceof PDMarkedContent) {
                PDMarkedContent markedContent = (PDMarkedContent) cObj;
                nodes.addAll(markedContentToString(markedContent));
            } else if (cObj instanceof PDFormXObject) {
                if (!"".equals(text))
                    nodes.add(doc.createTextNode(text));
                Element el = doc.createElement("form");
                nodes.add(el);
                text = "";
            } else if (cObj instanceof PDImageXObject) {
                if (!"".equals(text))
                    nodes.add(doc.createTextNode(text));
                Element el = doc.createElement("image");
                nodes.add(el);
                text = "";
            } else {

                throw new IllegalStateException("Bad class " + cObj.getClass().getName());
            }
        }
        if (!"".equals(text))
            nodes.add(doc.createTextNode(text));
        return nodes;
    }

    private ArrayList<Node> getContent(PDMarkedContentReference ref) throws IOException {
        final PDPage page = ref.getPage();
        if (!contentPageMap.containsKey(page)) {
            PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();

            extractor.processPage(ref.getPage());
            List<PDMarkedContent> contents = extractor.getMarkedContents();

            HashMap<Integer, ArrayList<Node>> contentMap = new HashMap<>();
            for (PDMarkedContent content :
                    contents) {
                contentMap.put(content.getMCID(), markedContentToString(content));
            }

            contentPageMap.put(page, contentMap);
        }
        return contentPageMap.get(page).get(ref.getMCID());


    }

    private Document appendChild(Element parent, PDMarkedContentReference ref) throws IOException {
        Element el = doc.createElement("content");

        ArrayList<Node> content = getContent(ref);

//        if(content.getAlternateDescription() != null){
//            el.setAttribute("alt", content.getAlternateDescription());
//        }
        for (Node child :
                content) {
            el.appendChild(child);
        }
//        el.setAttribute("page", "" + ref.getPage().getMetadata().);
        parent.appendChild(el);
        return doc;
    }

    private Document appendChild(Element parent, COSObjectable obj) {
        Element el = doc.createElement("unknwonObject");
        el.setAttribute("class", obj.getClass().getName());
        parent.appendChild(el);
        return doc;
    }

    private Document appendAttomic(Element parent, Object atomic) {
        Element el = doc.createElement("atomic");
        parent.appendChild(el);
        el.setAttribute("type", atomic.getClass().getName());
        el.setTextContent(atomic.toString());
        return doc;
    }


    private void setAttributes(Element el, PDStructureElement pdsElement) {
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
                setAttribute(el, "ColSpan", tableAttr.getColSpan() > 1 ? "" + tableAttr.getColSpan() : null);
                setAttribute(el, "RowSpan", tableAttr.getRowSpan() > 1 ? "" + tableAttr.getRowSpan() : null);
                setAttribute(el, "Scope", tableAttr.getScope());
//                setAttribute(el, "Headers", tableAttr.getHeaders() == null ? null :StringUtils.join(Arrays.asList(tableAttr.getHeaders()), " "));

            }
        }

    }

    private void setAttribute(Element el, String name, String value) {
        if (value != null) {
            el.setAttribute(name, value);
        }
    }

    public static PDFConverter newInstance(PDDocument pdf) {
        return new PDFConverter(pdf);
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {
        String testfile = "toc.long.properties_toc.long";

        InputStream inputStream = PDFConverter.class.getResourceAsStream("/" + testfile + ".pdf");
        PDDocument pdf = PDDocument.load(inputStream);
        PDFConverter pc = new PDFConverter(pdf);

//        int i = 1;
//        for(PDPage page : pdf.getPages()){
//            System.out.println(i++);
//            for (PDAnnotation anno:
//                 page.getAnnotations()) {
//                System.out.println(anno.getSubtype());
//            }
//        }


        pc.dropDom(new File(testfile + ".xml"));
        pdf.close();
    }


    public void dropDom(File file) throws TransformerException, ParserConfigurationException, IOException {
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        Result output = new StreamResult(file);
        transformer.transform(new DOMSource(this.asDom()), output);
    }


}
