/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.mif;

// Java
import java.io.IOException;
import java.io.OutputStream;

// XML
import org.xml.sax.SAXException;

// FOP
import org.apache.fop.apps.StructureHandler;
import org.apache.fop.fo.Title;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Flow;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.apps.FOPException;

// TODO: do we really want every method throwing a SAXException

/**
 * The MIF Handler.
 * This generates MIF output using the structure events from
 * the FO Tree sent to this structure handler.
 * This builds an MIF file and writes it to the output.
 */
public class MIFHandler extends StructureHandler {
    
    /** the MIFFile instance */
    protected MIFFile mifFile;
    /** the OutputStream to write to */
    protected OutputStream outStream;
    private FontInfo fontInfo = new FontInfo();

    // current state elements
    private MIFElement textFlow;
    private MIFElement para;

    /**
     * Creates a new MIF handler on a given OutputStream.
     * @param os OutputStream to write to
     */
    public MIFHandler(OutputStream os) {
        outStream = os;
        // use pdf fonts for now, this is only for resolving names
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo, null);
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#getFontInfo()
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        mifFile = new MIFFile();
        try {
            mifFile.output(outStream);
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        // finish all open elements
        mifFile.finish(true);
        try {
            mifFile.output(outStream);
            outStream.flush();
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * Start the page sequence.
     * This creates the pages in the MIF document that will be used
     * by the following flows and static areas.
     * @see org.apache.fop.apps.StructureHandler
     */
    public void startPageSequence(PageSequence pageSeq, Title seqTitle, LayoutMasterSet lms) {
        // get the layout master set
        // setup the pages for this sequence
        String name = pageSeq.getProperty("master-reference").getString();
        SimplePageMaster spm = lms.getSimplePageMaster(name);
        if (spm == null) {
            PageSequenceMaster psm = lms.getPageSequenceMaster(name);
        } else {
            // create simple master with regions
            MIFElement prop = new MIFElement("PageType");
            prop.setValue("BodyPage");

           MIFElement page = new MIFElement("Page");
           page.addElement(prop);

           prop = new MIFElement("PageBackground");
           prop.setValue("'Default'");
           page.addElement(prop);

           // build regions
           MIFElement textRect = new MIFElement("TextRect");
           prop = new MIFElement("ID");
           prop.setValue("1");
           textRect.addElement(prop);
           prop = new MIFElement("ShapeRect");
           prop.setValue("0.0 841.889 453.543 0.0");
           textRect.addElement(prop);
           page.addElement(textRect);

           textRect = new MIFElement("TextRect");
           prop = new MIFElement("ID");
           prop.setValue("2");
           textRect.addElement(prop);
           prop = new MIFElement("ShapeRect");
           prop.setValue("0.0 841.889 453.543 187.65");
           textRect.addElement(prop);
           page.addElement(textRect);



           mifFile.addPage(page);
        }
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endPageSequence(PageSequence)
     */
    public void endPageSequence(PageSequence pageSeq) throws FOPException {

    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
        // start text flow in body region
        textFlow = new MIFElement("TextFlow");
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
        textFlow.finish(true);
        mifFile.addElement(textFlow);
        textFlow = null;
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
        para = new MIFElement("Para");
        // get font
        textFlow.addElement(para);
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
        para.finish(true);
        para = null;
    }

    /**
     * @see org.apache.fop.apps.StructureHandler#characters(char[], int, int)
     */
    public void characters(char data[], int start, int length) {
        if (para != null) {
            String str = new String(data, start, length);
            str = str.trim();
            // break into nice length chunks
            if (str.length() == 0) {
                return;
            }

            MIFElement line = new MIFElement("ParaLine");
            MIFElement prop = new MIFElement("TextRectID");
            prop.setValue("2");
            line.addElement(prop);
            prop = new MIFElement("String");
            prop.setValue("\"" + str + "\"");
            line.addElement(prop);

            para.addElement(line);
        }
    }

}

