/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.mif;

import org.apache.fop.apps.StructureHandler;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

// Java
import java.io.*;
import java.util.*;

import org.xml.sax.SAXException;

// do we really want every method throwing a SAXException

/**
 * The MIF Handler.
 * This generates MIF output using the structure events from
 * the FO Tree sent to this structure handler.
 * This builds an MIF file and writes it to the output.
 */
public class MIFHandler extends StructureHandler {
    protected MIFFile mifFile;
    protected OutputStream outStream;
    private FontInfo fontInfo = new FontInfo();

    // current state elements
    MIFElement textFlow;
    MIFElement para;

    /**
     */
    public MIFHandler(OutputStream os) {
        outStream = os;
        // use pdf fonts for now, this is only for resolving names
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo, null);
    }

    public FontInfo getFontInfo() {
        return fontInfo;
    }

    public void startDocument() throws SAXException {
        mifFile = new MIFFile();
        try {
            mifFile.output(outStream);
        } catch(IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void endDocument() throws SAXException {
        // finish all open elements
        mifFile.finish(true);
        try {
            mifFile.output(outStream);
            outStream.flush();
        } catch(IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * Start the page sequence.
     * This creates the pages in the MIF document that will be used
     * by the following flows and static areas.
     */
    public void startPageSequence(PageSequence pageSeq, Title seqTitle, LayoutMasterSet lms) {
        // get the layout master set
        // setup the pages for this sequence
        String name = pageSeq.getProperty("master-reference").getString();
        SimplePageMaster spm = lms.getSimplePageMaster(name);
        if(spm == null) {
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

    public void endPageSequence(PageSequence pageSeq) throws FOPException {
        
    }

    public void startFlow(Flow fl) {
        // start text flow in body region
        textFlow = new MIFElement("TextFlow");
    }

    public void endFlow(Flow fl) {
        textFlow.finish(true);
        mifFile.addElement(textFlow);
        textFlow = null;
    }

    public void startBlock(Block bl) {
        para = new MIFElement("Para");
        // get font
        textFlow.addElement(para);
    }

    public void endBlock(Block bl) {
        para.finish(true);
        para = null;
    }

    public void characters(char data[], int start, int length) {
        if(para != null) {
            String str = new String(data, start, length);
            str = str.trim();
            // break into nice length chunks
            if(str.length() == 0) {
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

