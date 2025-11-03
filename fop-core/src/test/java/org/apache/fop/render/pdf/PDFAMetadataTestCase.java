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

package org.apache.fop.render.pdf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPArray;
import org.apache.xmlgraphics.xmp.XMPArrayType;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPProperty;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;

import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFMetadata;
import org.apache.fop.pdf.PDFUAMode;

/**
 * Test case for PDF/A metadata handling.
 */
public class PDFAMetadataTestCase {

    @Test
    public void testInfoUpdate() throws Exception {
        Metadata meta = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(meta);
        dc.setTitle("MyTitle");
        dc.setDescription(null, "MySubject");
        dc.addCreator("That's me");

        AdobePDFAdapter pdf = AdobePDFSchema.getAdapter(meta);
        pdf.setKeywords("XSL-FO XML");
        pdf.setProducer("SuperFOP");

        XMPBasicAdapter xmp = XMPBasicSchema.getAdapter(meta);
        xmp.setCreatorTool("WonderFOP");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
        cal1.set(2007, Calendar.JUNE, 5, 21, 49, 13);
        cal1.set(Calendar.MILLISECOND, 0);
        xmp.setCreateDate(cal1.getTime());
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
        cal2.set(2007, Calendar.JUNE, 6, 8, 15, 59);
        cal2.set(Calendar.MILLISECOND, 0);
        xmp.setModifyDate(cal2.getTime());

        PDFInfo info = new PDFInfo();
        assertNull(info.getTitle());
        PDFMetadata.updateInfoFromMetadata(meta, info);

        assertEquals("MyTitle", info.getTitle());
        assertEquals("MySubject", info.getSubject());
        assertEquals("That's me", info.getAuthor());
        assertEquals("XSL-FO XML", info.getKeywords());
        assertEquals("SuperFOP", info.getProducer());
        assertEquals("WonderFOP", info.getCreator());
        assertEquals(cal1.getTime(), info.getCreationDate());
        assertEquals(cal2.getTime(), info.getModDate());
    }

    @Test
    public void testXMPUpdate() throws Exception {
        PDFDocument doc = new PDFDocument("SuperFOP");
        PDFInfo info = doc.getInfo();
        info.setTitle("MyTitle");
        info.setSubject("MySubject");
        info.setAuthor("That's me");
        info.setKeywords("XSL-FO XML");
        //info.setProducer("SuperFOP");
        info.setCreator("WonderFOP");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
        cal1.set(2007, Calendar.JUNE, 5, 21, 49, 13);
        cal1.set(Calendar.MILLISECOND, 0);
        info.setCreationDate(cal1.getTime());
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
        cal2.set(2007, Calendar.JUNE, 6, 8, 15, 59);
        cal2.set(Calendar.MILLISECOND, 0);
        info.setModDate(cal2.getTime());

        Metadata meta = PDFMetadata.createXMPFromPDFDocument(doc);

        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(meta);
        assertEquals("MyTitle", dc.getTitle());
        assertEquals("MySubject", dc.getDescription());
        assertEquals(1, dc.getCreators().length);
        assertEquals("That's me", dc.getCreators()[0]);
        AdobePDFAdapter pdf = AdobePDFSchema.getAdapter(meta);
        assertEquals("XSL-FO XML", pdf.getKeywords());
        assertEquals("SuperFOP", pdf.getProducer());
        XMPBasicAdapter xmp = XMPBasicSchema.getAdapter(meta);
        assertEquals("WonderFOP", xmp.getCreatorTool());
        assertEquals(cal1.getTime(), xmp.getCreateDate());
        assertEquals(cal2.getTime(), xmp.getModifyDate());
    }

    @Test
    public void testXMPMetaDataForLanguageAndDateForPDF2A() throws Exception {
        PDFDocument doc = new PDFDocument("SuperFOP");
        doc.getRoot().setLanguage(new Locale("en"));
        doc.getProfile().setPDFAMode(PDFAMode.PDFA_2A);
        Metadata meta = PDFMetadata.createXMPFromPDFDocument(doc);
        assertTrue(meta.getProperty("http://purl.org/dc/elements/1.1/", "language").getValue().toString()
                .contains("rdf:Bag"));
        assertTrue(meta.getProperty("http://purl.org/dc/elements/1.1/", "date").getValue().toString()
                .contains("rdf:Seq"));
    }

