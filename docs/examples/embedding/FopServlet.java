/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;


/**
 * Example servlet to generate a PDF from a servlet.
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a formatting object file to render
 * </ul>
 *
 * Example URL: http://servername/servlet/FopServlet?fo=/home/fop/example/readme.fo
 * Compiling: you will need 
 * - servlet_2_2.jar
 * - fop.jar
 * - sax api
 */

public class FopServlet extends HttpServlet {
    public static final String FO_REQUEST_PARAM = "fo";

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {
            if (request.getParameter(FO_REQUEST_PARAM) != null) {
                FileInputStream file = new FileInputStream(
                                         request.getParameter(FO_REQUEST_PARAM));
                renderFO(new InputSource(file), response);
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
            driver.run();

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
