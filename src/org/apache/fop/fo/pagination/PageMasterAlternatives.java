/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.Iterator;

// FOP
import org.apache.fop.fo.pagination.PageCondition;

/**
 * Encodes an fo:repeatable-page-master-alternatives and associated
 * conditions.
 */
public class PageMasterAlternatives {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Constant representing the <i>no-limit</i> value of the
	    maximum-repeats property. */
    public static final int NO_LIMIT = -1;

    /** The minumum number of repeats for this set of alternatives. */
    public final int minRepeats;
    /** The maximum-repeats value for this set of alternatives. */
    public final int maxRepeats;

    private ArrayList alternatives = new ArrayList(1);

    /**
     * @param minRepeats - the <tt>int</tt> minimum number of repeats for
     * this alternative.
     * @param maxRepeats - the <tt>int</tt> maximum number of repeats for
     * this alternative.  Set from the <i>maximum-repeats</i> property.
     */
    public PageMasterAlternatives(int minRepeats, int maxRepeats) {
	this.minRepeats = minRepeats;
	this.maxRepeats = maxRepeats;
    }

    /**
     * Add a new alternative condition set.
     * @param condition - a <tt>PageCondition</tt>.
     */
    public void addCondition(PageCondition condition) {
	alternatives.add(condition);
    }

    /**
     * Get an <tt>Iterator</tt> over the set of alternative conditions.
     * @return the <tt>Iterator</tt>.
     */
    public Iterator conditions() {
	return alternatives.iterator();
    }
}
