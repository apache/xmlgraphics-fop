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

package org.apache.fop.tagging;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

/**
 * Does tests on the intermediate format.
 */
public class PdfTaggingTester {

    private final PdfTaggingChecksFactory pdfTaggingChecksFactory = new PdfTaggingChecksFactory();

    private final TransformerFactory tfactory;

    private File backupDir;

    /**
     * Main constructor.
     *
     * @param transformerFactory the factory used to serialize the intermediate format files
     * @param backupDir an optional directory in which to write the serialized
     * IF files (may be null)
     */
    public PdfTaggingTester(TransformerFactory transformerFactory, File backupDir) {
        this.tfactory = transformerFactory;
        this.backupDir = backupDir;
    }

    /**
     * Runs the PDF tagging checks.
     * @param testName the name of the test case
     * @param checksRoot the root element containing the PDF tagging checks
     * @param pdfTaggingDocument the PDF tagging XML
     * @throws TransformerException if an error occurs while transforming the content
     */
    public void doPdfTaggingChecks(String testName, Element checksRoot, Document pdfTaggingDocument)
            throws TransformerException {
        if (this.backupDir != null) {
            Transformer transformer = tfactory.newTransformer();
            Source src = new DOMSource(pdfTaggingDocument);
            File targetFile = new File(this.backupDir, testName + ".pdf-tagging.xml");
            Result res = new StreamResult(targetFile);
            transformer.transform(src, res);
        }
        List<PdfTaggingCheck> checks = pdfTaggingChecksFactory.createCheckList(checksRoot);
        if (checks.size() == 0) {
            throw new RuntimeException("No available PDF tagging check");
        }
        for (PdfTaggingCheck check : checks) {
            check.check(pdfTaggingDocument);
        }
    }

}
