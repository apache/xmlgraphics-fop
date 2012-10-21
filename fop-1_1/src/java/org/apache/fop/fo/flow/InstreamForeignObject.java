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

import org.xml.sax.Locator;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.XMLObj;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_instream-foreign-object">
 * <code>fo:instream-foreign-object</code></a> object.
 * This is an atomic inline object that contains XML data.
 */
public class InstreamForeignObject extends AbstractGraphics {

    // The value of properties relevant for fo:instream-foreign-object.
    // All property values contained in AbstractGraphics
    // End of property values

    //Additional value
    private Point2D intrinsicDimensions;
    private boolean instrisicSizeDetermined;

    private Length intrinsicAlignmentAdjust;

    /**
     * Constructs an instream-foreign-object object
     * (called by {@link org.apache.fop.fo.ElementMapping.Maker}).
     *
     * @param parent the parent {@link FONode}
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    @Override
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startInstreamForeignObject(this);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * {@link org.apache.fop.fo.FOEventHandler} that we are at
     * the end of the instream-foreign-object.
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("one (1) non-XSL namespace child");
        }
        getFOEventHandler().endInstreamForeignObject(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: one (1) non-XSL namespace child
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        } else if (firstChild != null) {
            tooManyNodesError(loc, new QName(nsURI, null, localName));
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "instream-foreign-object";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_INSTREAM_FOREIGN_OBJECT}
     */
    public int getNameId() {
        return FO_INSTREAM_FOREIGN_OBJECT;
    }

    /** Preloads the image so the intrinsic size is available. */
    private void prepareIntrinsicSize() {
        if (!this.instrisicSizeDetermined) {
            XMLObj child = (XMLObj) firstChild;
            Point2D csize = new Point2D.Float(-1, -1);
            intrinsicDimensions = child.getDimension(csize);
            if (intrinsicDimensions == null) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.ifoNoIntrinsicSize(this, getLocator());
            }
            intrinsicAlignmentAdjust = child.getIntrinsicAlignmentAdjust();
            this.instrisicSizeDetermined = true;
        }
    }

    /** {@inheritDoc} */
    public int getIntrinsicWidth() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getX() * 1000);
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    public int getIntrinsicHeight() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getY() * 1000);
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    public  Length getIntrinsicAlignmentAdjust() {
        prepareIntrinsicSize();
        return intrinsicAlignmentAdjust;
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode child) throws FOPException {
        super.addChildNode(child);
    }

    /** @return the {@link XMLObj} child node of the instream-foreign-object. */
    public XMLObj getChildXMLObj() {
        return (XMLObj) firstChild;
    }

}
