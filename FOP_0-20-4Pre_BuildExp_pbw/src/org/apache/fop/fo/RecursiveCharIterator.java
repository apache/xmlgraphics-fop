package org.apache.fop.fo;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;


public class RecursiveCharIterator extends AbstractCharIterator {
    Iterator childIter = null; // Child flow objects
    CharIterator curCharIter = null; // Children's characters
    private FONode fobj;
    private FONode curChild;

    public RecursiveCharIterator(FObj fobj) {
	// Set up first child iterator
	this.fobj = fobj;
	this.childIter = fobj.getChildren();
	getNextCharIter();
    }

    public RecursiveCharIterator(FObj fobj, FONode child) {
	// Set up first child iterator
	this.fobj = fobj;
	this.childIter = fobj.getChildren(child);
	getNextCharIter();
    }

    public CharIterator mark() {
	return (CharIterator) this.clone();
    }

    public Object clone() {
	RecursiveCharIterator ci = (RecursiveCharIterator)super.clone();
	ci.childIter = fobj.getChildren(ci.curChild);
	// Need to advance to the next child, else we get the same one!!!
	ci.childIter.next();
	ci.curCharIter = (CharIterator)curCharIter.clone();
	return ci;
    }


    public void replaceChar(char c) {
	if (curCharIter != null) {
	    curCharIter.replaceChar(c);
	}
    }


    private void getNextCharIter() {
	if (childIter.hasNext()) {
	    this.curChild = (FONode)childIter.next();
	    this.curCharIter = curChild.charIterator();
	}
	else {
	    curChild = null;
	    curCharIter = null;
	}
    }

    public boolean hasNext() {
	while (curCharIter != null) {
	    if (curCharIter.hasNext()==false) {
		getNextCharIter();
	    }
	    else return true;
	}
	return false;
    }

    public char nextChar() throws NoSuchElementException {
	if (curCharIter != null) {
	    return curCharIter.nextChar();
	}
	else throw new NoSuchElementException();
    }


    public void remove() {
	if (curCharIter != null) {
	    curCharIter.remove();
	}
    }
}
