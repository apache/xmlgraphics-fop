/*
 * $Id: RegionBA.java,v 1.6 2003/03/06 13:42:42 jeremias Exp $
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
package org.apache.fop.fo.pagination;

// Java
import java.awt.Rectangle;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Precedence;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * Abstract base class for fo:region-before and fo:region-after.
 */
public abstract class RegionBA extends RegionBASE {

    private boolean bPrecedence;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected RegionBA(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getPrecedence()
     */
    public boolean getPrecedence() {
        return bPrecedence;
    }

    /**
     * @see org.apache.fop.fo.FONode#end()
     */
    public void end() {
        super.end();
        bPrecedence =
            (this.properties.get("precedence").getEnum() == Precedence.TRUE);
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If precedence is false on a before or after region, its
     * inline-progression-dimension is limited by the extent of the start
     * and end regions if they are present.
     * @param vpRect viewport rectangle
     * @param wm writing mode
     */
    protected void adjustIPD(Rectangle vpRect, int wm) {
        int offset = 0;
        Region start = getSiblingRegion(Region.START);
        if (start != null) {
            offset = start.getExtent();
            vpRect.translate(offset, 0);
        }
        Region end = getSiblingRegion(Region.END);
        if (end != null) {
            offset += end.getExtent();
        }
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB) {
                vpRect.width -= offset;
            } else {
                vpRect.height -= offset;
            }
        }
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveVisitor(this);
    }

}

