/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;
import org.apache.fop.apps.XSLTInputHandler;

import org.apache.log.*;

/**
 * Example servlet to generate a PDF from a servlet.
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a formatting object file to render
 * </ul>
 *
 * Example URL: http://servername/servlet/FopServlet?fo=readme.fo
 * Example URL: http://servername/servlet/FopServlet?xml=data.xml&xsl=format.xsl
 * Compiling: you will need 
 * - servlet_2_2.jar
 * - fop.jar
 * - sax api
 * - logkit jar
 *
 * Running: you will need in the WEB-INF/lib/ directory:
 * - fop.jar
 * - batik.jar
 * - avalon-framework-4.0.jar
 * - logkit-1.0b4.jar
 * - xalan-2.0.0.jar
 */
public class FopServlet extends HttpServlet {
    public static final String FO_REQUEST_PARAM = "fo";
    public static final String XML_REQUEST_PARAM = "xml";
    public static final String XSL_REQUEST_PARAM = "xsl";
    Logger log = null;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        if(log == null) {
            Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
            log = hierarchy.getLoggerFor("fop");
            log.setPriority(Priority.WARN);
        }
        try {
            String foParam = request.getParameter(FO_REQUEST_PARAM);
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xslParam = request.getParameter(XSL_REQUEST_PARAM);

            if (foParam != null) {
                FileInputStream file = new FileInputStream(foParam);
                renderFO(new InputSource(file), response);
            } else if((xmlParam != null) && (xslParam != null)) {
                XSLTInputHandler input = new XSLTInputHandler(new File(xmlParam), new File(xslParam));
                renderXML(input, response);
            } else {
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Error</title></head>\n"+
                            "<body><h1>FopServlet Error</h1><h3>No 'fo' "+
                            "request param given.</body></html>");
            }
        } catch (ServletException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * renders an FO inputsource into a PDF file which is rendered
     * directly to the response object's OutputStream
     */
    public void renderFO(InputSource foFile,
                         HttpServletResponse response) throws ServletException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            response.setContentType("application/pdf");

            Driver driver = new Driver(foFile, out);
            driver.setLogger(log);
            driver.setRenderer(Driver.RENDER_PDF);
            driver.run();

            byte[] content = out.toByteArray();
            response.setContentLength(content.length);
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    public void renderXML(XSLTInputHandler input,
                         HttpServletResponse response) throws ServletException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            response.setContentType("application/pdf");

            Driver driver = new Driver();
            driver.setLogger(log);
            driver.setRenderer(Driver.RENDER_PDF);
            driver.setOutputStream(out);
            driver.render(input.getParser(), input.getInputSource());

            byte[] content = out.toByteArray();
            response.setContentLength(content.length);
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    static XMLReader createParser() throws ServletException {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }

        try {
            return (XMLReader) Class.forName(
                     parserClassName).newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
