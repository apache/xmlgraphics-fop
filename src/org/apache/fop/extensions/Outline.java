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

package org.apache.fop.extensions;

import org.apache.fop.fo.*;
import org.apache.fop.pdf.PDFGoTo;
import org.apache.fop.pdf.PDFAction;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.messaging.MessageHandler;

import java.util.*;


public class Outline extends ExtensionObj 
{
    private Label _label;
    private Vector _outlines = new Vector();
    
    private String _internalDestination;
    private String _externalDestination;
    
    /** The parent outline object if it exists */
    private Outline _parentOutline;
    
    /** an opaque renderer context object, e.g. PDFOutline for PDFRenderer */
    private Object _rendererObject;
    
    
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList) 
	{
	    return new Outline(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new Outline.Maker();
    }
    
    public Outline(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	
	_internalDestination = this.properties.get("internal-destination").getString();
	_externalDestination = this.properties.get("external-destination").getString();
	if (_externalDestination != null && !_externalDestination.equals("")) {
	    MessageHandler.errorln("WARNING: fox:outline external-destination not supported currently.");
	}
	
	if (_internalDestination == null || _internalDestination.equals("")) {
	    MessageHandler.errorln("WARNING: fox:outline requires an internal-destination.");
	}

	for (FONode node = getParent(); node != null; node = node.getParent()) {
	    if (node instanceof Outline) {
		_parentOutline = (Outline)node;
		break;
	    }
	}
	
    }

    protected void addChild(FONode obj) 
    {
	if (obj instanceof Label) {
	    _label = (Label)obj;
	}
	else if (obj instanceof Outline) {
	    _outlines.addElement(obj);
	}
	super.addChild(obj);
    }
    

    public void setRendererObject(Object o) 
    {
	_rendererObject = o;
    }

    public Object getRendererObject() 
    {
	return _rendererObject;
    }
    
    public Outline getParentOutline() 
    {
	return _parentOutline;
    }
    
    public Label getLabel() 
    {
	return _label == null?new Label(this,this.properties):_label;
    }
    
    public Vector getOutlines() 
    {
	return _outlines;
    }
    
    public String getInternalDestination() 
    {
	return _internalDestination;
    }
    
    

}

