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
package org.apache.fop.fo;

import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.util.CharUtilities;
import java.util.NoSuchElementException;


public class InlineCharIterator extends RecursiveCharIterator {
    private boolean bStartBoundary = false;
    private boolean bEndBoundary = false;

    public InlineCharIterator(FObj fobj, BorderAndPadding bap) {
        super(fobj);
        checkBoundaries(bap);
    }


    private void checkBoundaries(BorderAndPadding bap) {
        // TODO! use start and end in BAP!!
        bStartBoundary = (bap.getBorderLeftWidth(false) > 0
                       || bap.getPaddingLeft(false) > 0);
        bEndBoundary = (bap.getBorderRightWidth(false) > 0
                     || bap.getPaddingRight(false) > 0);
    }

    public boolean hasNext() {
        if (bStartBoundary) {
            return true;
        }
        return (super.hasNext() || bEndBoundary);
        /* If super.hasNext() returns false,
         * we return true if we are going to return a "boundary" signal
         * else false.
         */
    }

    public char nextChar() throws NoSuchElementException {
        if (bStartBoundary) {
            bStartBoundary = false;
            return CharUtilities.CODE_EOT;
        }
        try {
            return super.nextChar();
        } catch (NoSuchElementException e) {
            // Underlying has nothing more to return
            // Check end boundary char
            if (bEndBoundary) {
                bEndBoundary = false;
                return CharUtilities.CODE_EOT;
            } else {
                throw e;
            }
        }
    }
}

