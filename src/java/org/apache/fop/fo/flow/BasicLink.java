/*
 * $Id: BasicLink.java,v 1.21 2003/03/06 11:36:31 jeremias Exp $
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
package org.apache.fop.fo.flow;

// Java
import java.util.List;
import java.io.Serializable;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.Trait;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Area;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.LayoutProcessor;

/**
 * The basic link.
 * This sets the basic link trait on the inline parent areas
 * that are created by the fo element.
 */
public class BasicLink extends Inline {

    private String link = null;
    private boolean external = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BasicLink(FONode parent) {
        super(parent);
    }

    /**
     * Add start and end properties for the link
     * @see org.apache.fop.fo.FObj#addLayoutManager
     */
    public void addLayoutManager(List lms) {
        setup();
        InlineStackingLayoutManager lm;
        lm = new InlineStackingLayoutManager() {
                    protected InlineParent createArea() {
                        InlineParent area = super.createArea();
                        setupLinkArea(parentLM, area);
                        return area;
                    }
                };
        lm.setUserAgent(getUserAgent());
        lm.setFObj(this);
        lm.setLMiter(new LMiter(children.listIterator()));
        lms.add(lm);
    }

    protected void setupLinkArea(LayoutProcessor parentLM, InlineParent area) {
        if (link == null) {
            return;
        }
        if (external) {
            area.addTrait(Trait.EXTERNAL_LINK, link);
        } else {
            PageViewport page = parentLM.resolveRefID(link);
            if (page != null) {
                area.addTrait(Trait.INTERNAL_LINK, page.getKey());
            } else {
                LinkResolver res = new LinkResolver(link, area);
                parentLM.addUnresolvedArea(link, res);
            }
        }
    }

    private void setup() {
        String destination;
        int linkType;

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("destination-place-offset");
        // this.properties.get("dominant-baseline");
        String ext =  properties.get("external-destination").getString();
        setupID();
        // this.properties.get("indicate-destination");
        String internal = properties.get("internal-destination").getString();
        if (ext.length() > 0) {
            link = ext;
            external = true;
        } else if (internal.length() > 0) {
            link = internal;
        } else {
            getLogger().error("basic-link requires an internal or external destination");
        }
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("show-destination");
        // this.properties.get("target-processing-context");
        // this.properties.get("target-presentation-context");
        // this.properties.get("target-stylesheet");

    }

    /**
     * @return true (BasicLink can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * Link resolving for resolving internal links.
     * This is static since it is independant of the link fo.
     */
    protected static class LinkResolver implements Resolveable, Serializable {
        private boolean resolved = false;
        private String idRef;
        private Area area;

        /**
         * Create a new link resolver.
         *
         * @param id the id to resolve
         * @param a the area that will have the link attribute
         */
        public LinkResolver(String id, Area a) {
            idRef = id;
            area = a;
        }

        /**
         * @return true if this link is resolved
         */
        public boolean isResolved() {
            return resolved;
        }

        public String[] getIDs() {
            return new String[] {idRef};
        }

        /**
         * Resolve by adding an internal link.
         */
        public void resolve(String id, List pages) {
            resolved = true;
            if (idRef.equals(id) && pages != null) {
                PageViewport page = (PageViewport)pages.get(0);
                area.addTrait(Trait.INTERNAL_LINK, page.getKey());
            }
        }
    }

}

