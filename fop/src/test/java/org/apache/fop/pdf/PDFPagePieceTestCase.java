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

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.extensions.PDFCollectionEntryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryAttachment;
import org.apache.fop.render.pdf.extensions.PDFDictionaryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryType;

import junit.framework.Assert;

public class PDFPagePieceTestCase {
    @Test
    public void testPDF() throws IFException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(userAgent));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(bos));
        documentHandler.startDocument();
        documentHandler.startPage(0, "", "", new Dimension());

        PDFDictionaryExtension dictionaryExtension = mock(PDFDictionaryExtension.class);
        when(dictionaryExtension.getDictionaryType()).thenReturn(PDFDictionaryType.PagePiece);

        PDFDictionaryExtension child = mock(PDFDictionaryExtension.class);
        when(child.getKey()).thenReturn("a");
        when(dictionaryExtension.getEntries()).thenReturn(Arrays.<PDFCollectionEntryExtension>asList(child));
        documentHandler.handleExtensionObject(new PDFDictionaryAttachment(dictionaryExtension));

        documentHandler.endPage();
        Assert.assertTrue(bos.toString(), bos.toString().contains("/PieceInfo << /a << >> /LastModified (D:"));
    }
}
