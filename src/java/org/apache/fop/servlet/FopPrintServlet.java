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

package org.apache.fop.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;


/**
 * Example servlet to generate a fop printout from a servlet.
 * Printing goes to the default printer on host where the servlet executes.
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a XSL-FO file to render
 * </ul>
 * or
 * <ul>
 *   <li>xml: the path to an XML file to render</li>
 *   <li>xslt: the path to an XSLT file that can transform the above XML to XSL-FO</li>
 * </ul>
 * <br/>
 * Example URL: http://servername/fop/servlet/FopPrintServlet?fo=readme.fo
 * <br/>
 * Example URL: http://servername/fop/servlet/FopPrintServlet?xml=data.xml&xsl=format.xsl
 * <br/>
 * <b>Note:</b> This servlet is derived from FopServlet. Most methods are inherited from the
 * superclass. Only the differences to the base class are necessary.
 *
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id$
 */
public class FopPrintServlet extends FopServlet {

    private static final long serialVersionUID = 1645706757391617935L;

    /**
     * {@inheritDoc}
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response)
            throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent();

        //Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_FOP_PRINT, foUserAgent);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        reportOK(response);
    }

    // private helper, tell (browser) user that file printed
    private void reportOK(HttpServletResponse response) throws IOException {
        String sMsg = "<html><title>Success</title>\n"
                + "<body><h1>FopPrintServlet: </h1>"
                + "<h3>The requested data was printed to the default printer.</h3></body></html>";

        response.setContentType("text/html");
        response.setContentLength(sMsg.length());

        PrintWriter out = response.getWriter();
        out.println(sMsg);
        out.flush();
    }

}

