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

package org.apache.fop.fo.flow;

import java.awt.geom.Point2D;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.XMLObj;
import org.xml.sax.Locator;

/**
 * The instream-foreign-object flow formatting object.
 * This is an atomic inline object that contains
 * xml data.
 */
public class InstreamForeignObject extends AbstractGraphics {
    
    // The value of properties relevant for fo:instream-foreign-object.
    // All property values contained in AbstractGraphics
    // End of property values

    //Additional value
    private Point2D intrinsicDimensions;
    
    private Length intrinsicAlignmentAdjust;
    
    /**
     * constructs an instream-foreign-object object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("one (1) non-XSL namespace child");
        }
        getFOEventHandler().foreignObject(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: one (1) non-XSL namespace child
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        } else if (firstChild != null) {
            tooManyNodesError(loc, "child element");
        }
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "instream-foreign-object";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_INSTREAM_FOREIGN_OBJECT;
    }

    /**
     * Preloads the image so the intrinsic size is available.
     */
    private void prepareIntrinsicSize() {
        if (intrinsicDimensions == null) {
            XMLObj child = (XMLObj) firstChild;
            Point2D csize = new Point2D.Float(-1, -1);
            intrinsicDimensions = child.getDimension(csize);
            if (intrinsicDimensions == null) {
                log.error("Intrinsic dimensions of "
                        + " instream-foreign-object could not be determined");
            }
            intrinsicAlignmentAdjust = child.getIntrinsicAlignmentAdjust();
        }
    }

    /**
     * @see org.apache.fop.fo.flow.AbstractGraphics#getIntrinsicWidth()
     */
    public int getIntrinsicWidth() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getX() * 1000);
        } else {
            return 0;
        }
    }

    /**
     * @see org.apache.fop.fo.flow.AbstractGraphics#getIntrinsicHeight()
     */
    public int getIntrinsicHeight() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getY() * 1000);
        } else {
            return 0;
        }
    }

    /**
     * @see org.apache.fop.fo.flow.AbstractGraphics#getIntrinsicAlignmentAdjust()
     */
    public  Length getIntrinsicAlignmentAdjust()
    {
        prepareIntrinsicSize();
        return intrinsicAlignmentAdjust;
    }
    
    /** @see org.apache.fop.fo.FONode#addChildNode(org.apache.fop.fo.FONode) */
    protected void addChildNode(FONode child) throws FOPException {
        super.addChildNode(child);
    }

    /** @return the XMLObj child node of the instream-foreign-object. */
    public XMLObj getChildXMLObj() {
        return (XMLObj) firstChild;
    }
    
}
