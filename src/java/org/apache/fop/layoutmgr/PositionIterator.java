/*
 * $Id: PositionIterator.java,v 1.11 2003/03/07 07:58:51 jeremias Exp $
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
package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class PositionIterator implements Iterator {
    
    private Iterator parentIter;
    private Object nextObj;
    private LayoutProcessor childLM;
    private boolean bHasNext;

    PositionIterator(Iterator pIter) {
        parentIter = pIter;
        lookAhead();
        //checkNext();
    }

    public LayoutProcessor getNextChildLM() {
        // Move to next "segment" of iterator, ie: new childLM
        if (childLM == null && nextObj != null) {
            childLM = getLM(nextObj);
            bHasNext = true;
        }
        return childLM;
    }

    protected abstract LayoutProcessor getLM(Object nextObj);

    protected abstract Position getPos(Object nextObj);

    private void lookAhead() {
        if (parentIter.hasNext()) {
            bHasNext = true;
            nextObj = parentIter.next();
        } else {
            endIter();
        }
    }

    protected boolean checkNext() {
        LayoutProcessor lm = getLM(nextObj);
        if (childLM == null) {
            childLM = lm;
        } else if (childLM != lm) {
            // End of this sub-sequence with same child LM
            bHasNext = false;
            childLM = null;
            return false;
        }
        return true;
    }

    protected void endIter() {
        bHasNext = false;
        nextObj = null;
        childLM = null;
    }

    public boolean hasNext() {
        return (bHasNext && checkNext());
    }


    public Object next() throws NoSuchElementException {
        if (bHasNext) {
            Object retObj = getPos(nextObj);
            lookAhead();
            return retObj;
        } else {
            throw new NoSuchElementException("PosIter");
        }
    }

    protected Object peekNext() {
        return nextObj;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("PositionIterator doesn't support remove");
    }
}

