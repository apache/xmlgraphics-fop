/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Iterator;
import java.util.NoSuchElementException;

    
public class OneCharIterator extends AbstractCharIterator {

    private boolean bFirst=true;
    private char charCode;

    public OneCharIterator(char c) {
	this.charCode = c;
    }

    public boolean hasNext() {
	return bFirst;
    }

    public char nextChar() throws NoSuchElementException {
	if (bFirst) {
	    bFirst=false;
	    return charCode;
	}
	else throw new NoSuchElementException();
    }

}
