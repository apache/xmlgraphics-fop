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
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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
package org.apache.fop.fo.pagination;

import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;                   
import org.apache.fop.messaging.MessageHandler;

// Java
import java.util.Vector;

public class RepeatablePageMasterAlternatives extends FObj
	implements SubSequenceSpecifier {
	
    private static final int INFINITE = -1;
    

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new RepeatablePageMasterAlternatives(parent,propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new RepeatablePageMasterAlternatives.Maker();
    }

	private PageSequenceMaster pageSequenceMaster;
	
    /**
     * Max times this page master can be repeated.
     * INFINITE is used for the unbounded case */
    private int maximumRepeats;
    private int numberConsumed = 0;
   
    private Vector conditionalPageMasterRefs;

    public RepeatablePageMasterAlternatives(FObj parent, PropertyList propertyList)
		throws FOPException {
	super(parent, propertyList);
	this.name = "fo:repeatable-page-master-alternatives";

	conditionalPageMasterRefs = new Vector();
	
	if (parent.getName().equals("fo:page-sequence-master")) {
	    this.pageSequenceMaster = (PageSequenceMaster) parent;
		this.pageSequenceMaster.addSubsequenceSpecifier(this);
	} else {
	    throw new FOPException("fo:repeatable-page-master-alternatives" +
	        "must be child of fo:page-sequence-master, not " +
		parent.getName());
	}

	String mr = getProperty("maximum-repeats").getString();
	if (mr.equals("no-limit")) {
	    setMaximumRepeats(INFINITE);
	}
	else {
	    try {
		setMaximumRepeats( Integer.parseInt( mr ) );
	    } catch (NumberFormatException nfe) {
		throw new FOPException( "Invalid number for " +
					"'maximum-repeats' property" );
	    }
	}

    }
	
    public String getNextPageMaster( int currentPageNumber, 
				     boolean thisIsFirstPage,
				     boolean isEmptyPage ) {
		String pm = null;
		
		if (getMaximumRepeats() != INFINITE ) {
		    if (numberConsumed < getMaximumRepeats()) {
			numberConsumed++;
		    } else {
			return null;
		    }
		}

		for (int i = 0; i < conditionalPageMasterRefs.size(); i++)
		{
			ConditionalPageMasterReference cpmr =
			(ConditionalPageMasterReference)conditionalPageMasterRefs.elementAt(i);

			// 0-indexed page number
			if (cpmr.isValid(currentPageNumber + 1, thisIsFirstPage,false))
			{
				pm = cpmr.getMasterName();
				break;
			}
		}
		return pm;
    }

	private void setMaximumRepeats( int maximumRepeats)
	{
	    if (maximumRepeats == INFINITE) {
		this.maximumRepeats = maximumRepeats;
	    }
	    else {
		this.maximumRepeats =
		    (maximumRepeats < 0) ? 0 : maximumRepeats;
	    }
	    
	}
	
	private int getMaximumRepeats()
	{
		return this.maximumRepeats;
	}

	public void addConditionalPageMasterReference(
		ConditionalPageMasterReference cpmr)
	{
		this.conditionalPageMasterRefs.addElement( cpmr );
	}
	
    public void reset() 
    {
	this.numberConsumed = 0;
    }
    

    protected PageSequenceMaster getPageSequenceMaster() 
    {
	return pageSequenceMaster;
    }
}
