/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.pagination.FoSimplePageMaster;

/**
 * Encodes a condition set from an FoConditionalPageReferenceMaster.
 */
public class PageCondition {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The simple page master used when these conditions are true. */
    public final FoSimplePageMaster master;
    /** The blank-or-not-blank condition.
	Encoded from Properties.BlankOrNotBlank. */
    public final int blankOrNot;
    /** The odd-or-even condition.  Encoded from Properties.OddOrEven. */
    public final int oddOrEven;
    /** The page-position condition.  Encoded from Properties.pagePosition. */
    public final int pagePosition;

    /**
     * @param master - a reference to an <i>fo:simple-page-master</i>.
     * @param blankOrNot - an <tt>int</tt> encoding a
     * <i>blank-or-not-blank</i> condition.
     * @param oddOrEven - an <tt>int</tt> encoding an <i>odd-or-even</i>
     * condition.
     * @param pagePosition - an <tt>int</tt> encoding a <i>page-position</i>
     * condition.
     */
    public PageCondition(FoSimplePageMaster master,
			int blankOrNot, int oddOrEven, int pagePosition) {
	this.master       = master;
	this.blankOrNot   = blankOrNot;
	this.oddOrEven    = oddOrEven;
	this.pagePosition = pagePosition;
    }
}
