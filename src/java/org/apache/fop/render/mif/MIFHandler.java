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
package org.apache.fop.render.mif;

// Java
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.apps.Document;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fonts.FontSetup;
import org.xml.sax.SAXException;

// TODO: do we really want every method throwing a SAXException

/**
 * The MIF Handler.
 * This generates MIF output using the structure events from
 * the FO Tree sent to this structure handler.
 * This builds an MIF file and writes it to the output.
 */
public class MIFHandler extends FOInputHandler {

    /** the MIFFile instance */
    protected MIFFile mifFile;
    /** the OutputStream to write to */
    protected OutputStream outStream;

    // current state elements
    private MIFElement textFlow;
    private MIFElement para;

    /**
     * Creates a new MIF handler on a given OutputStream.
     * @param os OutputStream to write to
     */
    public MIFHandler(Document doc, OutputStream os) {
        super(doc);
        outStream = os;
        FontSetup.setup(doc, null);
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startDocument()
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
     * @see org.apache.fop.fo.FOInputHandler#endDocument()
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
     * @see org.apache.fop.fo.FOInputHandler
     */
    public void startPageSequence(PageSequence pageSeq) {
        // get the layout master set
        // setup the pages for this sequence
        String name = pageSeq.getProperty("master-reference").getString();
        SimplePageMaster spm = pageSeq.getLayoutMasterSet().getSimplePageMaster(name);
        if (spm == null) {
            PageSequenceMaster psm = pageSeq.getLayoutMasterSet().getPageSequenceMaster(name);
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
     * @see org.apache.fop.fo.FOInputHandler#endPageSequence(PageSequence)
     */
    public void endPageSequence(PageSequence pageSeq) throws FOPException {

    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startFlow(Flow)
     */
    public void startFlow(Flow fl) {
        // start text flow in body region
        textFlow = new MIFElement("TextFlow");
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endFlow(Flow)
     */
    public void endFlow(Flow fl) {
        textFlow.finish(true);
        mifFile.addElement(textFlow);
        textFlow = null;
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#startBlock(Block)
     */
    public void startBlock(Block bl) {
        para = new MIFElement("Para");
        // get font
        textFlow.addElement(para);
    }

    /**
     * @see org.apache.fop.fo.FOInputHandler#endBlock(Block)
     */
    public void endBlock(Block bl) {
        para.finish(true);
        para = null;
    }

    /**
     *
     * @param inl Inline that is starting.
     */
    public void startInline(Inline inl){
    }

    /**
     *
     * @param inl Inline that is ending.
     */
    public void endInline(Inline inl){
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
     *
     * @param tc TableColumn that is starting;
     */
    public void startColumn(TableColumn tc) {
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
     * @see org.apache.fop.fo.FOInputHandler#startLink(BasicLink basicLink)
     */
    public void startLink(BasicLink basicLink) {
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

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public void startPageNumber(PageNumber pagenum) {
    }

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public void endPageNumber(PageNumber pagenum) {
    }
}

