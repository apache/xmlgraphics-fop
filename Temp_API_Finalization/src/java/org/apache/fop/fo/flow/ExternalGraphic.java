/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.xml.sax.Locator;

/**
 * External graphic formatting object.
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
    private FopImage fopimage;
    
    /**
     * Create a new External graphic node.
     *
     * @param parent the parent of this node
     */
    public ExternalGraphic(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        src = pList.get(PR_SRC).getString();
        
        //Additional processing: preload image
        url = ImageFactory.getURL(getSrc());
        FOUserAgent userAgent = getUserAgent();
        ImageFactory fact = userAgent.getFactory().getImageFactory();
        fopimage = fact.getImage(url, userAgent);
        if (fopimage == null) {
            getLogger().error("Image not available: " + getSrc());
        } else {
            // load dimensions
            if (!fopimage.load(FopImage.DIMENSIONS)) {
                getLogger().error("Cannot read image dimensions: " + getSrc());
            }
        }
        //TODO Report to caller so he can decide to throw an exception
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(getId());
        getFOEventHandler().image(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @return the "src" property.
     */
    public String getSrc() {
        return src;
    }

    /**
     * @return Get the resulting URL based on the src property.
     */
    public String getURL() {
        return url;
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "external-graphic";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_EXTERNAL_GRAPHIC;
    }

    /**
     * @see org.apache.fop.fo.flow.AbstractGraphics#getIntrinsicWidth()
     */
    public int getIntrinsicWidth() {
        if (fopimage != null) {
            return fopimage.getIntrinsicWidth();
        } else {
            return 0;
        }
    }

    /**
     * @see org.apache.fop.fo.flow.AbstractGraphics#getIntrinsicHeight()
     */
    public int getIntrinsicHeight() {
        if (fopimage != null) {
            return fopimage.getIntrinsicHeight();
        } else {
            return 0;
        }
    }
    
}
