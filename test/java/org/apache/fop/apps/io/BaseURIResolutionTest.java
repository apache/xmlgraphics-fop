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

package org.apache.fop.apps.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;

import static org.apache.fop.FOPTestUtils.getBaseDir;
import static org.junit.Assert.assertTrue;

public abstract class BaseURIResolutionTest {

    private final FopFactory fopFactory;
    private SAXTransformerFactory tfactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    private static final File BACKUP_DIR = new File(getBaseDir(), "build/test-results");

    public BaseURIResolutionTest(FopFactoryBuilder builder, File foFile) throws FOPException,
            TransformerException, IOException {
        fopFactory = builder.build();
        createDocument(foFile);
    }

    public BaseURIResolutionTest(InputStream confStream, ResourceResolver resolver, File foFile)
            throws FOPException, TransformerException, SAXException, IOException {
        this(new FopConfParser(confStream, getBaseDir().toURI(), resolver).getFopFactoryBuilder(),
                foFile);
    }

    private void createDocument(File foFile) throws TransformerException, FOPException,
            IOException {
        FOUserAgent ua = fopFactory.newFOUserAgent();

        ByteArrayOutputStream baout = new ByteArrayOutputStream();

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, ua, baout);

        Transformer transformer = tfactory.newTransformer(); //Identity transf.
        Source src = new StreamSource(foFile);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        OutputStream out = new java.io.FileOutputStream(
                new File(BACKUP_DIR, foFile.getName() + ".pdf"));
        try {
            baout.writeTo(out);
        } finally {
            IOUtils.closeQuietly(out);
        }

        //Test using PDF as the area tree doesn't invoke Batik so we could check
        //if the resolver is actually passed to Batik by FOP
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    public abstract void testAssertions();

    static File getFODirectory() {
        return new File(getBaseDir(), "test/xml/uri-testing/");
    }
}
