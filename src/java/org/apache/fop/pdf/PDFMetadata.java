/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.Date;
import java.util.UUID;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSerializer;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFUAAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFUAXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFVTAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFVTXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFXAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFXXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.XAPMMAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.XAPMMXMPSchema;

/**
 * Special PDFStream for Metadata.
 * @since PDF 1.4
 */
public class PDFMetadata extends PDFStream {

    private Metadata xmpMetadata;
    private boolean readOnly = true;

    /**
     * @param xmp xmp metadata
     * @param readOnly true if read only
     * @see org.apache.fop.pdf.PDFObject#PDFObject()
     */
    public PDFMetadata(Metadata xmp, boolean readOnly) {
        super();
        if (xmp == null) {
            throw new NullPointerException(
                    "The parameter for the XMP Document must not be null");
        }
        this.xmpMetadata = xmp;
        this.readOnly = readOnly;
    }

    /** {@inheritDoc} */
    protected String getDefaultFilterName() {
        return PDFFilterList.METADATA_FILTER;
    }

    /**
     * @return the XMP metadata
     */
    public Metadata getMetadata() {
        return this.xmpMetadata;
    }

    /**
     * overload the base object method so we don't have to copy
     * byte arrays around so much
     * {@inheritDoc}
     */
    public int output(java.io.OutputStream stream)
                throws java.io.IOException {
        int length = super.output(stream);
        this.xmpMetadata = null; //Release DOM when it's not used anymore
        return length;
    }

