/*
 * $Id: BodyRegion.java,v 1.9 2003/03/05 15:19:31 jeremias Exp $
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
package org.apache.fop.area;

/**
 * The body region area.
 * This area contains a main reference area and optionally a
 * before float and footnote area.
 */
public class BodyRegion extends RegionReference {
    private BeforeFloat beforeFloat;
    private MainReference mainReference;
    private Footnote footnote;
    private int columnGap;
    private int columnCount;

    /** Reference inline progression dimension for the body. */
    private int refIPD;

    /**
     * Create a new body region area.
     * This sets the region reference area class to BODY.
     */
    public BodyRegion() {
        super(BODY);
    }

    /**
     * Set the number of columns for blocks when not spanning
     *
     * @param colCount the number of columns
     */
    public void setColumnCount(int colCount) {
        this.columnCount = colCount;
    }

    /**
     * Get the number of columns when not spanning
     *
     * @return the number of columns
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Set the column gap between columns
     * The length is in millipoints.
     *
     * @param colGap the column gap in millipoints
     */
    public void setColumnGap(int colGap) {
        this.columnGap = colGap;
    }

    /**
     * Set the before float area.
     *
     * @param bf the before float area
     */
    public void setBeforeFloat(BeforeFloat bf) {
        beforeFloat = bf;
    }

    /**
     * Set the main reference area.
     *
     * @param mr the main reference area
     */
    public void setMainReference(MainReference mr) {
        mainReference = mr;
    }

    /**
     * Set the footnote area.
     *
     * @param foot the footnote area
     */
    public void setFootnote(Footnote foot) {
        footnote = foot;
    }

    /**
     * Get the before float area.
     *
     * @return the before float area
     */
    public BeforeFloat getBeforeFloat() {
        return beforeFloat;
    }

    /**
     * Get the main reference area.
     *
     * @return the main reference area
     */
    public MainReference getMainReference() {
        return mainReference;
    }

    /**
     * Get the footnote area.
     *
     * @return the footnote area
     */
    public Footnote getFootnote() {
        return footnote;
    }

    /**
     * Clone this object.
     * This is only used to clone the current object, the child areas
     * are assumed to be null and are not cloned.
     *
     * @return a shallow copy of this object
     */ 
    public Object clone() {
        BodyRegion br = new BodyRegion();
        br.setCTM(getCTM());
        br.setIPD(getIPD());
        br.columnGap = columnGap;
        br.columnCount = columnCount;
        return br;
    }
}
