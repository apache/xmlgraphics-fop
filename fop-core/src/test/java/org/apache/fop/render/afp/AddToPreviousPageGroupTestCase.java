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

package org.apache.fop.render.afp;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;

public class AddToPreviousPageGroupTestCase extends AbstractAFPTest {
    @Test
    public void testAddToPreviousPageGroup() throws Exception {
        Assert.assertEquals("BEGIN DOCUMENT DOC00001 Triplets: 0x01,\n"
                + "BEGIN PAGE_GROUP PGP00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "END PAGE PGN00001\n"
                + "BEGIN PAGE PGN00002\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00002\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00002\n"
                + "END PAGE PGN00002\n"
                + "END PAGE_GROUP PGP00001\n"
                + "END DOCUMENT DOC00001\n", render());
    }

    private String render() throws IFException, IOException {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        AFPDocumentHandler documentHandler = new AFPDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(outputStream));
        documentHandler.startDocument();
        documentHandler.startPageSequence("");
        documentHandler.startPage(0, "", "", new Dimension());
        documentHandler.endPage();
        documentHandler.endPageSequence();
        Map<QName, String> attributes = new HashMap<>();
        attributes.put(AFPElementMapping.ADD_TO_PREVIOUS_PAGE_GROUP, "true");
        documentHandler.getContext().setForeignAttributes(attributes);
        documentHandler.startPageSequence("");
        documentHandler.startPage(1, "", "", new Dimension());
        documentHandler.endPage();
        documentHandler.endPageSequence();
        documentHandler.endDocument();
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(new ByteArrayInputStream(outputStream.toByteArray()), sb);
        return sb.toString();
    }
}
