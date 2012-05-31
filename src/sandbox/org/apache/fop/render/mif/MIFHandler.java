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

package org.apache.fop.render.mif;

import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fonts.FontSetup;

// TODO: do we really want every method throwing a SAXException

/**
 * The MIF Handler.
 * This generates MIF output using the structure events from
 * the FO Tree sent to this structure handler.
 * This builds an MIF file and writes it to the output.
 */
public class MIFHandler extends FOEventHandler {

    /** Logger */
    private static Log log = LogFactory.getLog(MIFHandler.class);

    /** the MIFFile instance */
    protected MIFFile mifFile;

    /** the OutputStream to write to */
    protected OutputStream outStream;

    // current state elements
    private MIFElement textFlow;
    private MIFElement para;

    /**
     * Creates a new MIF handler on a given OutputStream.
     * @param ua FOUserAgent instance for this process
     * @param os OutputStream to write to
     */
    public MIFHandler(FOUserAgent ua, OutputStream os) {
        super(ua);
        outStream = os;
        boolean base14Kerning = false; //TODO - FIXME
        FontSetup.setup(fontInfo, null, ua.getNewURIResolver(), base14Kerning);
    }

    /** {@inheritDoc} */
    public void startDocument() throws SAXException {
        log.fatal("The MIF Handler is non-functional at this time. Please help resurrect it!");
        mifFile = new MIFFile();
        try {
            mifFile.output(outStream);
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public void startPageSequence(PageSequence pageSeq) {
        // get the layout master set
        // setup the pages for this sequence
        String name = pageSeq.getMasterReference();
        SimplePageMaster spm = pageSeq.getRoot().getLayoutMasterSet().getSimplePageMaster(name);
        if (spm == null) {
            PageSequenceMaster psm
                = pageSeq.getRoot().getLayoutMasterSet().getPageSequenceMaster(name);
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

    /** {@inheritDoc} */
    public void endPageSequence(PageSequence pageSeq) {
    }

    /** {@inheritDoc} */
    public void startFlow(Flow fl) {
        // start text flow in body region
        textFlow = new MIFElement("TextFlow");
    }

    /** {@inheritDoc} */
    public void endFlow(Flow fl) {
        textFlow.finish(true);
        mifFile.addElement(textFlow);
        textFlow = null;
    }

    /** {@inheritDoc} */
    public void startBlock(Block bl) {
        para = new MIFElement("Para");
        // get font
        textFlow.addElement(para);
    }

    /** {@inheritDoc} */
    public void endBlock(Block bl) {
        para.finish(true);
        para = null;
    }

    /** {@inheritDoc} */
    public void startInline(Inline inl) {
    }

    /** {@inheritDoc} */
    public void endInline(Inline inl) {
    }

    /** {@inheritDoc} */
    public void startTable(Table tbl) {
    }

    /** {@inheritDoc} */
    public void endTable(Table tbl) {
    }

    /** {@inheritDoc} */
    public void startColumn(TableColumn tc) {
    }

    /** {@inheritDoc} */
    public void endColumn(TableColumn tc) {
    }

    /** {@inheritDoc} */
    public void startHeader(TableHeader th) {
    }

    /** {@inheritDoc} */
    public void endHeader(TableHeader th) {
    }

    /** {@inheritDoc} */
    public void startFooter(TableFooter tf) {
    }

    /** {@inheritDoc} */
    public void endFooter(TableFooter tf) {
    }

    /** {@inheritDoc} */
    public void startBody(TableBody tb) {
    }

    /** {@inheritDoc} */
    public void endBody(TableBody tb) {
    }

    /** {@inheritDoc} */
    public void startRow(TableRow tr) {
    }

    /** {@inheritDoc} */
    public void endRow(TableRow tr) {
    }

    /** {@inheritDoc} */
    public void startCell(TableCell tc) {
    }

    /** {@inheritDoc} */
    public void endCell(TableCell tc) {
    }

    /** {@inheritDoc} */
    public void startList(ListBlock lb) {
    }

    /** {@inheritDoc} */
    public void endList(ListBlock lb) {
    }

    /** {@inheritDoc} */
    public void startListItem(ListItem li) {
    }

    /** {@inheritDoc} */
    public void endListItem(ListItem li) {
    }

    /** {@inheritDoc} */
    public void startListLabel() {
    }

    /** {@inheritDoc} */
    public void endListLabel() {
    }

    /** {@inheritDoc} */
    public void startListBody() {
    }

    /** {@inheritDoc} */
    public void endListBody() {
    }

    /** {@inheritDoc} */
    public void startStatic() {
    }

    /** {@inheritDoc} */
    public void endStatic() {
    }

    /** {@inheritDoc} */
    public void startMarkup() {
    }

    /** {@inheritDoc} */
    public void endMarkup() {
    }

    /** {@inheritDoc} */
    public void startLink(BasicLink basicLink) {
    }

    /** {@inheritDoc} */
    public void endLink() {
    }

    /** {@inheritDoc} */
    public void image(ExternalGraphic eg) {
    }

    /** {@inheritDoc} */
    public void pageRef() {
    }

    /** {@inheritDoc} */
    public void foreignObject(InstreamForeignObject ifo) {
    }

    /** {@inheritDoc} */
    public void startFootnote(Footnote footnote) {
    }

    /** {@inheritDoc} */
    public void endFootnote(Footnote footnote) {
    }

    /** {@inheritDoc} */
    public void startFootnoteBody(FootnoteBody body) {
    }

    /** {@inheritDoc} */
    public void endFootnoteBody(FootnoteBody body) {
    }

    /** {@inheritDoc} */
    public void leader(Leader l) {
    }

    /** {@inheritDoc} */
    public void characters(char[] data, int start, int length) {
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

    /** {@inheritDoc} */
    public void startPageNumber(PageNumber pagenum) {
    }

    /** {@inheritDoc} */
    public void endPageNumber(PageNumber pagenum) {
    }
}

