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
package org.apache.fop.datatypes;

import org.apache.fop.pdf.PDFGoTo;


public class IDNode {
    private String idValue, internalLinkGoToPageReference;

    private PDFGoTo internalLinkGoTo;

    private String pageNumber;
    private int xPosition = 0;    // x position on page
    private int yPosition = 0;    // y position on page


    /**
     * Constructor for IDNode
     *
     * @param idValue The value of the id for this node
     */
    protected IDNode(String idValue) {
        this.idValue = idValue;
    }


    /**
     * Sets the page number for this node
     *
     * @param number page number of node
     */
    protected void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }


    /**
     * Returns the page number of this node
     *
     * @return page number of this node
     */
    public String getPageNumber() {
        return pageNumber;
    }

    /**
     * Returns the page reference.
     *
     * @return page reference of this node.
     */
    public String getPageReference() {
       if (null != internalLinkGoTo) {
         return internalLinkGoTo.getPageReference();
       } else {
         return internalLinkGoToPageReference;
       }
    }

    public int getXPosition() {
       return xPosition;
    }
    
    public int getYPosition() {
       return yPosition;
    }

    /**
     * creates a new GoTo object for an internal link
     *
     * @param objectNumber
     * the number to be assigned to the new object
     */
    protected void createInternalLinkGoTo(int objectNumber) {
        if (internalLinkGoToPageReference == null) {
            internalLinkGoTo = new PDFGoTo(objectNumber, null);
        } else {
            internalLinkGoTo = new PDFGoTo(objectNumber,
                                           internalLinkGoToPageReference);
        }

        if (xPosition
                != 0)    // if the position is known (if x is known, then y is known)
         {
            internalLinkGoTo.setXPosition(xPosition);
            internalLinkGoTo.setYPosition(yPosition);
        }

    }



    /**
     * sets the page reference for the internal link's GoTo.  The GoTo will jump to this page reference.
     *
     * @param pageReference
     * the page reference to which the internal link GoTo should jump
     * ex. 23 0 R
     */
    protected void setInternalLinkGoToPageReference(String pageReference) {
        if (internalLinkGoTo != null) {
            internalLinkGoTo.setPageReference(pageReference);
        } else {
            internalLinkGoToPageReference = pageReference;
        }

    }



    /**
     * Returns the reference to the Internal Link's GoTo object
     *
     * @return GoTo object reference
     */
    protected String getInternalLinkGoToReference() {
        return internalLinkGoTo.referencePDF();
    }



    /**
     * Returns the id value of this node
     *
     * @return this node's id value
     */
    protected String getIDValue() {
        return idValue;
    }



    /**
     * Returns the PDFGoTo object associated with the internal link
     *
     * @return PDFGoTo object
     */
    protected PDFGoTo getInternalLinkGoTo() {
        return internalLinkGoTo;
    }


    /**
     * Determines whether there is an internal link GoTo for this node
     *
     * @return true if internal link GoTo for this node is set, false otherwise
     */
    protected boolean isThereInternalLinkGoTo() {
        return internalLinkGoTo != null;
    }


    /**
     * Sets the position of this node
     *
     * @param x      the x position
     * @param y      the y position
     */
    protected void setPosition(int x, int y) {
        if (internalLinkGoTo != null) {
            internalLinkGoTo.setXPosition(x);
            internalLinkGoTo.setYPosition(y);
        } else {
            xPosition = x;
            yPosition = y;
        }
    }

}
