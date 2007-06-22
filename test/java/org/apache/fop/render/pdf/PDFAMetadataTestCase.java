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

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFMetadata;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;

import junit.framework.TestCase;

/**
 * Test case for PDF/A metadata handling.
 */
public class PDFAMetadataTestCase extends TestCase {

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
        
        Metadata meta = PDFMetadata.createXMPFromUserAgent(doc);
        
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
}
