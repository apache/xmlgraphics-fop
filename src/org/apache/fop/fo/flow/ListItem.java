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
package org.apache.fop.fo.flow;

// Java
import java.util.List;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;

public class ListItem extends FObj {
    
    private ListItemLabel label = null;
    private ListItemBody body = null;

    private int align;
    private int alignLast;
    private int breakBefore;
    private int breakAfter;
    private int lineHeight;
    private int startIndent;
    private int endIndent;
    private int spaceBefore;
    private int spaceAfter;

    public ListItem(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        if (label != null && body != null) {
            ListItemLayoutManager blm = new ListItemLayoutManager();
            blm.setUserAgent(getUserAgent());
            blm.setFObj(this);
            blm.setLabel(label.getItemLayoutManager());
            blm.setBody(body.getItemLayoutManager());
            list.add(blm);
        } else {
            getLogger().error("list-item requires list-item-label and list-item-body");
        }
    }

    public void setup() {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("break-after");
        // this.properties.get("break-before");
        setupID();
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("relative-align");

        this.align = this.properties.get("text-align").getEnum();
        this.alignLast = this.properties.get("text-align-last").getEnum();
        this.lineHeight =
            this.properties.get("line-height").getLength().getValue();
        this.spaceBefore =
            this.properties.get("space-before.optimum").getLength().getValue();
        this.spaceAfter =
            this.properties.get("space-after.optimum").getLength().getValue();

    }

    public void addChild(FONode child) {
        if ("fo:list-item-label".equals(child.getName())) {
            label = (ListItemLabel)child;
        } else if ("fo:list-item-body".equals(child.getName())) {
            body = (ListItemBody)child;
        } else if ("fo:marker".equals(child.getName())) {
            // marker
        } else {
            // error
        }
    }

    public boolean generatesInlineAreas() {
        return false;
    }

    protected boolean containsMarkers() {
        return true;
    }

}