    @Test
    public void testXMPMetaDataForLanguageAndDateForPDF1A() throws Exception {
        PDFDocument doc = new PDFDocument("SuperFOP");
        doc.getRoot().setLanguage(new Locale("en"));
        doc.getProfile().setPDFAMode(PDFAMode.PDFA_1A);
        Metadata meta = PDFMetadata.createXMPFromPDFDocument(doc);
        assertFalse(meta.getProperty("http://purl.org/dc/elements/1.1/", "language").getValue().toString()
                .contains("rdf:Bag"));
        assertFalse(meta.getProperty("http://purl.org/dc/elements/1.1/", "date").getValue().toString()
                .contains("rdf:Seq"));
    }

    @Test
    public void testPDFAExtensionSchema() {
        PDFDocument doc = new PDFDocument("SuperFOP");
        doc.getProfile().setPDFAMode(PDFAMode.PDFA_1A);
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        Metadata meta = PDFMetadata.createXMPFromPDFDocument(doc);

        XMPProperty schemas = meta.getProperty(XMPConstants.PDF_A_EXTENSION, "schemas");
        assertProperties(schemas, XMPConstants.PDF_A_EXTENSION, "schemas", null, null);
        assertNotNull("When PDF/A and PDF/UA are both active, we need to add an "
                + "extension element to avoid validation errors from PDF/A validators", schemas);

        List<XMPProperty> schemasArrayList = assertArrayValue(schemas, XMPArrayType.BAG);
        assertProperties(schemasArrayList.get(0), XMPConstants.PDF_A_SCHEMA, "schema",
                "pdfaSchema", "PDF/UA identification schema");
        assertProperties(schemasArrayList.get(1), XMPConstants.PDF_A_SCHEMA, "namespaceURI",
                "pdfaSchema", "http://www.aiim.org/pdfua/ns/id/");
        assertProperties(schemasArrayList.get(2), XMPConstants.PDF_A_SCHEMA, "prefix",
                "pdfaSchema", "pdfuaid");
        assertProperties(schemasArrayList.get(3), XMPConstants.PDF_A_SCHEMA, "property",
                "pdfaSchema", null);

        List<XMPProperty> propertyArrayList = assertArrayValue(schemasArrayList.get(3), XMPArrayType.SEQ);
        assertProperties(propertyArrayList.get(0), XMPConstants.PDF_A_PROPERTY, "name",
                "pdfaProperty", "part");
        assertProperties(propertyArrayList.get(1), XMPConstants.PDF_A_PROPERTY, "valueType",
                "pdfaProperty", "Integer");
        assertProperties(propertyArrayList.get(2), XMPConstants.PDF_A_PROPERTY, "category",
                "pdfaProperty", "internal");
        assertProperties(propertyArrayList.get(3), XMPConstants.PDF_A_PROPERTY, "description",
                "pdfaProperty", "Indicates, which part of ISO 14289 standard is followed");
    }

    private void assertProperties(XMPProperty prop, String ns, String localName, String prefix,
                                  String value) {
        QName name = prop.getName();
        assertEquals("Property must have expected value or the validator might fail",
                ns, name.getNamespaceURI());
        assertEquals("Property must have expected value or the validator might fail",
                localName, name.getLocalName());
        assertEquals("Property must have expected value or the validator might fail",
                prefix, name.getPrefix());

        if (value != null) {
            assertEquals("Property must have expected value or the validator might fail",
                    value, prop.getValue());
        }
    }

    private List<XMPProperty> assertArrayValue(XMPProperty prop, XMPArrayType type) {
        Object value = prop.getValue();
        assertEquals("Property value must be an array", XMPArray.class, value.getClass());

        XMPArray array = (XMPArray) value;
        assertEquals("The property expects an array of the given type",
                type, array.getType());
        assertEquals("Array must only have 1 element with 4 properties inside",
                1, array.getSize());

        Object arrayValue = array.getValue(0);
        assertEquals("Array must only have 1 element with 4 properties inside",
                ArrayList.class, arrayValue.getClass());

        List<XMPProperty> arrayList = (List<XMPProperty>) arrayValue;
        assertEquals("Array must only have 1 element with 4 properties inside",
                4, arrayList.size());

        return arrayList;
    }
}
