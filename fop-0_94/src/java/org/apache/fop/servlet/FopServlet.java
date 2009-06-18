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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Example servlet to generate a PDF from a servlet.
 * <br/>
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
 * Example URL: http://servername/fop/servlet/FopServlet?fo=readme.fo
 * <br/>
 * Example URL: http://servername/fop/servlet/FopServlet?xml=data.xml&xslt=format.xsl
 * <br/>
 * For this to work with Internet Explorer, you might need to append "&ext=.pdf"
 * to the URL.
 *
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id$
 * (todo) Ev. add caching mechanism for Templates objects
 */
public class FopServlet extends HttpServlet {

    /** Name of the parameter used for the XSL-FO file */
    protected static final String FO_REQUEST_PARAM = "fo";
    /** Name of the parameter used for the XML file */
    protected static final String XML_REQUEST_PARAM = "xml";
    /** Name of the parameter used for the XSLT file */
    protected static final String XSLT_REQUEST_PARAM = "xslt";

    /** Logger to give to FOP */
    protected SimpleLog log = null;
    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;
    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory = null;
    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver; 

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        this.log = new SimpleLog("FOP/Servlet");
        log.setLevel(SimpleLog.LOG_LEVEL_WARN);
        this.uriResolver = new ServletContextURIResolver(getServletContext());
        this.transFactory = TransformerFactory.newInstance();
        this.transFactory.setURIResolver(this.uriResolver);
        //Configure FopFactory as desired
        this.fopFactory = FopFactory.newInstance();
        this.fopFactory.setURIResolver(this.uriResolver);
        configureFopFactory();
    }
    
    /**
     * This method is called right after the FopFactory is instantiated and can be overridden
     * by subclasses to perform additional configuration.
     */
    protected void configureFopFactory() {
        //Subclass and override this method to perform additional configuration
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {
            //Get parameters
            String foParam = request.getParameter(FO_REQUEST_PARAM);
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xsltParam = request.getParameter(XSLT_REQUEST_PARAM);

            //Analyze parameters and decide with method to use
            if (foParam != null) {
                renderFO(foParam, response);
            } else if ((xmlParam != null) && (xsltParam != null)) {
                renderXML(xmlParam, xsltParam, response);
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Error</title></head>\n"
                          + "<body><h1>FopServlet Error</h1><h3>No 'fo' "
                          + "request param given.</body></html>");
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Converts a String parameter to a JAXP Source object.
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        Source src;
        try {
            src = uriResolver.resolve(param, null);
        } catch (TransformerException e) {
            src = null;
        }
        if (src == null) {
            src = new StreamSource(new File(param));
        }
        return src;
    }

    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
    
    /**
     * Renders an XSL-FO file into a PDF file. The PDF is written to a byte
     * array that is returned as the method's result.
     * @param fo the XSL-FO file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs while parsing the input
     * file
     * @throws IOException In case of an I/O problem
     */
    protected void renderFO(String fo, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup source
        Source foSrc = convertString2Source(fo);

        //Setup the identity transformation
        Transformer transformer = this.transFactory.newTransformer();
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(foSrc, transformer, response);
    }

    /**
     * Renders an XML file into a PDF file by applying a stylesheet
     * that converts the XML to XSL-FO. The PDF is written to a byte array
     * that is returned as the method's result.
     * @param xml the XML file
     * @param xslt the XSLT file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void renderXML(String xml, String xslt, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup sources
        Source xmlSrc = convertString2Source(xml);
        Source xsltSrc = convertString2Source(xslt);

        //Setup the XSL transformation
        Transformer transformer = this.transFactory.newTransformer(xsltSrc);
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(xmlSrc, transformer, response);
    }

    /**
     * Renders an input file (XML or XSL-FO) into a PDF file. It uses the JAXP
     * transformer given to optionally transform the input document to XSL-FO.
     * The transformer may be an identity transformer in which case the input
     * must already be XSL-FO. The PDF is written to a byte array that is
     * returned as the method's result.
     * @param src Input XML or XSL-FO
     * @param transformer Transformer to use for optional transformation
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent();

        //Setup output
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        sendPDF(out.toByteArray(), response);
    }
    
    /** @return a new FOUserAgent for FOP */
    protected FOUserAgent getFOUserAgent() {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        //Configure foUserAgent as desired
        return userAgent;
    }

}
