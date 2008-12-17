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

package org.apache.fop.render.ps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.MissingResourceException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.events.AbstractResourceDSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Abstract base class for PostScript verification tests.
 */
public abstract class AbstractPostScriptTestCase extends TestCase {

    /** the JAXP TransformerFactory */
    protected TransformerFactory tFactory = TransformerFactory.newInstance();
    /** the FopFactory */
    protected FopFactory fopFactory = FopFactory.newInstance();

    /**
     * Renders a test file.
     * @param ua the user agent (with override set!)
     * @param resourceName the resource name for the FO file
     * @param suffix a suffix for the output filename
     * @return the output file
     * @throws Exception if an error occurs
     */
    protected File renderFile(FOUserAgent ua, String resourceName, String suffix)
                throws Exception {
        File outputFile = new File("build/test-results/" + resourceName + suffix + ".ps");
        File outputDir = outputFile.getParentFile();
        FileUtils.forceMkdir(outputDir);

        // Prepare input file
        InputStream in = getClass().getResourceAsStream(resourceName);
        if (in == null) {
            throw new MissingResourceException(resourceName + " not found in resources",
                    getClass().getName(), null);
        }
        try {
            Source src = new StreamSource(in);

            // Create PostScript
            OutputStream out = new java.io.FileOutputStream(outputFile);
            out = new java.io.BufferedOutputStream(out);
            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_POSTSCRIPT, ua, out);
                SAXResult res = new SAXResult(fop.getDefaultHandler());

                Transformer transformer = tFactory.newTransformer();
                transformer.transform(src, res);
            } finally {
                IOUtils.closeQuietly(out);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return outputFile;
    }

    /**
     * Scans for a certain resource DSC comment and checks against a given resource.
     * @param parser the DSC parser
     * @param comment the comment to scan for
     * @param resource the resource to check against
     * @throws IOException if an I/O error occurs
     * @throws DSCException if a DSC error occurs
     */
    protected void checkResourceComment(DSCParser parser, String comment, PSResource resource)
                throws IOException, DSCException {
        AbstractResourceDSCComment resComment;
        resComment = (AbstractResourceDSCComment)gotoDSCComment(parser, comment);
        assertEquals(resource, resComment.getResource());
    }

    /**
     * Advances the DSC parser to a DSC comment with the given name.
     * @param parser the DSC parser
     * @param name the name of the DSC comment
     * @return the DSC comment
     * @throws IOException if an I/O error occurs
     * @throws DSCException if a DSC error occurs
     */
    protected static DSCComment gotoDSCComment(DSCParser parser, String name)
            throws IOException, DSCException {
        while (parser.hasNext()) {
            DSCEvent event = parser.nextEvent();
            if (event.isDSCComment()) {
                DSCComment comment = event.asDSCComment();
                if (comment.getName().equals(name)) {
                    return comment;
                }
            }
        }
        return null;
    }

}
