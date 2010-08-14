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

package org.apache.fop.memory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Debug tool to create and process large FO files by replicating them a specified number of times.
 */
public class MemoryEater {

    private SAXTransformerFactory tFactory
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
    private FopFactory fopFactory = FopFactory.newInstance();
    private Templates replicatorTemplates;

    private Stats stats;

    public MemoryEater() throws TransformerConfigurationException, MalformedURLException {
        File xsltFile = new File("test/xsl/fo-replicator.xsl");
        Source xslt = new StreamSource(xsltFile);
        replicatorTemplates = tFactory.newTemplates(xslt);
    }

    private void eatMemory(File foFile, int runRepeats, int replicatorRepeats) throws Exception {
        stats = new Stats();
        for (int i = 0; i < runRepeats; i++) {
            eatMemory(i, foFile, replicatorRepeats);
            stats.progress(i, runRepeats);
        }
        stats.dumpFinalStats();
        System.out.println(stats.getGoogleChartURL());
    }

    private void eatMemory(int callIndex, File foFile, int replicatorRepeats) throws Exception {
        Source src = new StreamSource(foFile);

        Transformer transformer = replicatorTemplates.newTransformer();
        transformer.setParameter("repeats", new Integer(replicatorRepeats));

        OutputStream out = new NullOutputStream(); //write to /dev/nul
        try {
            FOUserAgent userAgent = fopFactory.newFOUserAgent();
            userAgent.setBaseURL(foFile.getParentFile().toURI().toURL().toExternalForm());
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, out);
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            stats.notifyPagesProduced(fop.getResults().getPageCount());
            if (callIndex == 0) {
                System.out.println(foFile.getName() + " generates "
                        + fop.getResults().getPageCount() + " pages.");
            }
            stats.checkStats();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private static void prompt() throws IOException {
        BufferedReader in = new BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.print("Press return to continue...");
        in.readLine();
    }

    /**
     * Main method.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        boolean doPrompt = true; //true if you want a chance to start the monitoring console
        try {
            int replicatorRepeats = 2;
            int runRepeats = 1;
            if (args.length > 0) {
                replicatorRepeats = Integer.parseInt(args[0]);
            }
            if (args.length > 1) {
                runRepeats = Integer.parseInt(args[1]);
            }
            File testFile = new File("examples/fo/basic/readme.fo");

            System.out.println("MemoryEater! About to replicate the test file "
                    + replicatorRepeats + " times and run it " + runRepeats + " times...");
            if (doPrompt) {
                prompt();
            }

            System.out.println("Processing...");
            long start = System.currentTimeMillis();

            MemoryEater app = new MemoryEater();
            app.eatMemory(testFile, runRepeats, replicatorRepeats);

            long duration = System.currentTimeMillis() - start;
            System.out.println("Success! Job took " + duration + " ms");

            if (doPrompt) {
                prompt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