    /** {@inheritDoc} */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        try {
            XMPSerializer.writeXMPPacket(xmpMetadata, out, this.readOnly);
        } catch (TransformerConfigurationException tce) {
            throw new IOException("Error setting up Transformer for XMP stream serialization: "
                    + tce.getMessage());
        } catch (SAXException saxe) {
            throw new IOException("Error while serializing XMP stream: "
                    + saxe.getMessage());
        }
    }

    /** {@inheritDoc} */
    protected void populateStreamDict(Object lengthEntry) {
        final String filterEntry = getFilterList().buildFilterDictEntries();
        if (getDocumentSafely().getProfile().getPDFAMode().isPart1()
                && filterEntry != null && filterEntry.length() > 0) {
            throw new PDFConformanceException(
                    "The Filter key is prohibited when PDF/A-1 is active");
        }
        put("Type", new PDFName("Metadata"));
        put("Subtype", new PDFName("XML"));
        super.populateStreamDict(lengthEntry);
    }

    /**
     * Creates an XMP document based on the settings on the PDF Document.
     * @param pdfDoc the PDF Document
     * @return the requested XMP metadata
     */
    public static Metadata createXMPFromPDFDocument(PDFDocument pdfDoc) {
        Metadata meta = new Metadata();

        PDFInfo info = pdfDoc.getInfo();
        PDFRoot root = pdfDoc.getRoot();

        //Set creation date if not available, yet
        if (info.getCreationDate() == null) {
            Date d = new Date();
            info.setCreationDate(d);
        }

        //Important: Acrobat 7's preflight check for PDF/A-1b wants the creation date in the Info
        //object and in the XMP metadata to have the same timezone or else it shows a validation
        //error even if the times are essentially equal.

        //Dublin Core
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(meta);
        if (info.getAuthor() != null) {
            dc.addCreator(info.getAuthor());
        }
        if (info.getTitle() != null) {
            dc.setTitle(info.getTitle());
        }
        if (info.getSubject() != null) {
            //Subject maps to dc:description["x-default"] as per ISO-19005-1:2005/Cor.1:2007
            dc.setDescription(null, info.getSubject());
        }
        if (root.getLanguage() != null) {
            //Note: No check is performed to make sure the value is valid RFC 3066!
            dc.addLanguage(root.getLanguage());
        }
        dc.addDate(info.getCreationDate());

        //Somewhat redundant but some PDF/A checkers issue a warning without this.
        dc.setFormat("application/pdf");

        PDFUAMode pdfuaMode = pdfDoc.getProfile().getPDFUAMode();
        if (pdfuaMode.isEnabled()) {
            PDFUAAdapter pdfua = PDFUAXMPSchema.getAdapter(meta);
            pdfua.setPart(pdfuaMode.getPart());
        }

        //PDF/A identification
        PDFAMode pdfaMode = pdfDoc.getProfile().getPDFAMode();
        if (pdfaMode.isEnabled()) {
            PDFAAdapter pdfa = PDFAXMPSchema.getAdapter(meta);
            pdfa.setPart(pdfaMode.getPart());
            pdfa.setConformance(String.valueOf(pdfaMode.getConformanceLevel()));
        }
        AdobePDFAdapter adobePDF = AdobePDFSchema.getAdapter(meta);
        PDFXMode pdfxMode = pdfDoc.getProfile().getPDFXMode();
        if (pdfxMode != PDFXMode.DISABLED) {
            PDFXAdapter pdfx = PDFXXMPSchema.getAdapter(meta);
            pdfx.setVersion(pdfxMode.getName());

            XAPMMAdapter xapmm = XAPMMXMPSchema.getAdapter(meta);
            xapmm.setVersion("1");
            xapmm.setDocumentID("uuid:" + UUID.randomUUID().toString());
            xapmm.setInstanceID("uuid:" + UUID.randomUUID().toString());
            xapmm.setRenditionClass("default");
            adobePDF.setTrapped("False");
        }
        PDFProfile profile = pdfDoc.getProfile();
        PDFVTMode pdfvtMode = profile.getPDFVTMode();
        if (pdfvtMode != PDFVTMode.DISABLED) {
            PDFVTAdapter pdfvt = PDFVTXMPSchema.getAdapter(meta);
            pdfvt.setVersion("PDF/VT-1");
            if (info.getModDate() != null) {
                pdfvt.setModifyDate(info.getModDate());
            } else if (profile.isModDateRequired()) {
                //if modify date is needed but none is in the Info object, use creation date
                pdfvt.setModifyDate(info.getCreationDate());
            }
        }

        //XMP Basic Schema
        XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(meta);
        xmpBasic.setCreateDate(info.getCreationDate());
        if (info.getModDate() != null) {
            xmpBasic.setModifyDate(info.getModDate());
        } else if (profile.isModDateRequired()) {
            //if modify date is needed but none is in the Info object, use creation date
            xmpBasic.setModifyDate(info.getCreationDate());
        }
        if (info.getCreator() != null) {
            xmpBasic.setCreatorTool(info.getCreator());
        }


        if (info.getKeywords() != null) {
            adobePDF.setKeywords(info.getKeywords());
        }
        if (info.getProducer() != null) {
            adobePDF.setProducer(info.getProducer());
        }
        adobePDF.setPDFVersion(pdfDoc.getPDFVersionString());


        return meta;
    }

    /**
     * Updates the values in the Info object from the XMP metadata according to the rules defined
     * in PDF/A-1 (ISO 19005-1:2005)
     * @param meta the metadata
     * @param info the Info object
     */
    public static void updateInfoFromMetadata(Metadata meta, PDFInfo info) {
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(meta);
        info.setTitle(dc.getTitle());
        String[] creators = dc.getCreators();
        if (creators != null && creators.length > 0) {
            info.setAuthor(creators[0]);
        } else {
            info.setAuthor(null);
        }

        //dc:description["x-default"] maps to Subject as per ISO-19005-1:2005/Cor.1:2007
        info.setSubject(dc.getDescription());

        AdobePDFAdapter pdf = AdobePDFSchema.getAdapter(meta);
        info.setKeywords(pdf.getKeywords());
        info.setProducer(pdf.getProducer());

        XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(meta);
        info.setCreator(xmpBasic.getCreatorTool());
        Date d;
        d = xmpBasic.getCreateDate();
        xmpBasic.setCreateDate(d); //To make Adobe Acrobat happy (bug filed with Adobe)
        //Adobe Acrobat doesn't like it when the xmp:CreateDate has a different timezone
        //than Info/CreationDate
        info.setCreationDate(d);
        d = xmpBasic.getModifyDate();
        if (d != null) { //ModifyDate is only required for PDF/X
            xmpBasic.setModifyDate(d);
            info.setModDate(d);
        }
    }
}
