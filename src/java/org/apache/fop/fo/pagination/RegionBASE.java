/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.fo.pagination;

// XML
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;


/**
 * Base class for Before, After, Start and End regions (BASE).
 */
public abstract class RegionBASE extends Region {

    private int extent;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected RegionBASE(FONode parent, int regionId) {
        super(parent, regionId);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode()
     */
    protected void endOfNode() throws SAXParseException {
        // The problem with this is that it might not be known yet....
        // Supposing extent is calculated in terms of percentage
        this.extent = this.propertyList.get(PR_EXTENT).getLength().getValue();
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getExtent()
     */
    public int getExtent() {
        return this.extent;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRegionBASE(this);
    }

}

