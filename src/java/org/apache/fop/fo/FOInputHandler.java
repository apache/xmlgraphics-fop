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
package org.apache.fop.fo;

// Java
import java.util.Set;
import java.util.HashSet;

// Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;

// FOP
import org.apache.fop.apps.FOPException;
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

import org.xml.sax.SAXException;

/**
 * Abstract class defining what should be done with SAX events that map to
 * XSL-FO input. The events are actually captured by fo/FOTreeBuilder, passed
 * to the various fo Objects, which in turn, if needed, pass them to an instance
 * of FOInputHandler.
 *
 * Sub-classes will generally fall into one of two categories:
 * 1) a handler that actually builds an FO Tree from the events, or 2) a
 * handler that builds a structured (as opposed to formatted) document, such
 * as our MIF and RTF output targets.
 */
public abstract class FOInputHandler extends AbstractLogEnabled {
    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    /**
     * The FOTreeControl object that is controlling the FO Tree being built
     */
    public FOTreeControl foTreeControl = null;

    /**
     * Main constructor
     * @param foTreeControl the FOTreeControl implementation that is controlling
     * the FO Tree being built
     */
    public FOInputHandler(FOTreeControl foTreeControl) {
        this.foTreeControl = foTreeControl;
    }

    /**
     * Retuns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * Returns the FontInfo object associated with this FOInputHandler.
     * @return the FontInof object
     */
    public FOTreeControl getFontInfo() {
        return null;
    }

    /**
     * This method is called to indicate the start of a new document run.
     * @throws SAXException In case of a problem
     */
    public abstract void startDocument() throws SAXException;

    /**
     * This method is called to indicate the end of a document run.
     * @throws SAXException In case of a problem
     */
    public abstract void endDocument() throws SAXException;

    /**
     *
     * @param pageSeq PageSequence that is starting.
     */
    public abstract void startPageSequence(PageSequence pageSeq);

    /**
     *
     * @param pageSeq PageSequence that is ending.
     * @throws FOPException For errors encountered.
     */
    public abstract void endPageSequence(PageSequence pageSeq) throws FOPException;

    /**
     *
     * @param fl Flow that is starting.
     */
    public abstract void startFlow(Flow fl);

    /**
     *
     * @param fl Flow that is ending.
     */
    public abstract void endFlow(Flow fl);

    /**
     *
     * @param bl Block that is starting.
     */
    public abstract void startBlock(Block bl);

    /**
     *
     * @param bl Block that is ending.
     */
    public abstract void endBlock(Block bl);


    // Tables
    /**
     *
     * @param tbl Table that is starting.
     */
    public abstract void startTable(Table tbl);

    /**
     *
     * @param tbl Table that is ending.
     */
    public abstract void endTable(Table tbl);

    /**
     *
     * @param th TableBody that is starting;
     */
    public abstract void startHeader(TableBody th);

    /**
     *
     * @param th TableBody that is ending.
     */
    public abstract void endHeader(TableBody th);

    /**
     *
     * @param tf TableFooter that is starting.
     */
    public abstract void startFooter(TableBody tf);

    /**
     *
     * @param tf TableFooter that is ending.
     */
    public abstract void endFooter(TableBody tf);

    /**
     *
     * @param tb TableBody that is starting.
     */
    public abstract void startBody(TableBody tb);

    /**
     *
     * @param tb TableBody that is ending.
     */
    public abstract void endBody(TableBody tb);

    /**
     *
     * @param tr TableRow that is starting.
     */
    public abstract void startRow(TableRow tr);

    /**
     *
     * @param tr TableRow that is ending.
     */
    public abstract void endRow(TableRow tr);

    /**
     *
     * @param tc TableCell that is starting.
     */
    public abstract void startCell(TableCell tc);

    /**
     *
     * @param tc TableCell that is ending.
     */
    public abstract void endCell(TableCell tc);


    // Lists
    /**
     *
     * @param lb ListBlock that is starting.
     */
    public abstract void startList(ListBlock lb);

    /**
     *
     * @param lb ListBlock that is ending.
     */
    public abstract void endList(ListBlock lb);

    /**
     *
     * @param li ListItem that is starting.
     */
    public abstract void startListItem(ListItem li);

    /**
     *
     * @param li ListItem that is ending.
     */
    public abstract void endListItem(ListItem li);

    /**
     * Process start of a ListLabel.
     */
    public abstract void startListLabel();

    /**
     * Process end of a ListLabel.
     */
    public abstract void endListLabel();

    /**
     * Process start of a ListBody.
     */
    public abstract void startListBody();

    /**
     * Process end of a ListBody.
     */
    public abstract void endListBody();

    // Static Regions
    /**
     * Process start of a Static.
     */
    public abstract void startStatic();

    /**
     * Process end of a Static.
     */
    public abstract void endStatic();

    /**
     * Process start of a Markup.
     */
    public abstract void startMarkup();

    /**
     * Process end of a Markup.
     */
    public abstract void endMarkup();

    /**
     * Process start of a Link.
     */
    public abstract void startLink();

    /**
     * Process end of a Link.
     */
    public abstract void endLink();

    /**
     * Process an ExternalGraphic.
     * @param eg ExternalGraphic to process.
     */
    public abstract void image(ExternalGraphic eg);

    /**
     * Process a pageRef.
     */
    public abstract void pageRef();

    /**
     * Process an InstreamForeignObject.
     * @param ifo InstreamForeignObject to process.
     */
    public abstract void foreignObject(InstreamForeignObject ifo);

    /**
     * Process a footnote.
     */
    public abstract void footnote();

    /**
     * Process a Leader.
     * @param l Leader to process.
     */
    public abstract void leader(Leader l);

    /**
     * Process character data.
     * @param data Array of characters to process.
     * @param start Offset for characters to process.
     * @param length Portion of array to process.
     */
    public abstract void characters(char data[], int start, int length);

}

