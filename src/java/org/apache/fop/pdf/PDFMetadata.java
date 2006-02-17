/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.extensions.xmp.XMPConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Special PDFStream for Metadata.
 * @since PDF 1.4
 */
public class PDFMetadata extends PDFStream {
    
    private Document xmpMetadata;
    private boolean readOnly = true;

    /** @see org.apache.fop.pdf.PDFObject#PDFObject() */
    public PDFMetadata(Document xmp, boolean readOnly) {
        super();
        if (xmp == null) {
            throw new NullPointerException(
                    "DOM Document representing the metadata must no be null");
        }
        this.xmpMetadata = xmp;
        this.readOnly = readOnly;
    }

    /** @see org.apache.fop.pdf.AbstractPDFStream#setupFilterList() */
    protected void setupFilterList() {
        if (!getFilterList().isInitialized()) {
            getFilterList().addDefaultFilters(
                getDocumentSafely().getFilterMap(), 
                PDFFilterList.METADATA_FILTER);
        }
        super.setupFilterList();
    }

    /** @see org.apache.fop.pdf.AbstractPDFStream#allowEncryption() */
    protected boolean allowEncryption() {
        return false; //XMP metadata packet must be scannable by non PDF-compatible readers
    }

    /**
     * overload the base object method so we don't have to copy
     * byte arrays around so much
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(java.io.OutputStream stream)
                throws java.io.IOException {
        int length = super.output(stream);
        this.xmpMetadata = null; //Release DOM when it's not used anymore
        return length;
    }
    
    /** @see org.apache.fop.pdf.AbstractPDFStream#outputRawStreamData(java.io.OutputStream) */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        final String encoding = "UTF-8";
        out.write("<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
                .getBytes(encoding));
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            DOMSource src = new DOMSource(this.xmpMetadata);
            StreamResult res = new StreamResult(out);
            transformer.transform(src, res);
        } catch (TransformerConfigurationException e) {
            throw new IOException("Error setting up Transformer for XMP stream serialization: " 
                    + e.getMessage());
        } catch (TransformerException e) {
            throw new IOException("Error while serializing XMP stream: " 
                    + e.getMessage());
        }
        if (readOnly) {
            out.write("\n<?xpacket end=\"r\"?>".getBytes(encoding));
        } else {
            //Create padding string (40 * 101 characters is more or less the recommended 4KB)
            StringBuffer sb = new StringBuffer(101);
            sb.append('\n');
            for (int i = 0; i < 100; i++) {
                sb.append(" ");
            }
            byte[] padding = sb.toString().getBytes(encoding);
            for (int i = 0; i < 40; i++) {
                out.write(padding);
            }
            out.write("\n<?xpacket end=\"w\"?>".getBytes(encoding));
        }
    }
    
    /** @see org.apache.fop.pdf.AbstractPDFStream#buildStreamDict(String) */
    protected String buildStreamDict(String lengthEntry) {
        final String filterEntry = getFilterList().buildFilterDictEntries();
        final StringBuffer sb = new StringBuffer(128);
        sb.append(getObjectID());
        sb.append("<< ");
        sb.append("/Type /Metadata");
        sb.append("\n/Subtype /XML");
        sb.append("\n/Length " + lengthEntry);
        sb.append("\n" + filterEntry);
        sb.append("\n>>\n");
        return sb.toString();
    }

    /**
     * Creates an XMP document based on the settings on the PDF Document.
     * @param pdfDoc the PDF Document
     * @return a DOM document representing the requested XMP metadata
     */
    public static Document createXMPFromUserAgent(PDFDocument pdfDoc) {
        DOMImplementation domImplementation = ElementMapping.getDefaultDOMImplementation();
        Document doc = domImplementation.createDocument(
                XMPConstants.XMP_NAMESPACE, "x:xmpmeta", null);
        Element rdf = doc.createElementNS(XMPConstants.RDF_NAMESPACE, "rdf:RDF");
        doc.getDocumentElement().appendChild(rdf);
        
        Element desc, el;
        PDFInfo info = pdfDoc.getInfo();
        DateFormat pseudoISO8601DateFormat = new SimpleDateFormat(
            "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
        
        //Set creation date if not available, yet
        if (info.getCreationDate() == null) {
            Date d = new Date();
            info.setCreationDate(d);
        }

        //Dublin Core
        desc = doc.createElementNS(XMPConstants.RDF_NAMESPACE, "rdf:Description");
        desc.setAttribute("about", "");
        rdf.appendChild(desc);
        if (info.getAuthor() != null) {
            el = doc.createElementNS(XMPConstants.DUBLIN_CORE_NAMESPACE, "dc:creator");
            desc.appendChild(el);
            el.appendChild(doc.createTextNode(info.getAuthor()));
        }
        if (info.getTitle() != null) {
            el = doc.createElementNS(XMPConstants.DUBLIN_CORE_NAMESPACE, "dc:title");
            desc.appendChild(el);
            el.appendChild(doc.createTextNode(info.getTitle()));
        }
        if (info.getSubject() != null) {
            el = doc.createElementNS(XMPConstants.DUBLIN_CORE_NAMESPACE, "dc:subject");
            desc.appendChild(el);
            el.appendChild(doc.createTextNode(info.getSubject()));
        }
        el = doc.createElementNS(XMPConstants.DUBLIN_CORE_NAMESPACE, "dc:date");
        desc.appendChild(el);
        el.appendChild(doc.createTextNode(pseudoISO8601DateFormat.format(info.getCreationDate())));
        
        //XMP Basic Schema
        desc = doc.createElementNS(XMPConstants.RDF_NAMESPACE, "rdf:Description");
        desc.setAttribute("about", "");
        rdf.appendChild(desc);
        el = doc.createElementNS(XMPConstants.XMP_BASIC_NAMESPACE, "xmp:createDate");
        desc.appendChild(el);
        el.appendChild(doc.createTextNode(pseudoISO8601DateFormat.format(info.getCreationDate())));
        if (info.getCreator() != null) {
            el = doc.createElementNS(XMPConstants.XMP_BASIC_NAMESPACE, "xmp:creatorTool");
            desc.appendChild(el);
            el.appendChild(doc.createTextNode(info.getCreator()));
        }
        
        
        return doc;
    }
    
    
}
