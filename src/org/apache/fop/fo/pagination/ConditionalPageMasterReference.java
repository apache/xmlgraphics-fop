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
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;                   
import org.apache.fop.messaging.MessageHandler;

public class ConditionalPageMasterReference extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ConditionalPageMasterReference(parent,propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ConditionalPageMasterReference.Maker();
    }

	private String masterName;
	private RepeatablePageMasterAlternatives repeatablePageMasterAlternatives;
	
	private int pagePosition;
	private int oddOrEven;
	private int blankOrNotBlank;
	
    public ConditionalPageMasterReference(FObj parent, PropertyList propertyList)
		throws FOPException {
	super(parent, propertyList);
	this.name = "fo:conditional-page-master-reference";

	if (parent.getName().equals("fo:repeatable-page-master-alternatives")) {
	    this.repeatablePageMasterAlternatives =
			(RepeatablePageMasterAlternatives) parent;
	    setMasterName( this.properties.get("master-name").getString() );
	    if (getMasterName().equals("")) {
		System.err.println("WARNING: conditional-page-master-reference" +
		    "does not have a master-name and so is being ignored");
	    } else {
		this.repeatablePageMasterAlternatives.addConditionalPageMasterReference(this);
	    }
		
	    setPagePosition( this.properties.get("page-position").getEnum() );
	    setOddOrEven( this.properties.get("odd-or-even").getEnum() );
	    setBlankOrNotBlank( this.properties.get("blank-or-not-blank").getEnum() );
		
	} else {
	    throw new FOPException("fo:conditional-page-master-reference must be child "
				   + "of fo:repeatable-page-master-alternatives, not " 
				   + parent.getName());
	}
    }
	
	protected boolean isValid( int currentPageNumber, boolean thisIsFirstPage,
		boolean isEmptyPage )
	{
		// page-position
		boolean okOnPagePosition = true;	// default is 'any'
		switch (getPagePosition()) {
			case PagePosition.FIRST:
				if (!thisIsFirstPage)
					okOnPagePosition = false;
				break;
			case PagePosition.LAST:
				// how the hell do you know at this point?
				MessageHandler.log( "LAST PagePosition NYI" );
				okOnPagePosition = true;
				break;
			case PagePosition.REST:
				if (thisIsFirstPage)
					okOnPagePosition = false;
				break;
			case PagePosition.ANY:
				okOnPagePosition = true;
		}
		
		// odd or even
		boolean okOnOddOrEven = true;	// default is 'any'
		int ooe = getOddOrEven();
		boolean isOddPage = ((currentPageNumber % 2) == 1) ? true : false;
		if ((OddOrEven.ODD == ooe) && !isOddPage)
		{
			okOnOddOrEven = false;
		}
		if ((OddOrEven.EVEN == ooe) && isOddPage) {
			okOnOddOrEven = false;
		} 
		
		// experimental check for blank-or-not-blank
		boolean okOnBlankOrNotBlank = true;		// default is 'any'
		int bnb = getBlankOrNotBlank();
		if ((BlankOrNotBlank.BLANK == bnb) && !isEmptyPage)
		{
			okOnBlankOrNotBlank = false;
		}
		else if ((BlankOrNotBlank.NOT_BLANK == bnb) && isEmptyPage)
		{
			okOnBlankOrNotBlank = false;
		}

		return (okOnOddOrEven && okOnPagePosition && okOnBlankOrNotBlank);
	}
	
    protected void setPagePosition( int pagePosition )
	{
		this.pagePosition = pagePosition;
	}

    protected int getPagePosition()
	{
		return this.pagePosition;
	}

    protected void setOddOrEven( int oddOrEven )
	{
		this.oddOrEven = oddOrEven;
	}

    protected int getOddOrEven()
	{
		return this.oddOrEven;
	}

    protected void setBlankOrNotBlank( int blankOrNotBlank )
	{
		this.blankOrNotBlank = blankOrNotBlank;
	}

    protected int getBlankOrNotBlank()
	{
		return this.blankOrNotBlank;
	}

    public void setMasterName( String masterName )
	{
		this.masterName = masterName;
	}

    public String getMasterName()
	{
		return this.masterName;
	}
	
}
