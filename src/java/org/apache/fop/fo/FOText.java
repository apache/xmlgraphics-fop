/*
 * $Id: FOText.java,v 1.43 2003/03/05 21:48:01 jeremias Exp $
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

// Java
import java.util.NoSuchElementException;
import java.util.List;

// FOP
import org.apache.fop.layout.TextState;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.TextLayoutManager;
import org.apache.fop.apps.StructureHandler;
import org.apache.fop.fo.properties.WhiteSpaceCollapse;

/**
 * A text node in the formatting object tree.
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com.
 * Unfortunately the BufferManager implementatation holds
 * onto references to the character data in this object
 * longer than the lifetime of the object itself, causing
 * excessive memory consumption and OOM errors.
 */
public class FOText extends FObj {

    protected char[] ca;
    protected int start;
    protected int length;
    TextInfo textInfo;
    TextState ts;

    public FOText(char[] chars, int s, int e, TextInfo ti) {
        super(null);
        this.start = 0;
        this.ca = new char[e - s];
        System.arraycopy(chars, s, ca, 0, e - s);
        this.length = e - s;
        textInfo = ti;
    }

    public void setStructHandler(StructureHandler st) {
        super.setStructHandler(st);
        structHandler.characters(ca, start, length);
    }

    /**
     * Check if this text node will create an area.
     * This means either there is non-whitespace or it is
     * preserved whitespace.
     * Maybe this just needs to check length > 0, since char iterators
     * handle whitespace.
     *
     * @return true if this will create an area in the output
     */
    public boolean willCreateArea() {
        if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE
                && length > 0) {
            return true;
        }

        for (int i = start; i < start + length; i++) {
            char ch = ca[i];
            if (!((ch == ' ') 
                    || (ch == '\n') 
                    || (ch == '\r')
                    || (ch == '\t'))) { // whitespace
                return true;
            }
        }
        return false;
    }

    public void addLayoutManager(List list) {
        // if nothing left (length=0)?
        if (length == 0) {
            return;
        }

        if (length < ca.length) {
            char[] tmp = ca;
            ca = new char[length];
            System.arraycopy(tmp, 0, ca, 0, length);
        }
        LayoutManager lm = new TextLayoutManager(ca, textInfo);
        lm.setFObj(this);
        list.add(lm);
    }

    public CharIterator charIterator() {
        return new TextCharIterator();
    }

    private class TextCharIterator extends AbstractCharIterator {
        private int curIndex = 0;
        
        public boolean hasNext() {
            return (curIndex < length);
        }

        public char nextChar() {
            if (curIndex < length) {
                // Just a char class? Don't actually care about the value!
                return ca[curIndex++];
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (curIndex > 0 && curIndex < length) {
                // copy from curIndex to end to curIndex-1
                System.arraycopy(ca, curIndex, ca, curIndex - 1,
                                 length - curIndex);
                length--;
                curIndex--;
            } else if (curIndex == length) {
                curIndex = --length;
            }
        }


        public void replaceChar(char c) {
            if (curIndex > 0 && curIndex <= length) {
                ca[curIndex - 1] = c;
            }
        }


    }
}

