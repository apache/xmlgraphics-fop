package org.apache.fop.fo;

import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.util.CharUtilities;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;


public class InlineCharIterator extends RecursiveCharIterator {
    private boolean bStartBoundary=false;
    private boolean bEndBoundary=false;

    public InlineCharIterator(FObj fobj, BorderAndPadding bap) {
	super(fobj);
	checkBoundaries(bap);
    }


    private void checkBoundaries(BorderAndPadding bap) {
	// TODO! use start and end in BAP!!
	bStartBoundary = (bap.getBorderLeftWidth(false)>0 ||
			  bap.getPaddingLeft(false)>0);
	bEndBoundary = (bap.getBorderRightWidth(false)>0 ||
			  bap.getPaddingRight(false)>0);
    }

    public boolean hasNext() {
	if (bStartBoundary) return true;
	return (super.hasNext() || bEndBoundary);
	/* If super.hasNext() returns false,
	 * we return true if we are going to return a "boundary" signal
	 * else false.
	 */
    }

    public char nextChar() throws NoSuchElementException {
	if (bStartBoundary) {
	    bStartBoundary=false;
	    return CharUtilities.CODE_EOT;
	}
	try {
	    return super.nextChar();
	}
	catch (NoSuchElementException e) {
	    // Underlying has nothing more to return
	    // Check end boundary char
	    if (bEndBoundary) {
		bEndBoundary=false;
		return CharUtilities.CODE_EOT;
	    }
	    else throw e;
	}
    }
}
