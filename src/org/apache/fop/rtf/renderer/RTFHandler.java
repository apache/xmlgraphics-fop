/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.rtf.renderer;

import org.apache.fop.apps.StructureHandler;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.apps.FOPException;

import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.Title;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Flow;

import org.jfor.jfor.rtflib.rtfdoc.RtfFile;
import org.jfor.jfor.rtflib.rtfdoc.RtfSection;
import org.jfor.jfor.rtflib.rtfdoc.RtfParagraph;
import org.jfor.jfor.rtflib.rtfdoc.RtfDocumentArea;

import org.xml.sax.SAXException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * RTF Handler: generates RTF output using the structure events from
 * the FO Tree sent to this structure handler.
 *
 *  @author bdelacretaz@apache.org
 */

 public class RTFHandler extends StructureHandler {
    private FontInfo _fontInfo = new FontInfo();
    private RtfFile _rtfFile;
    private RtfSection _sect;
    private RtfDocumentArea _docArea;
    private RtfParagraph _para;
    private boolean _warned = false;
    
    private static final String ALPHA_WARNING = "WARNING: RTF renderer is veryveryalpha at this time, see class org.apache.fop.rtf.renderer.RTFHandler";

    public RTFHandler(OutputStream os) throws IOException {
        _rtfFile = new RtfFile(new OutputStreamWriter(os));
        // use pdf fonts for now, this is only for resolving names
        org.apache.fop.render.pdf.FontSetup.setup(_fontInfo, null);
        System.err.println(ALPHA_WARNING);
    }

    public FontInfo getFontInfo() {
        return _fontInfo;
    }

    public void startDocument() throws SAXException {
        // FIXME sections should be created 
        try {
            _docArea = _rtfFile.startDocumentArea();
        } catch(IOException ioe) {
            // FIXME could we throw Exception in all StructureHandler events?
            throw new SAXException("IOException: " + ioe);
        }
    }

    public void endDocument() throws SAXException {
        try {
            _rtfFile.flush();
        } catch(IOException ioe) {
            // FIXME could we throw Exception in all StructureHandler events?
            throw new SAXException("IOException: " + ioe);
        }
    }

    public void startPageSequence(PageSequence pageSeq, Title seqTitle, LayoutMasterSet lms)  {
        try {
            _sect = _docArea.newSection();
            if(!_warned) {
                _sect.newParagraph().newText(ALPHA_WARNING);
                _warned = true;
            }
        } catch(IOException ioe) {
            // FIXME could we throw Exception in all StructureHandler events?
            throw new Error("IOException: " + ioe);
        }
    }

    public void endPageSequence(PageSequence pageSeq) throws FOPException {
    }

    public void startFlow(Flow fl) {
    }

    public void endFlow(Flow fl) {
    }

    public void startBlock(Block bl) {
        try {
            _para = _sect.newParagraph();
        } catch(IOException ioe) {
            // FIXME could we throw Exception in all StructureHandler events?
            throw new Error("IOException: " + ioe);
        }
    }

    public void endBlock(Block bl) {
    }

    public void characters(char data[], int start, int length) {
        try {
            _para.newText(new String(data,start,length));
         } catch(IOException ioe) {
            // FIXME could we throw Exception in all StructureHandler events?
            throw new Error("IOException: " + ioe);
        }
   }
}
