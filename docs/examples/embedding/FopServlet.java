/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
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
 *
 */	

public class FopServlet extends HttpServlet 
{
    public static final String FO_REQUEST_PARAM = "fo";

    public void doGet(HttpServletRequest request, 
		      HttpServletResponse response) 
	throws ServletException
    {
	try {
	    if (request.getParameter(FO_REQUEST_PARAM) != null) {
		FileInputStream file = new FileInputStream(request.getParameter(FO_REQUEST_PARAM));
		renderFO(new InputSource(file), response);
	    }
	    else {
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Error</title></head>\n"+
			    "<body><h1>FopServlet Error</h1><h3>No 'fo' "+
			    "request param given.</body></html>");
	    }
	}
	catch (ServletException ex) {
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
    public void renderFO(InputSource foFile, HttpServletResponse response) 
	throws ServletException
    {
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    
	    response.setContentType("application/pdf");
	    
	    Driver driver = new Driver();
	    driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", 
			       Version.getVersion());
	    driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
	    driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
	    driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
	    driver.setOutputStream(out);
	    driver.buildFOTree(createParser(), foFile);
	    driver.format();
	    driver.render();
	    byte[] content = out.toByteArray();
	    response.setContentLength(content.length);
	    response.getOutputStream().write(content);
	    response.getOutputStream().flush();
	    
	}
	catch (ServletException ex) {
	    throw ex;
	}
	catch (Exception ex) {
	    throw new ServletException(ex);
	}
	
    }
    
    /**
       * creates a SAX parser, using the value of org.xml.sax.parser
       * defaulting to org.apache.xerces.parsers.SAXParser
       *
       * @return the created SAX parser
       */
    static XMLReader createParser() 
	throws ServletException
    {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
       
        try {
            return (XMLReader) Class.forName(parserClassName).newInstance();
        } catch (Exception e) {
	    throw new ServletException(e);
	}
    }

}
