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
package org.apache.fop.rtf.renderer;

import java.util.ArrayList;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.rtf.rtflib.interfaces.ITableColumnsInfo;


/** Used when handling fo:table to hold information to build the table.
 *
 *  Contributor(s):
 *  @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 *  @author Trembicki-Guy, Ed <GuyE@DNB.com>
 *  @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 *  @author Peter Herweg <pherweg@web.de>
 *
 *  This class was originally developed for the JFOR project and
 *  is now integrated into FOP.
 */

class TableContext implements ITableColumnsInfo {
    private final Logger log = new ConsoleLogger();
    private final BuilderContext m_context;
    private final ArrayList m_colWidths = new ArrayList();
    private int m_colIndex;

    /**
     * Added by Peter Herweg on 2002-06-29
     * This ArrayList contains one element for each column in the table.
     * value == 0 means there is no row-spanning
     * value >  0 means there is row-spanning
     * Each value in the list is decreased by 1 after each finished table-row
     */
    private final ArrayList m_colRowSpanningNumber = new ArrayList();

    /**
     * Added by Peter Herweg on 2002-06-29
     * If there has a vertical merged cell to be created, its attributes are
     * inherited from the corresponding MERGE_START-cell.
     * For this purpose the attributes of a cell are stored in this array, as soon
     * as a number-rows-spanned attribute has been found.
     */
    private final ArrayList m_colRowSpanningAttrs = new ArrayList();

    private boolean m_bNextRowBelongsToHeader = false;

    public void setNextRowBelongsToHeader(boolean bNextRowBelongsToHeader) {
        m_bNextRowBelongsToHeader = bNextRowBelongsToHeader;
    }

    public boolean getNextRowBelongsToHeader() {
        return m_bNextRowBelongsToHeader;
    }

    TableContext(BuilderContext ctx) {
        m_context = ctx;
    }

    void setNextColumnWidth(String strWidth)
            throws Exception {
        m_colWidths.add(new Float(FoUnitsConverter.getInstance().convertToTwips(strWidth)));
    }

    //Added by Peter Herweg on 2002-06-29
    RtfAttributes getColumnRowSpanningAttrs() {
        return (RtfAttributes)m_colRowSpanningAttrs.get(m_colIndex);
    }

    //Added by Peter Herweg on 2002-06-29
    Integer getColumnRowSpanningNumber() {
        return (Integer)m_colRowSpanningNumber.get(m_colIndex);
    }

    //Added by Peter Herweg on 2002-06-29
    void setCurrentColumnRowSpanning(Integer iRowSpanning,  RtfAttributes attrs)
            throws Exception {

        if (m_colIndex < m_colRowSpanningNumber.size()) {
            m_colRowSpanningNumber.set(m_colIndex, iRowSpanning);
            m_colRowSpanningAttrs.set(m_colIndex, attrs);
        } else {
            m_colRowSpanningNumber.add(iRowSpanning);
            m_colRowSpanningAttrs.add(m_colIndex, attrs);
        }
    }

    //Added by Peter Herweg on 2002-06-29
    public void setNextColumnRowSpanning(Integer iRowSpanning,
            RtfAttributes attrs) {
        m_colRowSpanningNumber.add(iRowSpanning);
        m_colRowSpanningAttrs.add(m_colIndex, attrs);
    }

    /**
     * Added by Peter Herweg on 2002-06-29
     * This function is called after each finished table-row.
     * It decreases all values in m_colRowSpanningNumber by 1. If a value
     * reaches 0 row-spanning is finished, and the value won't be decreased anymore.
     */
    public void decreaseRowSpannings() {
        for (int z = 0; z < m_colRowSpanningNumber.size(); ++z) {
            Integer i = (Integer)m_colRowSpanningNumber.get(z);

            if (i.intValue() > 0) {
                i = new Integer(i.intValue() - 1);
            }

            m_colRowSpanningNumber.set(z, i);

            if (i.intValue() == 0) {
                m_colRowSpanningAttrs.set(z, null);
            }
        }
    }

    /**
     * Reset the column iteration index, meant to be called when creating a new row
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public void selectFirstColumn() {
        m_colIndex = 0;
    }

    /**
     * Increment the column iteration index
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public void selectNextColumn() {
        m_colIndex++;
    }

    /**
     * Get current column width according to column iteration index
     * @return INVALID_COLUMN_WIDTH if we cannot find the value
     * The 'public' modifier has been added by Boris Poudérous for
     * 'number-columns-spanned' processing
     */
    public float getColumnWidth() {
        try {
            return ((Float)m_colWidths.get(m_colIndex)).floatValue();
        } catch (IndexOutOfBoundsException ex) {
            // this code contributed by Trembicki-Guy, Ed <GuyE@DNB.com>
            log.warn("fo:table-column width not defined, using " + INVALID_COLUM_WIDTH);
            return INVALID_COLUM_WIDTH;
        }
    }

     /** Added by Boris Poudérous on 07/22/2002 */
     public int getColumnIndex() {
       return m_colIndex;
     }
     /** - end - */

     /** Added by Boris Poudérous on 07/22/2002 */
     public int getNumberOfColumns() {
       return m_colWidths.size();
     }
     /** - end - */
}

