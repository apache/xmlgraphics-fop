/*
 * $Id: RTFHandler.java,v 1.4 2003/03/07 09:47:56 jeremias Exp $
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
package org.apache.fop.rtf.renderer;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.control.Document;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfColorTable;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfText;
import org.xml.sax.SAXException;

/**
 * RTF Handler: generates RTF output using the structure events from
 * the FO Tree sent to this structure handler.
 *
 * @author bdelacretaz@apache.org
 */
public class RTFHandler extends FOInputHandler {

    private RtfFile rtfFile;
    private final OutputStream os;
    private RtfSection sect;
    private RtfDocumentArea docArea;
    private RtfParagraph para;
    private boolean warned = false;

    private static final String ALPHA_WARNING = "WARNING: RTF renderer is "
        + "veryveryalpha at this time, see class org.apache.fop.rtf.renderer.RTFHandler";

    /**
     * Tracks current background color. BG color is not reset automatically
     * anywhere, so we need a persistent way to see whether it has changed.
     */
    private int currentRTFBackgroundColor = -1;

    /**
     * Creates a new RTF structure handler.
     * @param os OutputStream to write to
     */
    public RTFHandler(Document doc, OutputStream os) {
        super(doc);
        this.os = os;
        // use pdf fonts for now, this is only for resolving names
        org.apache.fop.render.pdf.FontSetup.setup(doc, null);
        System.err.println(ALPHA_WARNING);
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // FIXME sections should be created
        try {
            rtfFile = new RtfFile(new OutputStreamWriter(os));
            docArea = rtfFile.startDocumentArea();
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            throw new SAXException("IOException: " + ioe);
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            rtfFile.flush();
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            throw new SAXException("IOException: " + ioe);
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler
     */
    public void startPageSequence(PageSequence pageSeq)  {
        try {
            sect = docArea.newSection();
            if (!warned) {
                sect.newParagraph().newText(ALPHA_WARNING);
                warned = true;
            }
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            throw new Error("IOException: " + ioe);
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endPageSequence(PageSequence)
     */
    public void endPageSequence(PageSequence pageSeq) throws FOPException {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
        try {
            RtfAttributes rtfAttr = new RtfAttributes();
            attrBlockTextAlign(bl, rtfAttr);
            attrBlockBackgroundColor(bl, rtfAttr);
            para = sect.newParagraph(rtfAttr);
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            throw new Error("IOException: " + ioe);
        }
    }


    /**
     * @see org.apache.fop.fo.FOInputHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startTable(Table)
     */
    public void startTable(Table tbl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endTable(Table)
     */
    public void endTable(Table tbl) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startHeader(TableBody)
     */
    public void startHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endHeader(TableBody)
     */
    public void endHeader(TableBody th) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFooter(TableBody)
     */
    public void startFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFooter(TableBody)
     */
    public void endFooter(TableBody tf) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBody(TableBody)
     */
    public void startBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endBody(TableBody)
     */
    public void endBody(TableBody tb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startRow(TableRow)
     */
    public void startRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endRow(TableRow)
     */
    public void endRow(TableRow tr) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startCell(TableCell)
     */
    public void startCell(TableCell tc) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endCell(TableCell)
     */
    public void endCell(TableCell tc) {
    }

    // Lists
    /**
     * @see org.apache.fop.fo.FOInputHandler#startList(ListBlock)
     */
    public void startList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endList(ListBlock)
     */
    public void endList(ListBlock lb) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListItem(ListItem)
     */
    public void startListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListItem(ListItem)
     */
    public void endListItem(ListItem li) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListLabel()
     */
    public void startListLabel() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListLabel()
     */
    public void endListLabel() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startListBody()
     */
    public void startListBody() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endListBody()
     */
    public void endListBody() {
    }

    // Static Regions
    /**
     * @see org.apache.fop.fo.FOInputHandler#startStatic()
     */
    public void startStatic() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endStatic()
     */
    public void endStatic() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startMarkup()
     */
    public void startMarkup() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endMarkup()
     */
    public void endMarkup() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startLink()
     */
    public void startLink() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endLink()
     */
    public void endLink() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#image(ExternalGraphic)
     */
    public void image(ExternalGraphic eg) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#pageRef()
     */
    public void pageRef() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#foreignObject(InstreamForeignObject)
     */
    public void foreignObject(InstreamForeignObject ifo) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#footnote()
     */
    public void footnote() {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#leader(Leader)
     */
    public void leader(Leader l) {
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#characters(char[], int, int)
     */
    public void characters(char data[], int start, int length) {
        try {
            para.newText(new String(data, start, length));
         } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            throw new Error("IOException: " + ioe);
        }
    }

    private void attrBlockTextAlign(Block bl, RtfAttributes rtfAttr) {
        int fopValue = bl.properties.get("text-align").getEnum();
        String rtfValue = null;
        switch (fopValue) {
            case Constants.CENTER: {
                rtfValue = RtfText.ALIGN_CENTER;
                break;
            }
            case Constants.END: {
                rtfValue = RtfText.ALIGN_RIGHT;
                break;
            }
            case Constants.JUSTIFY: {
                rtfValue = RtfText.ALIGN_JUSTIFIED;
                break;
            }
            default: {
                rtfValue = RtfText.ALIGN_LEFT;
                break;
            }
        }
        rtfAttr.set(rtfValue);
    }

    private void attrBlockBackgroundColor(Block bl, RtfAttributes rtfAttr) {
        ColorType fopValue = bl.properties.get("background-color").getColorType();
        int rtfColor = 0;
        /* FOP uses a default background color of "transparent", which is
           actually a transparent black, which is generally not suitable as a
           default here. Changing FOP's default to "white" causes problems in
           PDF output, so we will look for the default here & change it to
           white. */
        if ((fopValue.getRed() == 0) && (fopValue.getGreen() == 0)
                && (fopValue.getBlue() == 0) && (fopValue.alpha() == 1)) {
            rtfColor = RtfColorTable.getInstance().getColorNumber("white");
        } else {
            rtfColor = convertFOPColorToRTF(fopValue);
        }
        if (rtfColor != currentRTFBackgroundColor) {
            rtfAttr.set(RtfText.ATTR_BACKGROUND_COLOR, rtfColor);
            currentRTFBackgroundColor = rtfColor;
        }
    }

    /**
     * Converts a FOP ColorType to the integer pointing into the RTF color table
     * @param fopColor the ColorType object to be converted
     * @return integer pointing into the RTF color table
     */
    public static int convertFOPColorToRTF(ColorType fopColor) {
        int redComponent = ColorType.convertChannelToInteger (fopColor.getRed());
        int greenComponent = ColorType.convertChannelToInteger (fopColor.getGreen());
        int blueComponent = ColorType.convertChannelToInteger (fopColor.getBlue());
        return RtfColorTable.getInstance().getColorNumber(redComponent,
                greenComponent, blueComponent);
    }

}
