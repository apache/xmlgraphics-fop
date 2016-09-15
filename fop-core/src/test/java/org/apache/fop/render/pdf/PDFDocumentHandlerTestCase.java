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

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;

public class PDFDocumentHandlerTestCase {
    @Test
    public void testPageContentsDeduplicated() throws IFException {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler d = new PDFDocumentHandler(new IFContext(userAgent));
        d.setFontInfo(new FontInfo());
        OutputStream writer = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(writer);
        d.setResult(result);
        d.startDocument();

        addPage(d, "a", 0);
        addPage(d, "b", 1);
        addPage(d, "a", 2);
        d.endDocument();

        List<String> contents = new ArrayList<String>();
        for (String line : writer.toString().split("\n")) {
            if (line.trim().startsWith("/Contents")) {
                contents.add(line);
            }
        }
        Assert.assertEquals(contents.size(), 3);
        Assert.assertEquals(contents.get(0), contents.get(2));
        Assert.assertFalse(contents.get(0).equals(contents.get(1)));
    }

    private void addPage(PDFDocumentHandler d, String command, int i) throws IFException {
        d.startPage(i, "", "", new Dimension());
        d.getGenerator().add(command);
        d.endPage();
    }
}
