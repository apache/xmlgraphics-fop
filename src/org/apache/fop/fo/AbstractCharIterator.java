/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import java.util.NoSuchElementException;
    
public abstract class AbstractCharIterator implements CharIterator, Cloneable {

    abstract public boolean hasNext();

    abstract public char nextChar() throws NoSuchElementException ;

    public Object next() throws NoSuchElementException {
	return new Character(nextChar());
    }

    public void remove() {
	throw new UnsupportedOperationException();
    }


    public void replaceChar(char c) {
    }

    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException ex) {
	    return null;
	}
    }
};
