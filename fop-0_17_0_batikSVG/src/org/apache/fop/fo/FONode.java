/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.LinkSet;

// Java
import java.util.Vector;

/**
 * base class for nodes in the formatting object tree
 */
abstract public class FONode {

    protected FObj parent;

    public Vector children = new Vector();		// made public for searching for id's

    /** value of marker before layout begins */
    public final static int START = -1000;

    /** value of marker after break-after */
    public final static int BREAK_AFTER = -1001;

    /** 
     * where the layout was up to.
     *  for FObjs it is the child number
     *  for FOText it is the character number
     */
    protected int marker = START;

    protected boolean isInTableCell = false;

    protected int forcedStartOffset = 0;
    protected int forcedWidth = 0;

    protected int widows = 0;
    protected int orphans = 0;

    protected LinkSet linkSet;

    protected FONode(FObj parent) {
	this.parent = parent;
    }

    public void setIsInTableCell() {
	this.isInTableCell = true;
	// made recursive by Eric Schaeffer
	for (int i = 0; i < this.children.size(); i++) {
	    FONode child = (FONode) this.children.elementAt(i);
	    child.setIsInTableCell();
	}
    }

    public void forceStartOffset(int offset) {
	this.forcedStartOffset = offset;
	// made recursive by Eric Schaeffer
	for (int i = 0; i < this.children.size(); i++) {
	    FONode child = (FONode) this.children.elementAt(i);
	    child.forceStartOffset(offset);
	}
    }

    public void forceWidth(int width) {
	this.forcedWidth = width;
	// made recursive by Eric Schaeffer
	for (int i = 0; i < this.children.size(); i++) {
	    FONode child = (FONode) this.children.elementAt(i);
	    child.forceWidth(width);
	}
    }

    public void resetMarker() {
	this.marker = START;
	int numChildren = this.children.size();
	for (int i = 0; i < numChildren; i++) {
	    ((FONode) children.elementAt(i)).resetMarker();
	}
    }

    public void setWidows(int wid)
    {
        widows = wid;
    }

    public void setOrphans(int orph)
    {
        orphans = orph;
    }

    public void removeAreas() {
	// still to do
    }

    protected void addChild(FONode child) {
	children.addElement(child);
    }

    public FObj getParent() {
	return this.parent;
    }

    public void setLinkSet(LinkSet linkSet) {
	this.linkSet = linkSet;        	
	for (int i = 0; i < this.children.size(); i++) {
	    FONode child = (FONode) this.children.elementAt(i);
	    child.setLinkSet(linkSet);
        }
    }

    public LinkSet getLinkSet() {
	return this.linkSet;
    }

    abstract public Status layout(Area area)
	throws FOPException;
	
    /**
    * lets outside sources access the property list
    * first used by PageNumberCitation to find the "id" property
    * returns null by default, overide this function when there is a property list
    *@param name - the name of the desired property to obtain
    * @returns the property 
    */
    public Property getProperty(String name)
    {
    	return(null);
    }

	/**
	 * At the start of a new span area layout may be partway through a
	 * nested FO, and balancing requires rollback to this known point.
	 * The snapshot records exactly where layout is at.
	 * @param snapshot a Vector of markers (Integer)
	 * @returns the updated Vector of markers (Integers)
	 */
	public Vector getMarkerSnapshot(Vector snapshot)
	{
		snapshot.addElement(new Integer(this.marker));
		
		// terminate if no kids or child not yet accessed
		if (this.marker < 0)
			return snapshot;
		else if (children.isEmpty())
			return snapshot;
		else
			return ((FONode) children.elementAt(this.marker)).getMarkerSnapshot(snapshot);
	}
	
	/**
	 * When balancing occurs, the flow layout() method restarts at the
	 * point specified by the current marker snapshot, which is retrieved
	 * and restored using this method.
	 * @param snapshot the Vector of saved markers (Integers)
	 */
	public void rollback(Vector snapshot)
	{
		this.marker = ((Integer)snapshot.elementAt(0)).intValue();
		snapshot.removeElementAt(0);

		if (this.marker == START)
		{
			// make sure all the children of this FO are also reset
			resetMarker();
			return;
		}
		else if ((this.marker == -1) || children.isEmpty())
			return;
			
		int numChildren = this.children.size();
		for (int i = this.marker + 1; i < numChildren; i++) {
			FONode fo = (FONode) children.elementAt(i);
			fo.resetMarker();
		}
		((FONode) children.elementAt(this.marker)).rollback(snapshot);
	}
}
