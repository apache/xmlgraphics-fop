/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Iterator;
import java.util.NoSuchElementException;


public interface CharIterator extends Iterator {

    char nextChar() throws NoSuchElementException ;
    void replaceChar(char c);
    Object clone();
}
