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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Locator;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.FixedLength;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_external-graphic">
 * <code>fo:external-graphic</code></a> object.
 * This FO node handles the external graphic. It creates an image
 * inline area that can be added to the area tree.
 */
public class ExternalGraphic extends AbstractGraphics {

    // The value of properties relevant for fo:external-graphic.
    // All but one of the e-g properties are kept in AbstractGraphics
    private String src;
    // End of property values

    //Additional values
    private String url;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private Length intrinsicAlignmentAdjust;

    /**
     * Create a new ExternalGraphic node that is a child
     * of the given {@link FONode}.
     *
     * @param parent the parent of this node
     */
    public ExternalGraphic(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        src = pList.get(PR_SRC).getString();

        //Additional processing: obtain the image's intrinsic size and baseline information
        url = URISpecification.getURL(src);
        FOUserAgent userAgent = getUserAgent();
        ImageManager manager = userAgent.getImageManager();
        ImageInfo info = null;
        try {
            info = manager.getImageInfo(url, userAgent.getImageSessionContext());
        } catch (ImageException e) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, url, e, getLocator());
        } catch (FileNotFoundException fnfe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, url, fnfe, getLocator());
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, url, ioe, getLocator());
        }
        if (info != null) {
            this.intrinsicWidth = info.getSize().getWidthMpt();
            this.intrinsicHeight = info.getSize().getHeightMpt();
            int baseline = info.getSize().getBaselinePositionFromBottom();
            if (baseline != 0) {
                this.intrinsicAlignmentAdjust
                    = FixedLength.getInstance(-baseline);
            }
        }
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().image(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /** @return the "src" property */
    public String getSrc() {
        return src;
    }

    /** @return Get the resulting URL based on the src property */
    public String getURL() {
        return url;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "external-graphic";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_EXTERNAL_GRAPHIC}
     */
    public int getNameId() {
        return FO_EXTERNAL_GRAPHIC;
    }

    /** {@inheritDoc} */
    public int getIntrinsicWidth() {
        return this.intrinsicWidth;
    }

    /** {@inheritDoc} */
    public int getIntrinsicHeight() {
        return this.intrinsicHeight;
    }

    /** {@inheritDoc} */
    public Length getIntrinsicAlignmentAdjust() {
        return this.intrinsicAlignmentAdjust;
    }

}
