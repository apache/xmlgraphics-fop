/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

import java.io.*;
import java.util.Vector ;

import java.awt.print.PrinterJob ;
import java.awt.print.PrinterException ;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.apps.Version;
import org.apache.fop.apps.XSLTInputHandler;

import org.apache.fop.render.awt.AWTRenderer ;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * Example servlet to generate a fop printout from a servlet.
 * Printing goes to the default printer on host where the servlet executes.
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a formatting object file to render
 * </ul>
 *
 * Example URL: http://servername/fop/servlet/FopPrintServlet?fo=readme.fo
 * Example URL: http://servername/fop/servlet/FopPrintServlet?xml=data.xml&xsl=format.xsl
 * Compiling: you will need
 * - servlet_2_2.jar
 * - fop.jar
 * - sax api
 * - avalon-framework-x.jar (where x is the version found the FOP lib dir)
 *
 * Running: you will need in the WEB-INF/lib/ directory:
 * - fop.jar
 * - batik.jar
 * - avalon-framework-x.jar (where x is the version found the FOP lib dir)
 * - xalan-2.0.0.jar
 */

public class FopPrintServlet extends HttpServlet {
    public static final String FO_REQUEST_PARAM = "fo";
    public static final String XML_REQUEST_PARAM = "xml";
    public static final String XSL_REQUEST_PARAM = "xsl";
    Logger log = null;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        if (log == null) {
            log = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
        }

        try {
            String foParam = request.getParameter(FO_REQUEST_PARAM);
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xslParam = request.getParameter(XSL_REQUEST_PARAM);

            if (foParam != null) {
                FileInputStream file = new FileInputStream(foParam);
                renderFO(new InputSource(file), response);
            } else if ((xmlParam != null) && (xslParam != null)) {
                XSLTInputHandler input =
                  new XSLTInputHandler(new File(xmlParam),
                                       new File(xslParam));
                renderXML(input, response);
            } else {
                response.setContentType ("text/html");

                PrintWriter out = response.getWriter();
                out.println("<html><title>Error</title>\n"+ "<body><h1>FopServlet Error</h1><h3>No 'fo' or 'xml/xsl' "+
                            "request param given.</h3></body></html>");
            }
        } catch (ServletException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Renders an FO inputsource to the default printer.
     */
    public void renderFO(InputSource foFile,
                         HttpServletResponse response) throws ServletException {
        try {
            Driver driver = new Driver(foFile, null);
            PrinterJob pj = PrinterJob.getPrinterJob();
            PrintRenderer renderer = new PrintRenderer(pj);

            driver.setLogger (log);
            driver.setRenderer(renderer);
            driver.run();

            reportOK (response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Renders an FO generated using an XML and a stylesheet to the default printer.
     */
    public void renderXML(XSLTInputHandler input,
                          HttpServletResponse response) throws ServletException {
        try {
            Driver driver = new Driver();
            PrinterJob pj = PrinterJob.getPrinterJob();
            PrintRenderer renderer = new PrintRenderer(pj);

            pj.setCopies(1);

            driver.setLogger (log);
            driver.setRenderer (renderer);
            driver.render (input.getParser(), input.getInputSource());

            reportOK (response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    // private helper, tell (browser) user that file printed
    private void reportOK (HttpServletResponse response)
    throws ServletException {
        String sMsg =
          "<html><title>Success</title>\n" + "<body><h1>FopPrintServlet: </h1>" +
          "<h3>The requested data was printed</h3></body></html>" ;

        response.setContentType ("text/html");
        response.setContentLength (sMsg.length());

        try {
            PrintWriter out = response.getWriter();
            out.println (sMsg);
            out.flush();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    // This is stolen from PrintStarter
    class PrintRenderer extends AWTRenderer {

        private static final int EVEN_AND_ALL = 0;
        private static final int EVEN = 1;
        private static final int ODD = 2;

        private int startNumber;
        private int endNumber;
        private int mode = EVEN_AND_ALL;
        private int copies = 1;
        private PrinterJob printerJob;

        PrintRenderer(PrinterJob printerJob) {
            super(null);

            this.printerJob = printerJob;
            startNumber = 0 ;
            endNumber = -1;

            printerJob.setPageable(this);

            mode = EVEN_AND_ALL;
            String str = System.getProperty("even");
            if (str != null) {
                try {
                    mode = Boolean.valueOf(str).booleanValue() ? EVEN : ODD;
                } catch (Exception e) {}

            }
        }

        public void stopRenderer()
        throws IOException {
            super.stopRenderer();

            if (endNumber == -1)
                endNumber = getPageCount();

            Vector numbers = getInvalidPageNumbers();
            for (int i = numbers.size() - 1; i > -1; i--)
                //removePage(
                //  Integer.parseInt((String) numbers.elementAt(i)));

            try {
                printerJob.print();
            } catch (PrinterException e) {
                e.printStackTrace();
                throw new IOException("Unable to print: " +
                                      e.getClass().getName() + ": " + e.getMessage());
            }
        }

        public void renderPage(PageViewport page) throws IOException, FOPException {
            pageWidth = (int)((float) page.getViewArea().getWidth() / 1000f);
            pageHeight = (int)((float) page.getViewArea().getHeight() / 1000f);
            super.renderPage(page);
        }


        private Vector getInvalidPageNumbers() {

            Vector vec = new Vector();
            int max = getPageCount();
            boolean isValid;
            for (int i = 0; i < max; i++) {
                isValid = true;
                if (i < startNumber || i > endNumber) {
                    isValid = false;
                } else if (mode != EVEN_AND_ALL) {
                    if (mode == EVEN && ((i + 1) % 2 != 0))
                        isValid = false;
                    else if (mode == ODD && ((i + 1) % 2 != 1))
                        isValid = false;
                }

                if (!isValid)
                    vec.add(i + "");
            }

            return vec;
        }
    } // class PrintRenderer

}

