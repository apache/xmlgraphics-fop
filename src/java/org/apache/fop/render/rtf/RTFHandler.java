/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.rtf;

// Java
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.render.DefaultFontResolver;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfAfterContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfBeforeContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfListContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfTableContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfTextrunContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAfter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfBefore;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfExternalGraphic;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFootnote;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfHyperLink;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfList;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem.RtfListItemLabel;
import org.apache.fop.render.rtf.rtflib.tools.BuilderContext;
import org.apache.fop.render.rtf.rtflib.tools.TableContext;

/**
 * RTF Handler: generates RTF output using the structure events from
 * the FO Tree sent to this structure handler.
 *
 * @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 * @author Trembicki-Guy, Ed <GuyE@DNB.com>
 * @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 * @author Peter Herweg <pherweg@web.de>
 * @author Andreas Putz <a.putz@skynamics.com>
 */
public class RTFHandler extends FOEventHandler {

    private RtfFile rtfFile;
    private final OutputStream os;
    private static Log log = LogFactory.getLog(RTFHandler.class);
    private RtfSection sect;
    private RtfDocumentArea docArea;
    private boolean bDefer;              //true, if each called handler shall be
                                         //processed at later time.
    private boolean bPrevHeaderSpecified = false; //true, if there has been a
                                                  //header in any page-sequence
    private boolean bPrevFooterSpecified = false; //true, if there has been a
                                                  //footer in any page-sequence
    private boolean bHeaderSpecified = false;  //true, if there is a header
                                               //in current page-sequence
    private boolean bFooterSpecified = false;  //true, if there is a footer
                                               //in current page-sequence
    private BuilderContext builderContext = new BuilderContext(null);

    private SimplePageMaster pagemaster;

    /**
     * Creates a new RTF structure handler.
     * @param userAgent the FOUserAgent for this process
     * @param os OutputStream to write to
     */
    public RTFHandler(FOUserAgent userAgent, OutputStream os) {
        super(userAgent);
        this.os = os;
        bDefer = true;

        FontSetup.setup(fontInfo, null, new DefaultFontResolver(userAgent));
    }

    /**
     * Central exception handler for I/O exceptions.
     * @param ioe IOException to handle
     */
    protected void handleIOTrouble(IOException ioe) {
        RendererEventProducer eventProducer = RendererEventProducer.Factory.create(
                getUserAgent().getEventBroadcaster());
        eventProducer.ioError(this, ioe);
    }

