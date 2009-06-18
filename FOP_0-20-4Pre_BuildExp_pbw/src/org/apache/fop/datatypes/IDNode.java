/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.pdf.PDFGoTo;


public class IDNode {
    private String idValue, internalLinkGoToPageReference;

    private PDFGoTo internalLinkGoTo;

    private int pageNumber = -1, xPosition = 0,    // x position on page
    yPosition = 0;                                 // y position on page


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
    protected void setPageNumber(int number) {
        pageNumber = number;
    }


    /**
     * Returns the page number of this node
     *
     * @return page number of this node
     */
    public String getPageNumber() {
        return (pageNumber != -1) ? new Integer(pageNumber).toString() : null;
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
