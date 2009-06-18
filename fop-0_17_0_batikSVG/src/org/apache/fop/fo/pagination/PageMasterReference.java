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

/**
 * Base PageMasterReference class. Provides implementation for handling the
 * master-name attribute and containment within a PageSequenceMaster
 */
public abstract class PageMasterReference extends FObj 
    implements SubSequenceSpecifier 
{

    private String _masterName;
    private PageSequenceMaster _pageSequenceMaster;

    public PageMasterReference(FObj parent, PropertyList propertyList)
		throws FOPException 
    {
	super(parent, propertyList);
	this.name = getElementName();
	if (getProperty("master-name") != null) {
	    setMasterName(getProperty("master-name").getString() );
	}
	validateParent(parent);

    }
		
    protected void setMasterName( String masterName )
    {
	_masterName = masterName;
    }
    
    /**
     * Returns the "master-name" attribute of this page master reference
     */
    public String getMasterName()
    {
	return _masterName;
    }

    protected void setPageSequenceMaster(PageSequenceMaster pageSequenceMaster) 
    {
	_pageSequenceMaster = pageSequenceMaster;
    }

    protected PageSequenceMaster getPageSequenceMaster() 
    {
	return _pageSequenceMaster;
    }
    
    public abstract String getNextPageMaster(int currentPageNumber,
					     boolean thisIsFirstPage, boolean isEmptyPage);
    
    /** 
     * Gets the formating object name for this object. Subclasses must provide this.
     *
     * @return the element name of this reference. e.g. fo:repeatable-page-master-reference
     */
    protected abstract String getElementName();

    /**
     * Checks that the parent is the right element. The default implementation
     * checks for fo:page-sequence-master
     */
    protected  void validateParent(FObj parent) 
	throws FOPException
    {
	if (parent.getName().equals("fo:page-sequence-master")) {
	   _pageSequenceMaster = (PageSequenceMaster)parent;

	    if (getMasterName() == null) {
		MessageHandler.errorln("WARNING: " + getElementName() +
		    " does not have a master-name and so is being ignored");
	    } else {
		_pageSequenceMaster.addSubsequenceSpecifier(this);
	    }
	} else {
	    throw new FOPException(getElementName() + " must be" +
                "child of fo:page-sequence-master, not " 
		+ parent.getName());
	}
    }
    
    public abstract void reset() ;
    
  
    

}