    /**
     * {@inheritDoc}
     */
    public void startDocument() throws SAXException {
        // TODO sections should be created
        try {
            rtfFile = new RtfFile(new OutputStreamWriter(os));
            docArea = rtfFile.startDocumentArea();
        } catch (IOException ioe) {
            // TODO could we throw Exception in all FOEventHandler events?
            throw new SAXException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endDocument() throws SAXException {
        try {
            rtfFile.flush();
        } catch (IOException ioe) {
            // TODO could we throw Exception in all FOEventHandler events?
            throw new SAXException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startPageSequence(PageSequence pageSeq)  {
        try {
            //This is needed for region handling
            if (this.pagemaster == null) {
                String reference = pageSeq.getMasterReference();
                this.pagemaster
                        = pageSeq.getRoot().getLayoutMasterSet().getSimplePageMaster(reference);
                if (this.pagemaster == null) {
                    RTFEventProducer eventProducer = RTFEventProducer.Factory.create(
                            getUserAgent().getEventBroadcaster());
                    eventProducer.onlySPMSupported(this, reference, pageSeq.getLocator());
                    PageSequenceMaster master 
                        = pageSeq.getRoot().getLayoutMasterSet().getPageSequenceMaster(reference);
                    this.pagemaster = master.getNextSimplePageMaster(
                            false, false, false, false, false);
                }
            }

            if (bDefer) {
                return;
            }

            sect = docArea.newSection();

            //read page size and margins, if specified
            //only simple-page-master supported, so pagemaster may be null
            if (pagemaster != null) {
                sect.getRtfAttributes().set(
                    PageAttributesConverter.convertPageAttributes(
                            pagemaster));
            } else {
                RTFEventProducer eventProducer = RTFEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.noSPMFound(this, pageSeq.getLocator());
            }

            builderContext.pushContainer(sect);

            bHeaderSpecified = false;
            bFooterSpecified = false;
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (FOPException fope) {
            // TODO could we throw Exception in all FOEventHandler events?
            log.error("startPageSequence: " + fope.getMessage(), fope);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endPageSequence(PageSequence pageSeq) {
        if (bDefer) {
            //If endBlock was called while SAX parsing, and the passed FO is Block
            //nested within another Block, stop deferring.
            //Now process all deferred FOs.
            bDefer = false;
            recurseFONode(pageSeq);
            this.pagemaster = null;
            bDefer = true;

            return;
        } else {
            builderContext.popContainer();
            this.pagemaster = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startFlow(Flow fl) {
        if (bDefer) {
            return;
        }

        try {
            log.debug("starting flow: " + fl.getFlowName());
            boolean handled = false;
            Region regionBody = pagemaster.getRegion(Constants.FO_REGION_BODY);
            Region regionBefore = pagemaster.getRegion(Constants.FO_REGION_BEFORE);
            Region regionAfter = pagemaster.getRegion(Constants.FO_REGION_AFTER);
            if (fl.getFlowName().equals(regionBody.getRegionName())) {
                // if there is no header in current page-sequence but there has been
                // a header in a previous page-sequence, insert an empty header.
                if (bPrevHeaderSpecified && !bHeaderSpecified) {
                    RtfAttributes attr = new RtfAttributes();
                    attr.set(RtfBefore.HEADER);

                    final IRtfBeforeContainer contBefore
                        = (IRtfBeforeContainer)builderContext.getContainer
                                (IRtfBeforeContainer.class, true, this);
                    contBefore.newBefore(attr);
                }

                // if there is no footer in current page-sequence but there has been
                // a footer in a previous page-sequence, insert an empty footer.
                if (bPrevFooterSpecified && !bFooterSpecified) {
                    RtfAttributes attr = new RtfAttributes();
                    attr.set(RtfAfter.FOOTER);

                    final IRtfAfterContainer contAfter
                        = (IRtfAfterContainer)builderContext.getContainer
                                (IRtfAfterContainer.class, true, this);
                    contAfter.newAfter(attr);
                }
                handled = true;
            } else if (regionBefore != null 
                    && fl.getFlowName().equals(regionBefore.getRegionName())) {
                bHeaderSpecified = true;
                bPrevHeaderSpecified = true;

                final IRtfBeforeContainer c
                    = (IRtfBeforeContainer)builderContext.getContainer(
                        IRtfBeforeContainer.class,
                        true, this);

                RtfAttributes beforeAttributes = ((RtfElement)c).getRtfAttributes();
                if (beforeAttributes == null) {
                    beforeAttributes = new RtfAttributes();
                }
                beforeAttributes.set(RtfBefore.HEADER);

                RtfBefore before = c.newBefore(beforeAttributes);
                builderContext.pushContainer(before);
                handled = true;
            } else if (regionAfter != null 
                    && fl.getFlowName().equals(regionAfter.getRegionName())) {
                bFooterSpecified = true;
                bPrevFooterSpecified = true;

                final IRtfAfterContainer c
                    = (IRtfAfterContainer)builderContext.getContainer(
                        IRtfAfterContainer.class,
                        true, this);

                RtfAttributes afterAttributes = ((RtfElement)c).getRtfAttributes();
                if (afterAttributes == null) {
                    afterAttributes = new RtfAttributes();
                }

                afterAttributes.set(RtfAfter.FOOTER);

                RtfAfter after = c.newAfter(afterAttributes);
                builderContext.pushContainer(after);
                handled = true;
            }
            if (!handled) {
                log.warn("A " + fl.getLocalName() + " has been skipped: " + fl.getFlowName());
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startFlow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endFlow(Flow fl) {
        if (bDefer) {
            return;
        }

        try {
            Region regionBody = pagemaster.getRegion(Constants.FO_REGION_BODY);
            Region regionBefore = pagemaster.getRegion(Constants.FO_REGION_BEFORE);
            Region regionAfter = pagemaster.getRegion(Constants.FO_REGION_AFTER);
            if (fl.getFlowName().equals(regionBody.getRegionName())) {
                //just do nothing
            } else if (regionBefore != null 
                    && fl.getFlowName().equals(regionBefore.getRegionName())) {
                builderContext.popContainer();
            } else if (regionAfter != null 
                    && fl.getFlowName().equals(regionAfter.getRegionName())) {
                builderContext.popContainer();
            }
        } catch (Exception e) {
            log.error("endFlow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startBlock(Block bl) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertAttributes(bl);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.pushBlockAttributes(rtfAttr);
            textrun.addBookmark(bl.getId());
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startBlock: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void endBlock(Block bl) {

        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.popBlockAttributes();

        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startBlock:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startBlockContainer(BlockContainer blc) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertBlockContainerAttributes(blc);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.pushBlockAttributes(rtfAttr);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startBlock: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endBlockContainer(BlockContainer bl) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.popBlockAttributes();

        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startBlock:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startTable(Table tbl) {
        if (bDefer) {
            return;
        }

        // create an RtfTable in the current table container
        TableContext tableContext = new TableContext(builderContext);

        try {
            final IRtfTableContainer tc
                = (IRtfTableContainer)builderContext.getContainer(
                        IRtfTableContainer.class, true, null);
            
            RtfAttributes atts
                = TableAttributesConverter.convertTableAttributes(tbl);
            
            RtfTable table = tc.newTable(atts, tableContext);
            
            CommonBorderPaddingBackground border = tbl.getCommonBorderPaddingBackground();
            RtfAttributes borderAttributes = new RtfAttributes();
                    
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.BEFORE,
                    borderAttributes, ITableAttributes.CELL_BORDER_TOP);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.AFTER,
                    borderAttributes, ITableAttributes.CELL_BORDER_BOTTOM);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.START,
                    borderAttributes, ITableAttributes.CELL_BORDER_LEFT);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.END,
                    borderAttributes,  ITableAttributes.CELL_BORDER_RIGHT);
            
            table.setBorderAttributes(borderAttributes);
            
            builderContext.pushContainer(table);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startTable:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        builderContext.pushTableContext(tableContext);
    }

    /**
     * {@inheritDoc}
     */
    public void endTable(Table tbl) {
        if (bDefer) {
            return;
        }

        builderContext.popTableContext();
        builderContext.popContainer();
    }

    /**
    *
    * @param tc TableColumn that is starting;
    */

    public void startColumn(TableColumn tc) {
        if (bDefer) {
            return;
        }

        try {
            /**
             * Pass a SimplePercentBaseContext to getValue in order to
             * avoid a NullPointerException, which occurs when you use
             * proportional-column-width function in column-width attribute.
             * Of course the results won't be correct, but at least the
             * rest of the document will be rendered. Usage of the
             * TableLayoutManager is not welcome due to design reasons and
             * it also does not provide the correct values.
             * TODO: Make proportional-column-width working for rtf output 
             */
             SimplePercentBaseContext context
                = new SimplePercentBaseContext(null,
                                               LengthBase.TABLE_UNITS,
                                               100000);
            
            Integer iWidth
                = new Integer(tc.getColumnWidth().getValue(context) / 1000);
            
            String strWidth = iWidth.toString() + FixedLength.POINT;
            Float width = new Float(
                    FoUnitsConverter.getInstance().convertToTwips(strWidth));
            builderContext.getTableContext().setNextColumnWidth(width);
            builderContext.getTableContext().setNextColumnRowSpanning(
                    new Integer(0), null);
            builderContext.getTableContext().setNextFirstSpanningCol(false);
        } catch (Exception e) {
            log.error("startColumn: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

     /**
     *
     * @param tc TableColumn that is ending;
     */

    public void endColumn(TableColumn tc) {
        if (bDefer) {
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startHeader(TableBody th) {
    }

    /**
     * {@inheritDoc}
     */
    public void endHeader(TableBody th) {
    }

    /**
     * {@inheritDoc}
     */
    public void startFooter(TableBody tf) {
    }

    /**
     * {@inheritDoc}
     */
    public void endFooter(TableBody tf) {
    }

    /**
     *
     * @param inl Inline that is starting.
     */
    public void startInline(Inline inl) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertCharacterAttributes(inl);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.pushInlineAttributes(rtfAttr);
            textrun.addBookmark(inl.getId());
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (FOPException fe) {
            log.error("startInline:" + fe.getMessage());
            throw new RuntimeException(fe.getMessage());
        } catch (Exception e) {
            log.error("startInline:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param inl Inline that is ending.
     */
    public void endInline(Inline inl) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.popInlineAttributes();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startInline:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

     /**
     * {@inheritDoc}
     */
    public void startBody(TableBody tb) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes atts = TableAttributesConverter.convertTableBodyAttributes(tb);

            RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class, true, this);
            tbl.setHeaderAttribs(atts);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startBody: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endBody(TableBody tb) {
        if (bDefer) {
            return;
        }

        try {
            RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class, true, this);
            tbl.setHeaderAttribs(null);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("endBody: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startRow(TableRow tr) {
        if (bDefer) {
            return;
        }

        try {
            // create an RtfTableRow in the current RtfTable
            final RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class,
                    true, null);

            RtfAttributes atts = TableAttributesConverter.convertRowAttributes(tr,
                    tbl.getHeaderAttribs());

            if (tr.getParent() instanceof TableHeader) {
                atts.set(ITableAttributes.ATTR_HEADER);
            }

            builderContext.pushContainer(tbl.newTableRow(atts));

            // reset column iteration index to correctly access column widths
            builderContext.getTableContext().selectFirstColumn();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startRow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endRow(TableRow tr) {
        if (bDefer) {
            return;
        }
        
        try {
            TableContext tctx = builderContext.getTableContext();
            final RtfTableRow row = (RtfTableRow)builderContext.getContainer(RtfTableRow.class,
                    true, null);

            //while the current column is in row-spanning, act as if
            //a vertical merged cell would have been specified.
            while (tctx.getNumberOfColumns() > tctx.getColumnIndex()
                  && tctx.getColumnRowSpanningNumber().intValue() > 0) {
                RtfTableCell vCell = row.newTableCellMergedVertically(
                        (int)tctx.getColumnWidth(),
                        tctx.getColumnRowSpanningAttrs());
                
                if (!tctx.getFirstSpanningCol()) {
                    vCell.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
                }
                
                tctx.selectNextColumn();
            }
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("endRow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }


        builderContext.popContainer();
        builderContext.getTableContext().decreaseRowSpannings();
    }

    /**
     * {@inheritDoc}
     */
    public void startCell(TableCell tc) {
        if (bDefer) {
            return;
        }

        try {
            TableContext tctx = builderContext.getTableContext();
            final RtfTableRow row = (RtfTableRow)builderContext.getContainer(RtfTableRow.class,
                    true, null);

            int numberRowsSpanned = tc.getNumberRowsSpanned();
            int numberColumnsSpanned = tc.getNumberColumnsSpanned();

            //while the current column is in row-spanning, act as if
            //a vertical merged cell would have been specified.
            while (tctx.getNumberOfColumns() > tctx.getColumnIndex()
                  && tctx.getColumnRowSpanningNumber().intValue() > 0) {
                RtfTableCell vCell = row.newTableCellMergedVertically(
                        (int)tctx.getColumnWidth(),
                        tctx.getColumnRowSpanningAttrs());
                
                if (!tctx.getFirstSpanningCol()) {
                    vCell.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
                }
                
                tctx.selectNextColumn();
            }

            //get the width of the currently started cell
            float width = tctx.getColumnWidth();

            // create an RtfTableCell in the current RtfTableRow
            RtfAttributes atts = TableAttributesConverter.convertCellAttributes(tc);
            RtfTableCell cell = row.newTableCell((int)width, atts);
            
            //process number-rows-spanned attribute
            if (numberRowsSpanned > 1) {
                // Start vertical merge
                cell.setVMerge(RtfTableCell.MERGE_START);

                // set the number of rows spanned
                tctx.setCurrentColumnRowSpanning(new Integer(numberRowsSpanned), 
                        cell.getRtfAttributes());
            } else {
                tctx.setCurrentColumnRowSpanning(
                        new Integer(numberRowsSpanned), null);
            }

            //process number-columns-spanned attribute
            if (numberColumnsSpanned > 0) {
                // Get the number of columns spanned
                RtfTable table = row.getTable();
                tctx.setCurrentFirstSpanningCol(true);
                
                // We widthdraw one cell because the first cell is already created
                // (it's the current cell) !
                 for (int i = 0; i < numberColumnsSpanned - 1; ++i) {
                    tctx.selectNextColumn();
                    
                    tctx.setCurrentFirstSpanningCol(false);
                    RtfTableCell hCell = row.newTableCellMergedHorizontally(
                            0, null);
                    
                    if (numberRowsSpanned > 1) {
                        // Start vertical merge
                        hCell.setVMerge(RtfTableCell.MERGE_START);

                        // set the number of rows spanned
                        tctx.setCurrentColumnRowSpanning(
                                new Integer(numberRowsSpanned), 
                                cell.getRtfAttributes());
                    } else {
                        tctx.setCurrentColumnRowSpanning(
                                new Integer(numberRowsSpanned), null);
                    }
                }
            }
            
            builderContext.pushContainer(cell);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startCell: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endCell(TableCell tc) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
        builderContext.getTableContext().selectNextColumn();
    }

    // Lists
    /**
     * {@inheritDoc}
     */
    public void startList(ListBlock lb) {
        if (bDefer) {
            return;
        }

        try  {
            // create an RtfList in the current list container
            final IRtfListContainer c
                = (IRtfListContainer)builderContext.getContainer(
                    IRtfListContainer.class, true, this);
            final RtfList newList = c.newList(
                ListAttributesConverter.convertAttributes(lb));
            builderContext.pushContainer(newList);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (FOPException fe) {
            log.error("startList: " + fe.getMessage());
            throw new RuntimeException(fe.getMessage());
        } catch (Exception e) {
            log.error("startList: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endList(ListBlock lb) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
    }

    /**
     * {@inheritDoc}
     */
    public void startListItem(ListItem li) {
        if (bDefer) {
            return;
        }
        
        // create an RtfListItem in the current RtfList
        try {
            RtfList list = (RtfList)builderContext.getContainer(
                    RtfList.class, true, this);
            
            /**
             * If the current list already contains a list item, then close the
             * list and open a new one, so every single list item gets its own
             * list. This allows every item to have a different list label.
             * If all the items would be in the same list, they had all the
             * same label.
             */
            //TODO: do this only, if the labels content <> previous labels content
            if (list.getChildCount() > 0) {
                this.endListBody();
                this.endList((ListBlock) li.getParent());
                this.startList((ListBlock) li.getParent());
                this.startListBody();
                
                list = (RtfList)builderContext.getContainer(
                        RtfList.class, true, this);
            }            
            
            builderContext.pushContainer(list.newListItem());
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startList: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endListItem(ListItem li) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
    }

    /**
     * {@inheritDoc}
     */
    public void startListLabel() {
        if (bDefer) {
            return;
        }

        try {
            RtfListItem item
                = (RtfListItem)builderContext.getContainer(RtfListItem.class, true, this);

            RtfListItemLabel label = item.new RtfListItemLabel(item);
            builderContext.pushContainer(label);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startPageNumber: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endListLabel() {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
    }

    /**
     * {@inheritDoc}
     */
    public void startListBody() {
    }

    /**
     * {@inheritDoc}
     */
    public void endListBody() {
    }

    // Static Regions
    /**
     * {@inheritDoc}
     */
    public void startStatic() {
    }

    /**
     * {@inheritDoc}
     */
    public void endStatic() {
    }

    /**
     * {@inheritDoc}
     */
    public void startMarkup() {
    }

    /**
     * {@inheritDoc}
     */
    public void endMarkup() {
    }

    /**
     * {@inheritDoc}
     */
    public void startLink(BasicLink basicLink) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();

            RtfHyperLink link = textrun.addHyperlink(new RtfAttributes());

            if (basicLink.hasExternalDestination()) {
                link.setExternalURL(basicLink.getExternalDestination());
            } else {
                link.setInternalURL(basicLink.getInternalDestination());
            }

            builderContext.pushContainer(link);

        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startLink: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endLink() {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
    }

    /**
     * {@inheritDoc}
     */
    public void image(ExternalGraphic eg) {
        if (bDefer) {
            return;
        }

        String uri = eg.getURL();
        ImageInfo info = null;
        try {

            //set image data
            FOUserAgent userAgent = eg.getUserAgent();
            ImageManager manager = userAgent.getFactory().getImageManager();
            info = manager.getImageInfo(uri, userAgent.getImageSessionContext());
            
            putGraphic(eg, info);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void foreignObject(InstreamForeignObject ifo) {
        if (bDefer) {
            return;
        }
        
        try {
            XMLObj child = (XMLObj) ifo.getChildXMLObj();
            Document doc = child.getDOMDocument();
            String ns = child.getNamespaceURI();
            
            ImageInfo info = new ImageInfo(null, null);
            // Set the resolution to that of the FOUserAgent
            FOUserAgent ua = ifo.getUserAgent();
            ImageSize size = new ImageSize();
            size.setResolution(ua.getSourceResolution());
            
            // Set the image size to the size of the svg.
            Point2D csize = new Point2D.Float(-1, -1);
            Point2D intrinsicDimensions = child.getDimension(csize);
            if (intrinsicDimensions == null) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.ifoNoIntrinsicSize(this, child.getLocator());
                return;
            }
            size.setSizeInMillipoints(
                    (int)Math.round(intrinsicDimensions.getX() * 1000),
                    (int)Math.round(intrinsicDimensions.getY() * 1000));
            size.calcPixelsFromSize();
            info.setSize(size);

            ImageXMLDOM image = new ImageXMLDOM(info, doc, ns);
            
            FOUserAgent userAgent = ifo.getUserAgent();
            ImageManager manager = userAgent.getFactory().getImageManager();
            Image converted = manager.convertImage(image, FLAVORS);
            putGraphic(ifo, converted);
            
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, null, ie, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, null, ioe, null);
        }
    }

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_EMF, ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
    };
    
    /**
     * Puts a graphic/image into the generated RTF file.
     * @param abstractGraphic the graphic (external-graphic or instream-foreign-object)
     * @param info the image info object
     * @throws IOException In case of an I/O error
     */
    private void putGraphic(AbstractGraphics abstractGraphic, ImageInfo info) 
            throws IOException {
        try {
            FOUserAgent userAgent = abstractGraphic.getUserAgent();
            ImageManager manager = userAgent.getFactory().getImageManager();
            ImageSessionContext sessionContext = userAgent.getImageSessionContext();
            Map hints = ImageUtil.getDefaultHints(sessionContext);
            Image image = manager.getImage(info, FLAVORS, hints, sessionContext);

            putGraphic(abstractGraphic, image);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, null, ie, null);
        }
    }
    
    /**
     * Puts a graphic/image into the generated RTF file.
     * @param abstractGraphic the graphic (external-graphic or instream-foreign-object)
     * @param image the image
     * @throws IOException In case of an I/O error
     */
    private void putGraphic(AbstractGraphics abstractGraphic, Image image) 
            throws IOException {
        byte[] rawData = null;
        
        ImageInfo info = image.getInfo();

        if (image instanceof ImageRawStream) {
            ImageRawStream rawImage = (ImageRawStream)image;
            InputStream in = rawImage.createInputStream();
            try {
                rawData = IOUtils.toByteArray(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        if (rawData == null) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, null);
            return;
        }

        final IRtfTextrunContainer c
            = (IRtfTextrunContainer)builderContext.getContainer(
                IRtfTextrunContainer.class, true, this);

        final RtfExternalGraphic rtfGraphic = c.getTextrun().newImage();
   
        //set URL
        if (info.getOriginalURI() != null) {
            rtfGraphic.setURL(info.getOriginalURI());
        }
        rtfGraphic.setImageData(rawData);

        //set scaling
        if (abstractGraphic.getScaling() == Constants.EN_UNIFORM) {
            rtfGraphic.setScaling ("uniform");
        }

        //get width
        int width = 0;
        if (abstractGraphic.getWidth().getEnum() == Constants.EN_AUTO) {
            width = info.getSize().getWidthMpt();
        } else {
            width = abstractGraphic.getWidth().getValue();
        }

        //get height
        int height = 0;
        if (abstractGraphic.getWidth().getEnum() == Constants.EN_AUTO) {
            height = info.getSize().getHeightMpt();
        } else {
            height = abstractGraphic.getHeight().getValue();
        }

        //get content-width
        int contentwidth = 0;
        if (abstractGraphic.getContentWidth().getEnum()
                == Constants.EN_AUTO) {
            contentwidth = info.getSize().getWidthMpt();
        } else if (abstractGraphic.getContentWidth().getEnum()
                == Constants.EN_SCALE_TO_FIT) {
            contentwidth = width;
        } else {
            //TODO: check, if the value is a percent value
            contentwidth = abstractGraphic.getContentWidth().getValue();
        }

        //get content-width
        int contentheight = 0;
        if (abstractGraphic.getContentHeight().getEnum()
                == Constants.EN_AUTO) {

            contentheight = info.getSize().getHeightMpt();

        } else if (abstractGraphic.getContentHeight().getEnum()
                == Constants.EN_SCALE_TO_FIT) {

            contentheight = height;
        } else {
            //TODO: check, if the value is a percent value
            contentheight = abstractGraphic.getContentHeight().getValue();
        }

        //set width in rtf
        //newGraphic.setWidth((long) (contentwidth / 1000f) + FixedLength.POINT);
        rtfGraphic.setWidth((long) (contentwidth / 50f) + "twips");

        //set height in rtf
        //newGraphic.setHeight((long) (contentheight / 1000f) + FixedLength.POINT);
        rtfGraphic.setHeight((long) (contentheight / 50f) + "twips");

        //TODO: make this configurable:
        //      int compression = m_context.m_options.getRtfExternalGraphicCompressionRate ();
        int compression = 0;
        if (compression != 0) {
            if (!rtfGraphic.setCompressionRate(compression)) {
                log.warn("The compression rate " + compression
                    + " is invalid. The value has to be between 1 and 100 %.");
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void pageRef() {
    }

    /**
     * {@inheritDoc}
     */
    public void startFootnote(Footnote footnote) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();
            RtfFootnote rtfFootnote = textrun.addFootnote();

            builderContext.pushContainer(rtfFootnote);

        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startFootnote: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endFootnote(Footnote footnote) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer();
    }

    /**
     * {@inheritDoc}
     */
    public void startFootnoteBody(FootnoteBody body) {
        if (bDefer) {
            return;
        }

        try {
            RtfFootnote rtfFootnote
                = (RtfFootnote)builderContext.getContainer(
                    RtfFootnote.class,
                    true, this);

            rtfFootnote.startBody();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startFootnoteBody: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endFootnoteBody(FootnoteBody body) {
        if (bDefer) {
            return;
        }

        try {
            RtfFootnote rtfFootnote
                = (RtfFootnote)builderContext.getContainer(
                    RtfFootnote.class,
                    true, this);

            rtfFootnote.endBody();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("endFootnoteBody: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void leader(Leader l) {
    }

    /**
     * @param text FOText object
     * @param data Array of characters to process.
     * @param start Offset for characters to process.
     * @param length Portion of array to process.
     */
    public void text(FOText text, char[] data, int start, int length) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertCharacterAttributes(text);

            textrun.pushInlineAttributes(rtfAttr);
            textrun.addString(new String(data, start, length - start));
            textrun.popInlineAttributes();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("characters:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public void startPageNumber(PageNumber pagenum) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertCharacterAttributes(
                    pagenum);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.addPageNumber(rtfAttr);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        } catch (Exception e) {
            log.error("startPageNumber: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public void endPageNumber(PageNumber pagenum) {
        if (bDefer) {
            return;
        }
    }

    /**
     * Calls the appropriate event handler for the passed FObj.
     *
     * @param foNode FO node whose event is to be called
     * @param bStart TRUE calls the start handler, FALSE the end handler
     */
    private void invokeDeferredEvent(FONode foNode, boolean bStart) {
        if (foNode instanceof PageSequence) {
            if (bStart) {
                startPageSequence( (PageSequence) foNode);
            } else {
                endPageSequence( (PageSequence) foNode);
            }
        } else if (foNode instanceof Flow) {
            if (bStart) {
                startFlow( (Flow) foNode);
            } else {
                endFlow( (Flow) foNode);
            }
        } else if (foNode instanceof StaticContent) {
            if (bStart) {
                startStatic();
            } else {
                endStatic();
            }
        } else if (foNode instanceof ExternalGraphic) {
            if (bStart) {
                image( (ExternalGraphic) foNode );
            }
        } else if (foNode instanceof InstreamForeignObject) {
            if (bStart) {
                foreignObject( (InstreamForeignObject) foNode );
            }
        } else if (foNode instanceof Block) {
            if (bStart) {
                startBlock( (Block) foNode);
            } else {
                endBlock( (Block) foNode);
            }
        } else if (foNode instanceof BlockContainer) {
            if (bStart) {
                startBlockContainer( (BlockContainer) foNode);
            } else {
                endBlockContainer( (BlockContainer) foNode);
            }
        } else if (foNode instanceof BasicLink) {
            //BasicLink must be placed before Inline
            if (bStart) {
                startLink( (BasicLink) foNode);
            } else {
                endLink();
            }
        } else if (foNode instanceof Inline) {
            if (bStart) {
                startInline( (Inline) foNode);
            } else {
                endInline( (Inline) foNode);
            }
        } else if (foNode instanceof FOText) {
            if (bStart) {
                FOText text = (FOText) foNode;
                text(text, text.ca, text.startIndex, text.endIndex);
            }
        } else if (foNode instanceof Character) {
            if (bStart) {
                Character c = (Character) foNode;
                character(c);
            }
        } else if (foNode instanceof PageNumber) {
            if (bStart) {
                startPageNumber( (PageNumber) foNode);
            } else {
                endPageNumber( (PageNumber) foNode);
            }
        } else if (foNode instanceof Footnote) {
            if (bStart) {
                startFootnote( (Footnote) foNode);
            } else {
                endFootnote( (Footnote) foNode);
            }
        } else if (foNode instanceof FootnoteBody) {
            if (bStart) {
                startFootnoteBody( (FootnoteBody) foNode);
            } else {
                endFootnoteBody( (FootnoteBody) foNode);
            }
        } else if (foNode instanceof ListBlock) {
            if (bStart) {
                startList( (ListBlock) foNode);
            } else {
                endList( (ListBlock) foNode);
            }
        } else if (foNode instanceof ListItemBody) {
            if (bStart) {
                startListBody();
            } else {
                endListBody();
            }
        } else if (foNode instanceof ListItem) {
            if (bStart) {
                startListItem( (ListItem) foNode);
            } else {
                endListItem( (ListItem) foNode);
            }
        } else if (foNode instanceof ListItemLabel) {
            if (bStart) {
                startListLabel();
            } else {
                endListLabel();
            }
        } else if (foNode instanceof Table) {
            if (bStart) {
                startTable( (Table) foNode);
            } else {
                endTable( (Table) foNode);
            }
        } else if (foNode instanceof TableBody) {
            if (bStart) {
                startBody( (TableBody) foNode);
            } else {
                endBody( (TableBody) foNode);
            }
        } else if (foNode instanceof TableColumn) {
            if (bStart) {
                startColumn( (TableColumn) foNode);
            } else {
                endColumn( (TableColumn) foNode);
            }
        } else if (foNode instanceof TableRow) {
            if (bStart) {
                startRow( (TableRow) foNode);
            } else {
                endRow( (TableRow) foNode);
            }
        } else if (foNode instanceof TableCell) {
            if (bStart) {
                startCell( (TableCell) foNode);
            } else {
                endCell( (TableCell) foNode);
            }
        } else {
            RTFEventProducer eventProducer = RTFEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.ignoredDeferredEvent(this, foNode, bStart, foNode.getLocator());
        }
    }

    /**
     * Calls the event handlers for the passed FONode and all its elements.
     *
     * @param foNode FONode object which shall be recursed
     */
    private void recurseFONode(FONode foNode) {
        invokeDeferredEvent(foNode, true);

        if (foNode instanceof PageSequence) {
            PageSequence pageSequence = (PageSequence) foNode;

            Region regionBefore = pagemaster.getRegion(Constants.FO_REGION_BEFORE);
            if (regionBefore != null) {
                FONode staticBefore = (FONode) pageSequence.getFlowMap().get(
                        regionBefore.getRegionName());
                if (staticBefore != null) {
                    recurseFONode(staticBefore);
                }
            }
            Region regionAfter = pagemaster.getRegion(Constants.FO_REGION_AFTER);
            if (regionAfter != null) {
                FONode staticAfter = (FONode) pageSequence.getFlowMap().get(
                        regionAfter.getRegionName());
                if (staticAfter != null) {
                    recurseFONode(staticAfter);
                }
            }


            recurseFONode( pageSequence.getMainFlow() );
        } else if (foNode instanceof Table) {
            Table table = (Table) foNode;

            //recurse all table-columns
            if (table.getColumns() != null) {
                for (Iterator it = table.getColumns().iterator(); it.hasNext();) {
                    recurseFONode( (FONode) it.next() );
                }
            } else {
                //TODO Implement implicit column setup handling!
                RTFEventProducer eventProducer = RTFEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.explicitTableColumnsRequired(this, table.getLocator());
            }

            //recurse table-header
            if (table.getTableHeader() != null) {
                recurseFONode( table.getTableHeader() );
            }

            //recurse table-footer
            if (table.getTableFooter() != null) {
                recurseFONode( table.getTableFooter() );
            }

            if (foNode.getChildNodes() != null) {
                for (Iterator it = foNode.getChildNodes(); it.hasNext();) {
                    recurseFONode( (FONode) it.next() );
                }
            }
        } else if (foNode instanceof ListItem) {
            ListItem item = (ListItem) foNode;

            recurseFONode(item.getLabel());
            recurseFONode(item.getBody());
        } else if (foNode instanceof Footnote) {
            Footnote fn = (Footnote)foNode;

            recurseFONode(fn.getFootnoteCitation());
            recurseFONode(fn.getFootnoteBody());
        } else {
            //Any other FO-Object: Simply recurse through all childNodes.
            if (foNode.getChildNodes() != null) {
                for (Iterator it = foNode.getChildNodes(); it.hasNext();) {
                    FONode fn = (FONode)it.next();
                    if (log.isTraceEnabled()) {
                        log.trace("  ChildNode for " + fn + " (" + fn.getName() + ")");
                    }
                    recurseFONode(fn);
                }
            }
        }

        invokeDeferredEvent(foNode, false);
    }
}
