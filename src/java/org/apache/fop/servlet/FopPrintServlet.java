/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.Log;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.XSLTInputHandler;
import org.xml.sax.InputSource;

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
 *
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @version $Id: FopPrintServlet.java,v 1.2 2003/03/07 09:48:05 jeremias Exp $
 * (todo) Doesn't work since there's no AWTRenderer at the moment. Revisit when
 * available.
 * (todo) Don't use XSLTInputHandler anymore
 * (todo) Ev. add caching mechanism for Templates objects
 */
public class FopPrintServlet extends HttpServlet {

    /** Name of the parameter used for the XSL-FO file */
    protected static final String FO_REQUEST_PARAM = "fo";
    /** Name of the parameter used for the XML file */
    protected static final String XML_REQUEST_PARAM = "xml";
    /** Name of the parameter used for the XSLT file */
    protected static final String XSLT_REQUEST_PARAM = "xslt";

    /** Logger to give to FOP */
    protected SimpleLog log = null;
    
    /** The TransformerFactory to use to create Transformer instances */
    protected TransformerFactory transFactory = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        this.log = new SimpleLog("FOP/Print Servlet");
        log.setLevel(SimpleLog.LOG_LEVEL_WARN);
        this.transFactory = TransformerFactory.newInstance();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        if (log == null) {
            log = new SimpleLog("FOP/Print Servlet");
            log.setLevel(SimpleLog.LOG_LEVEL_WARN);
        }

        try {
            String foParam = request.getParameter(FO_REQUEST_PARAM);
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xsltParam = request.getParameter(XSLT_REQUEST_PARAM);

            if (foParam != null) {
                InputStream file = new java.io.FileInputStream(foParam);
                renderFO(new InputSource(file), response);
            } else if ((xmlParam != null) && (xsltParam != null)) {
                XSLTInputHandler input =
                  new XSLTInputHandler(new File(xmlParam),
                                       new File(xsltParam), null);
                renderXML(input, response);
            } else {
                response.setContentType("text/html");

                PrintWriter out = response.getWriter();
                out.println("<html><title>Error</title>\n"
                        + "<body><h1>FopServlet Error</h1>\n"
                        + "<h3>No 'fo' or 'xml/xsl' "
                        + "request param given.</h3></body>\n</html>");
            }
        } catch (ServletException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Renders an FO inputsource to the default printer.
     * @param foFile The XSL-FO file
     * @param response Response to write to
     * @throws ServletException In case of a problem
     */
    public void renderFO(InputSource foFile,
                         HttpServletResponse response) throws ServletException {
        try {
            Driver driver = new Driver(foFile, null);
            driver.setRenderer(Driver.RENDER_PRINT);
            driver.run();

            reportOK (response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Renders an FO generated using an XML and a stylesheet to the default printer.
     * @param input XSLTInputHandler to use
     * @param response Response to write to
     * @throws ServletException In case of a problem
     */
    public void renderXML(XSLTInputHandler input,
                          HttpServletResponse response) throws ServletException {
        try {
            Driver driver = new Driver();
            driver.setRenderer(Driver.RENDER_PRINT);
            driver.render(input.getParser(), input.getInputSource());

            reportOK (response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    // private helper, tell (browser) user that file printed
    private void reportOK(HttpServletResponse response)
                throws ServletException {
        String sMsg = "<html><title>Success</title>\n"
                + "<body><h1>FopPrintServlet: </h1>"
                + "<h3>The requested data was printed</h3></body></html>";

        response.setContentType("text/html");
        response.setContentLength(sMsg.length());

        try {
            PrintWriter out = response.getWriter();
            out.println(sMsg);
            out.flush();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}

