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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.ValidationException;

/**
 * Class modelling the <a href=http://www.w3.org/TR/xsl/#fo_wrapper">
 * <code>fo:wrapper</code></a> object.
 * The <code>fo:wrapper</code> object serves as a property holder for
 * its child node objects.
 */
public class Wrapper extends FObjMixed {
    // The value of properties relevant for fo:wrapper.
    // End of property values

    // used for FO validation
    private boolean blockOrInlineItemFound = false;

    /**
     * Create a Wrapper instance that is a child of the
     * given {@link FONode}
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public Wrapper(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (#PCDATA|%inline;|%block;)*
     * <br><i>Additionally (unimplemented): "An fo:wrapper that is a child of an
     * fo:multi-properties is only permitted to have children that would
     * be permitted in place of the fo:multi-properties."</i>
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if ("marker".equals(localName)) {
                if (blockOrInlineItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker",
                        "(#PCDATA|%inline;|%block;)");
                }
            } else if (isBlockOrInlineItem(nsURI, localName)) {
                /* delegate validation to parent, but keep the error reporting
                 * tidy. If we would simply call validateChildNode() on the
                 * parent, the user would get a wrong impression, as only the
                 * locator (if any) will contain a reference to the offending
                 * fo:wrapper.
                 */
                try {
                    FONode.validateChildNode(this.parent, loc, nsURI, localName);
                } catch (ValidationException vex) {
                    invalidChildError(loc, getName(), FO_URI, localName,
                                      "rule.wrapperInvalidChildForParent");
                }
                blockOrInlineItemFound = true;
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode child) throws FOPException {
        super.addChildNode(child);
        /* If the child is a text node, and it generates areas
         * (i.e. contains either non-white-space or preserved
         * white-space), then check whether the nearest non-wrapper
         * ancestor allows this.
         */
        if (child instanceof FOText
                && ((FOText)child).willCreateArea()) {
            FONode ancestor = parent;
            while (ancestor.getNameId() == Constants.FO_WRAPPER) {
                ancestor = ancestor.getParent();
            }
            if (!(ancestor instanceof FObjMixed)) {
                invalidChildError(
                        getLocator(),
                        getLocalName(),
                        FONode.FO_URI,
                        "#PCDATA",
                        "rule.wrapperInvalidChildForParent");
            }
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "wrapper";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_WRAPPER}
     */
    public int getNameId() {
        return FO_WRAPPER;
    }
}

