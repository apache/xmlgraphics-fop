/*
 * $Id: StructureHandler.java,v 1.7 2003/02/27 10:13:07 jeremias Exp $
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
package org.apache.fop.apps;

// Java
import java.util.Set;
import java.util.HashSet;

// Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;

// FOP
import org.apache.fop.fo.Title;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Flow;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.FontInfo;

import org.xml.sax.SAXException;

/**
 * This class receives structure events from the FO Tree.
 * Sub-classes can then implement various methods to handle
 * the FO Tree when the SAX events occur.
 */
abstract public class StructureHandler extends AbstractLogEnabled {
    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    /**
     * Main constructor
     */
    public StructureHandler() {
    }

    /**
     * Retuns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * Returns the FontInfo object associated with this StructureHandler.
     * @return the FontInof object
     */
    public FontInfo getFontInfo() {
        return null;
    }

    /**
     * This method is called to indicate the start of a new document run.
     * @throws SAXException In case of a problem
     */
    abstract public void startDocument() throws SAXException;

    /**
     * This method is called to indicate the end of a document run.
     * @throws SAXException In case of a problem
     */
    abstract public void endDocument() throws SAXException;

    abstract public void startPageSequence(PageSequence pageSeq, Title seqTitle, LayoutMasterSet lms);

    abstract public void endPageSequence(PageSequence pageSeq) throws FOPException;

    abstract public void startFlow(Flow fl);

    abstract public void endFlow(Flow fl);

    abstract public void startBlock(Block bl);

    abstract public void endBlock(Block bl);


    // Tables
    abstract public void startTable(Table tbl);

    abstract public void endTable(Table tbl);

    abstract public void startHeader(TableBody th);

    abstract public void endHeader(TableBody th);

    abstract public void startFooter(TableBody tf);

    abstract public void endFooter(TableBody tf);

    abstract public void startBody(TableBody tb);

    abstract public void endBody(TableBody tb);

    abstract public void startRow(TableRow tr);

    abstract public void endRow(TableRow tr);

    abstract public void startCell(TableCell tc);

    abstract public void endCell(TableCell tc);


    // Lists
    abstract public void startList(ListBlock lb);

    abstract public void endList(ListBlock lb);

    abstract public void startListItem(ListItem li);

    abstract public void endListItem(ListItem li);

    abstract public void startListLabel();

    abstract public void endListLabel();

    abstract public void startListBody();

    abstract public void endListBody();

    // Static Regions
    abstract public void startStatic();

    abstract public void endStatic();

    abstract public void startMarkup();

    abstract public void endMarkup();


    abstract public void startLink();

    abstract public void endLink();


    abstract public void image(ExternalGraphic eg);

    abstract public void pageRef();

    abstract public void foreignObject(InstreamForeignObject ifo);

    abstract public void footnote();

    abstract public void leader(Leader l);

    abstract public void characters(char data[], int start, int length);

    public void pageBreak() {

    }

}

