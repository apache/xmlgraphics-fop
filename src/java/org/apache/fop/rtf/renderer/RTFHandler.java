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

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.Property;
import org.apache.fop.apps.Document;
import org.apache.fop.rtf.rtflib.rtfdoc.IRtfAfterContainer;
import org.apache.fop.rtf.rtflib.rtfdoc.IRtfBeforeContainer;
import org.apache.fop.rtf.rtflib.rtfdoc.IRtfPageNumberContainer;
import org.apache.fop.rtf.rtflib.rtfdoc.IRtfParagraphContainer;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfAfter;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfBefore;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfColorTable;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfText;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.rtf.rtflib.rtfdoc.IRtfTableContainer;
import org.xml.sax.SAXException;

/**
 * RTF Handler: generates RTF output using the structure events from
 * the FO Tree sent to this structure handler.
 *
 * @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 * @author Trembicki-Guy, Ed <GuyE@DNB.com>
 * @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 * @author Peter Herweg <pherweg@web.de>
 */
public class RTFHandler extends FOInputHandler {

    private RtfFile rtfFile;
    private final OutputStream os;
    private final Logger log = new ConsoleLogger();
    private RtfSection sect;
    private RtfDocumentArea docArea;
    private RtfParagraph para;
    private boolean warned = false;
    private boolean bPrevHeaderSpecified = false;//true, if there has been a
                                                 //header in any page-sequence
    private boolean bPrevFooterSpecified = false;//true, if there has been a
                                                 //footer in any page-sequence
    private boolean bHeaderSpecified = false;  //true, if there is a header
                                               //in current page-sequence
    private boolean bFooterSpecified = false;  //true, if there is a footer
                                               //in current page-sequence
    private BuilderContext builderContext = new BuilderContext(null);

    private static final String ALPHA_WARNING = "WARNING: RTF renderer is "
        + "veryveryalpha at this time, see class org.apache.fop.rtf.renderer.RTFHandler";

    /**
     * Tracks current background color. BG color is not reset automatically
     * anywhere, so we need a persistent way to see whether it has changed.
     */
    private int currentRTFBackgroundColor = -1;

