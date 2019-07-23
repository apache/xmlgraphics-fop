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
package org.apache.fop.render;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;

public class ConserveMemoryTestCase {
    @Test
    public void testLink() throws Throwable {
        final String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + " <fo:block><fo:basic-link internal-destination=\"a\">a</fo:basic-link></fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";

        ExecutorService es = Executors.newCachedThreadPool();
        final Throwable[] ex = new Throwable[1];
        for (int i = 0; i < 5; i++) {
            Runnable thread = new Runnable() {
                public void run() {
                    try {
                        foToOutput(fo);
                    } catch (Throwable e) {
                        ex[0] = e;
                    }
                }
            };
            es.execute(thread);
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.MINUTES);
        if (ex[0] != null) {
            throw ex[0];
        }
    }

    private void foToOutput(String fo) throws SAXException, TransformerException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setConserveMemoryPolicy(true);
        userAgent.setAccessibility(true);
        Fop fop = fopFactory.newFop("application/pdf", userAgent, new ByteArrayOutputStream());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }
}
