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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Base class for automated tests that create PDF files
 */
public class BasePDFTest {

    /** the FopFactory */
    protected final FopFactory fopFactory;

    /** the JAXP TransformerFactory */
    protected final TransformerFactory tFactory = TransformerFactory.newInstance();

    public BasePDFTest(String fopConf) throws SAXException, IOException {
        fopFactory = FopFactory.newInstance(new File(fopConf));
    }

    public BasePDFTest(InputStream confStream) throws SAXException, IOException {
        fopFactory = new FopConfParser(confStream, new File(".").toURI()).getFopFactoryBuilder()
                                                                         .build();
    }

    /**
     * Convert a test FO file to PDF
     * @param foFile the FO file
     * @param ua the preconfigured user agent
     * @param dumpPdfFile if true, dumps the generated PDF file to a file name (foFile).pdf
     * @return the generated PDF data
     * @throws Exception if the conversion fails
     */
    protected byte[] convertFO(File foFile, FOUserAgent ua, boolean dumpPdfFile)
             throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, ua, baout);
        Transformer transformer = tFactory.newTransformer();
        Source src = new StreamSource(foFile);
        SAXResult res = new SAXResult(fop.getDefaultHandler());
        try {
            transformer.transform(src, res);
            final byte[] result = baout.toByteArray();
            if (dumpPdfFile) {
                final File outFile = new File(foFile.getParentFile(), foFile.getName() + ".pdf");
                FileUtils.writeByteArrayToFile(outFile, result);
            }
            return result;
        } catch (TransformerException e) {
            throw extractOriginalException(e);
        }
    }

    private static Exception extractOriginalException(Exception e) {
        if (e.getCause() != null) {
            return extractOriginalException((Exception)e.getCause());
        } else if (e instanceof SAXException) {
            SAXException se = (SAXException)e;
            if (se.getException() != null) {
                return extractOriginalException(se.getException());
            }
        }
        return e;
    }

    /**
     * get FOP config File
     * @return user config file to be used for testing
     */
    protected static String getDefaultConfFile() {
        return "test/test.xconf";
    }
}