    /**
     * Creates a new RTF structure handler.
     * @param doc the Document for which this RTFHandler is processing
     * @param os OutputStream to write to
     */
    public RTFHandler(Document doc, OutputStream os) {
        super(doc);
        this.os = os;
        // use pdf fonts for now, this is only for resolving names
        org.apache.fop.render.pdf.FontSetup.setup(doc, null);
        log.warn(ALPHA_WARNING);
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
            builderContext.pushContainer(sect);

            bHeaderSpecified = false;
            bFooterSpecified = false;
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            log.error("startPageSequence: " + ioe.getMessage());
            throw new Error("IOException: " + ioe);
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endPageSequence(PageSequence)
     */
    public void endPageSequence(PageSequence pageSeq) throws FOPException {
        builderContext.popContainer();
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
        try {
            if (fl.getFlowName().equals("xsl-region-body")) {
                // if there is no header in current page-sequence but there has been
                // a header in a previous page-sequence, insert an empty header.
                if (bPrevHeaderSpecified && !bHeaderSpecified) {
                    RtfAttributes attr = new RtfAttributes();
                    attr.set(RtfBefore.HEADER);

                    final IRtfBeforeContainer contBefore =
                            (IRtfBeforeContainer)builderContext.getContainer
                                (IRtfBeforeContainer.class, true, this);
                    contBefore.newBefore(attr);
                }

                // if there is no footer in current page-sequence but there has been
                // a footer in a previous page-sequence, insert an empty footer.
                if (bPrevFooterSpecified && !bFooterSpecified) {
                    RtfAttributes attr = new RtfAttributes();
                    attr.set(RtfAfter.FOOTER);

                    final IRtfAfterContainer contAfter =
                            (IRtfAfterContainer)builderContext.getContainer
                                (IRtfAfterContainer.class, true, this);
                    contAfter.newAfter(attr);
                }

                // print ALPHA_WARNING
                if (!warned) {
                    sect.newParagraph().newText(ALPHA_WARNING);
                    warned = true;
                }
            } else if (fl.getFlowName().equals("xsl-region-before")) {
                bHeaderSpecified = true;
                bPrevHeaderSpecified = true;

                final IRtfBeforeContainer c =
                        (IRtfBeforeContainer)builderContext.getContainer(IRtfBeforeContainer.class,
                        true, this);

                RtfAttributes beforeAttributes = ((RtfElement)c).getRtfAttributes();
                if (beforeAttributes == null) {
                    beforeAttributes = new RtfAttributes();
                }
                beforeAttributes.set(RtfBefore.HEADER);

                RtfBefore before = c.newBefore(beforeAttributes);
                builderContext.pushContainer(before);
            } else if (fl.getFlowName().equals("xsl-region-after")) {
                bFooterSpecified = true;
                bPrevFooterSpecified = true;

                final IRtfAfterContainer c =
                        (IRtfAfterContainer)builderContext.getContainer(IRtfAfterContainer.class,
                        true, this);

                RtfAttributes afterAttributes = ((RtfElement)c).getRtfAttributes();
                if (afterAttributes == null) {
                    afterAttributes = new RtfAttributes();
                }

                afterAttributes.set(RtfAfter.FOOTER);

                RtfAfter after = c.newAfter(afterAttributes);
                builderContext.pushContainer(after);
            }
        } catch (IOException ioe) {
            log.error("startFlow: " + ioe.getMessage());
            throw new Error(ioe.getMessage());
        } catch (Exception e) {
            log.error("startFlow: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
        try {
            if (fl.getFlowName().equals("xsl-region-body")) {
                //just do nothing
            } else if (fl.getFlowName().equals("xsl-region-before")) {
                builderContext.popContainer();
            } else if (fl.getFlowName().equals("xsl-region-after")) {
                builderContext.popContainer();
            }
        } catch (Exception e) {
            log.error("endFlow: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
        try {
            RtfAttributes rtfAttr = new RtfAttributes();
            attrBlockTextAlign(bl, rtfAttr);
            attrBlockBackgroundColor(bl, rtfAttr);
            attrBlockFontSize(bl, rtfAttr);
            attrBlockFontWeight(bl, rtfAttr);

            IRtfParagraphContainer pc =
                    (IRtfParagraphContainer)builderContext.getContainer
                        (IRtfParagraphContainer.class, true, null);
            para = pc.newParagraph(rtfAttr);

            builderContext.pushContainer(para);
        } catch (IOException ioe) {
            // FIXME could we throw Exception in all FOInputHandler events?
            log.error("startBlock: " + ioe.getMessage());
            throw new Error("IOException: " + ioe);
        } catch (Exception e) {
            log.error("startBlock: " + e.getMessage());
            throw new Error("Exception: " + e);
        }
    }


    /**
     * @see org.apache.fop.fo.FOInputHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
        builderContext.popContainer();
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startTable(Table)
     */
    public void startTable(Table tbl) {
        // create an RtfTable in the current table container
        TableContext tableContext = new TableContext(builderContext);
        RtfAttributes atts = new RtfAttributes();

        try {
            final IRtfTableContainer tc =
                   (IRtfTableContainer)builderContext.getContainer(IRtfTableContainer.class,
                   true, null);
            builderContext.pushContainer(tc.newTable(atts, tableContext));
        } catch (Exception e) {
            log.error("startTable:" + e.getMessage());
            throw new Error(e.getMessage());
        }

        builderContext.pushTableContext(tableContext);
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endTable(Table)
     */
    public void endTable(Table tbl) {
        builderContext.popTableContext();
        builderContext.popContainer();
    }

    /**
    *
    * @param tc TableColumn that is starting;
    */

    public void startColumn(TableColumn tc) {
        try {
            Integer iWidth = new Integer(tc.getColumnWidth() / 1000);
            builderContext.getTableContext().setNextColumnWidth(iWidth.toString() + "pt");
            builderContext.getTableContext().setNextColumnRowSpanning(new Integer(0), null);
        } catch (Exception e) {
            log.error("startColumn: " + e.getMessage());
            throw new Error(e.getMessage());
        }

    }

     /**
     *
     * @param tc TableColumn that is ending;
     */

    public void endColumn(TableColumn tc) {
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
        try {
            RtfAttributes atts = TableAttributesConverter.convertRowAttributes (tb.properties,
                   null, null);

            RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class, true, this);
            tbl.setHeaderAttribs(atts);
        } catch (Exception e) {
            log.error("startBody: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endBody(TableBody)
     */
    public void endBody(TableBody tb) {
        try {
            RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class, true, this);
            tbl.setHeaderAttribs(null);
        } catch (Exception e) {
            log.error("endBody: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startRow(TableRow)
     */
    public void startRow(TableRow tr) {
        try {
            // create an RtfTableRow in the current RtfTable
            final RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class,
                    true, null);

            RtfAttributes tblAttribs = tbl.getRtfAttributes();
            RtfAttributes tblRowAttribs = new RtfAttributes();
            RtfAttributes atts = TableAttributesConverter.convertRowAttributes(tr.properties,
                    null, tbl.getHeaderAttribs());

            builderContext.pushContainer(tbl.newTableRow(atts));

            // reset column iteration index to correctly access column widths
            builderContext.getTableContext().selectFirstColumn();
        } catch (Exception e) {
            log.error("startRow: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endRow(TableRow)
     */
    public void endRow(TableRow tr) {
        builderContext.popContainer();
        builderContext.getTableContext().decreaseRowSpannings();
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startCell(TableCell)
     */
    public void startCell(TableCell tc) {
        try {
            TableContext tctx = builderContext.getTableContext();
            final RtfTableRow row = (RtfTableRow)builderContext.getContainer(RtfTableRow.class,
                    true, null);


            //while the current column is in row-spanning, act as if
            //a vertical merged cell would have been specified.
            while (tctx.getNumberOfColumns() > tctx.getColumnIndex()
                  && tctx.getColumnRowSpanningNumber().intValue() > 0) {
                row.newTableCellMergedVertically((int)tctx.getColumnWidth(),
                        tctx.getColumnRowSpanningAttrs());
                tctx.selectNextColumn();
            }

            //get the width of the currently started cell
            float width = tctx.getColumnWidth();

            // create an RtfTableCell in the current RtfTableRow
            RtfAttributes atts = TableAttributesConverter.convertCellAttributes(tc.properties,
                    null);
            RtfTableCell cell = row.newTableCell((int)width, atts);

            //process number-rows-spanned attribute
            Property p = null;
            if ((p = tc.properties.get("number-rows-spanned")) != null && false) {
                // Start vertical merge
                cell.setVMerge(RtfTableCell.MERGE_START);

                // set the number of rows spanned
                tctx.setCurrentColumnRowSpanning(new Integer(p.getNumber().intValue()),
                        cell.getRtfAttributes());
            } else {
                tctx.setCurrentColumnRowSpanning(new Integer(1), null);
            }

            builderContext.pushContainer(cell);
        } catch (Exception e) {
            log.error("startCell: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endCell(TableCell)
     */
    public void endCell(TableCell tc) {
        builderContext.popContainer();
        builderContext.getTableContext().selectNextColumn();
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
            log.error("characters: " + ioe.getMessage());
            throw new Error("IOException: " + ioe);
        }
    }

    private void attrBlockFontSize(Block bl, RtfAttributes rtfAttr) {
        int fopValue = bl.properties.get("font-size").getLength().getValue() / 500;
        rtfAttr.set("fs", fopValue);
    }

    private void attrBlockFontWeight(Block bl, RtfAttributes rtfAttr) {
        String fopValue = bl.properties.get("font-weight").getString();
        if (fopValue == "bold" || fopValue == "700") {
            rtfAttr.set("b", 1);
        } else {
            rtfAttr.set("b", 0);
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
                && (fopValue.getBlue() == 0) && (fopValue.getAlpha() == 0)) {
            rtfColor = RtfColorTable.getInstance().getColorNumber("white").intValue();
            currentRTFBackgroundColor = -1;
            return;
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
                greenComponent, blueComponent).intValue();
    }

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public void startPageNumber(PageNumber pagenum) {
        try {
            //insert page number
            IRtfPageNumberContainer pageNumberContainer =
                    (IRtfPageNumberContainer)builderContext.getContainer
                        (IRtfPageNumberContainer.class, true, this);
            builderContext.pushContainer(pageNumberContainer.newPageNumber());

            //set Attribute "WhiteSpaceFalse" in order to prevent the rtf library from
            //stripping the whitespaces. This applies to whole paragraph.
            if (pageNumberContainer instanceof RtfParagraph) {
                RtfParagraph para = (RtfParagraph)pageNumberContainer;
                para.getRtfAttributes().set("WhiteSpaceFalse");
            }
        } catch (Exception e) {
            log.error("startPageNumber: " + e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public void endPageNumber(PageNumber pagenum) {
        builderContext.popContainer();
    }
}
