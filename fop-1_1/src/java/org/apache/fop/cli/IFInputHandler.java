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

package org.apache.fop.cli;

import java.io.File;
import java.io.OutputStream;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFParser;
import org.apache.fop.render.intermediate.IFUtil;

/**
 * InputHandler for the intermediate format XML as input.
 */
public class IFInputHandler extends InputHandler {

    /**
     * Constructor for XML->XSLT->intermediate XML input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @param params Vector of command-line parameters (name, value,
     *      name, value, ...) for XSL stylesheet, null if none
     */
    public IFInputHandler(File xmlfile, File xsltfile, Vector params) {
        super(xmlfile, xsltfile, params);
    }

    /**
     * Constructor for intermediate input
     * @param iffile the file to read the intermediate format document from.
     */
    public IFInputHandler(File iffile) {
        super(iffile);
    }

    /** {@inheritDoc} */
    public void renderTo(FOUserAgent userAgent, String outputFormat, OutputStream out)
                throws FOPException {
        IFDocumentHandler documentHandler
            = userAgent.getRendererFactory().createDocumentHandler(
                    userAgent, outputFormat);
        try {
            documentHandler.setResult(new StreamResult(out));
            IFUtil.setupFonts(documentHandler);

            //Create IF parser
            IFParser parser = new IFParser();

            // Resulting SAX events are sent to the parser
            Result res = new SAXResult(parser.getContentHandler(documentHandler, userAgent));

            transformTo(res);
        } catch (IFException ife) {
            throw new FOPException(ife);
        }
    }

}
