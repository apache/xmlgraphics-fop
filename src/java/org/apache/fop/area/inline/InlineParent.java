/*
 * $Id: InlineParent.java,v 1.9 2003/03/05 16:45:42 jeremias Exp $
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
package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import java.util.List;
import java.util.ArrayList;

/**
 * Inline parent area.
 * This is an inline area that can have other inlines as children.
 */
public class InlineParent extends InlineArea {
    /**
     * The list of inline areas added to this inline parent.
     */
    protected List inlines = new ArrayList();

    /**
     * An inline parent is a reference area somay have clipping
     */
    protected boolean clip = false;

    /**
     * Create a new inline parent to add areas to.
     */
    public InlineParent() {
    }

    /**
     * Handle InlineAreaVisitor request by passing this back to it.
     *
     * @param visitor the InlineAreaVisitor wishing to process this.
     * @see org.apache.fop.area.inline.InlineAreaVisitor
     */
    public void acceptVisitor(InlineAreaVisitor visitor) {
        visitor.serveVisitor(this);
    }

    /**
     * Override generic Area method.
     *
     * @param childArea the child area to add
     */
    public void addChild(Area childArea) {
        if (childArea instanceof InlineArea) {
            inlines.add(childArea);
            increaseIPD(((InlineArea) childArea).getAllocIPD());
        }
    }

    /**
     * Get the child areas for this inline parent.
     *
     * @return the list of child areas
     */
    public List getChildAreas() {
        return inlines;
    }

}

