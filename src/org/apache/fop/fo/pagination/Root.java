/*-- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;
import org.apache.fop.extensions.ExtensionObj;

// Java
import java.util.Vector;
import java.util.Enumeration;

/**
 * The fo:root formatting object. Contains page masters, root extensions,
 * page-sequences.
 */
public class Root extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Root(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new Root.Maker();
    }

    LayoutMasterSet layoutMasterSet;
    Vector pageSequences;

    /** keeps count of page number from over PageSequence instances*/
    private int runningPageNumberCounter = 0;

    protected Root(FObj parent,
                   PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "fo:root";

      //this.properties.get("media-usage");

        pageSequences = new Vector();
		
        if (parent != null) {
            throw new FOPException("root must be root element");
        }
    }

    protected int getRunningPageNumberCounter() {
        return this.runningPageNumberCounter;
    }

    protected void setRunningPageNumberCounter(int count) {
        this.runningPageNumberCounter = count;
    }

    /** @deprecated handled by addChild now
     */
    public void addPageSequence(PageSequence pageSequence) {
        this.pageSequences.addElement(pageSequence);
    }

	public int getPageSequenceCount() {
		return pageSequences.size();
	}
	
	/**
	 * Some properties, such as 'force-page-count', require a
	 * page-sequence to know about some properties of the next.
	 * @returns succeeding PageSequence; null if none
	*/
	public PageSequence getSucceedingPageSequence(PageSequence current) {
		int currentIndex = pageSequences.indexOf(current);
		if (currentIndex == -1) return null;
		if (currentIndex < (pageSequences.size()-1)) {
			return (PageSequence)pageSequences.elementAt(currentIndex+1);
		} else {
			return null;
		}
	}
	
    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }

    public void format(AreaTree areaTree) throws FOPException {
        //	MessageHandler.errorln(" Root[" + marker + "] ");
        if (layoutMasterSet == null) {
            throw new FOPException("No layout master set.");
        }

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof PageSequence) {
                ((PageSequence) o).format(areaTree);
            } else if (o instanceof ExtensionObj) {
                ((ExtensionObj) o).format(areaTree);
            }
        }
    }
}
