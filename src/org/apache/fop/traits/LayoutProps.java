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
package org.apache.fop.traits;

import org.apache.fop.fo.properties.Constants;

/**
 * Store properties affecting layout: break-before, break-after, keeps, span.
 * for a block level FO.
 * Public "structure" allows direct member access.
 */
public class LayoutProps {

    public int breakBefore; // enum constant BreakBefore.xxx
    public int breakAfter; // enum constant BreakAfter.xxx
    public boolean bIsSpan;
    public SpaceVal spaceBefore;
    public SpaceVal spaceAfter;

    private static final int[] BREAK_PRIORITIES =
        new int[]{ Constants.AUTO, Constants.COLUMN, Constants.PAGE };


    public LayoutProps() {
        breakBefore = breakAfter = Constants.AUTO;
        bIsSpan = false;
    }

    //     public static int higherBreak(int brkParent, int brkChild) {
    // if (brkParent == brkChild) return brkChild;
    // for (int i=0; i < s_breakPriorities.length; i++) {
    //     int bp = s_breakPriorities[i];
    //     if (bp == brkParent) return brkChild;
    //     else if (bp == brkChild) return brkParent;
    // }
    // return brkChild;
    //     }

    public void combineWithParent(LayoutProps parentLP) {
        if (parentLP.breakBefore != breakBefore) {
            for (int i = 0; i < BREAK_PRIORITIES.length; i++) {
                int bp = BREAK_PRIORITIES[i];
                if (bp == breakBefore) {
                    breakBefore = parentLP.breakBefore;
                    break;
                } else if (bp == parentLP.breakBefore) {
                    break;
                }
            }
        }
        // Parent span always overrides child span
        bIsSpan = parentLP.bIsSpan;
    }
}

