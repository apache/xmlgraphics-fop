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

import org.apache.fop.fo.properties.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.svg.*;
import org.apache.fop.datatypes.*;

import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

import java.util.Hashtable;

public class PropertyListBuilder {
    
    private Hashtable propertyListTable;
	private Hashtable elementTable;

    public PropertyListBuilder() {
        this.propertyListTable = new Hashtable();
        this.elementTable = new Hashtable();
	}

    public void addList(Hashtable list)
    {
        propertyListTable = list; // should add all
    }

    public void addElementList(String element, Hashtable list)
    {
        elementTable.put(element, list);
    }

    public Property computeProperty(PropertyList propertyList, String space, String element, String propertyName) {
	
	Property p = null;
	Property.Maker propertyMaker = findMaker(space, element, propertyName);
	if (propertyMaker != null) {
	    p = propertyMaker.compute(propertyList);
	} else {
	    MessageHandler.errorln("WARNING: property " + propertyName + " ignored");
	}
	return p;
    }
    
    public boolean isInherited(String space, String element, String propertyName) {
	boolean b;
	
	Property.Maker propertyMaker = findMaker(space, element, propertyName);
	if (propertyMaker != null) {
	    b = propertyMaker.isInherited();
	} else {
	    //MessageHandler.errorln("WARNING: Unknown property " + propertyName);
	    b = true;
	}
	return b;
    }
    
    public PropertyList makeList(String elementName, Attributes attributes, PropertyList parentPropertyList) throws FOPException {
	int index = elementName.indexOf("^");
	String space = "http://www.w3.org/TR/1999/XSL/Format";
	if(index != -1) {
		space = elementName.substring(0, index);
	}

	PropertyList par = null;
	if(parentPropertyList != null && space.equals(parentPropertyList.getNameSpace())) {
		par = parentPropertyList;
	}
//	System.out.println(elementName.substring(index + 1));
	PropertyList p = new PropertyList(par, space, elementName.substring(index + 1));
	p.setBuilder(this);
    Hashtable table;
    table = (Hashtable)elementTable.get(elementName.substring(index + 1));
	for (int i = 0; i < attributes.getLength(); i++) {
	    String attributeName = attributes.getQName(i);
	    Property.Maker propertyMaker = null;
    	if(table != null) {
	    	propertyMaker = (Property.Maker)table.get(attributeName);
	    }
	    if(propertyMaker == null) {
        	propertyMaker = (Property.Maker)propertyListTable.get(attributeName);
	    }
	    if (propertyMaker != null) {
		p.put(attributeName,propertyMaker.make(p,attributes.getValue(i)));
	    } else {
		//MessageHandler.errorln("WARNING: property " + attributeName + " ignored");
	    }
	}
	
	return p;
    }
    
    public Property makeProperty(PropertyList propertyList, String space, String element, String propertyName) throws FOPException {
	
	Property p = null;
	
	Property.Maker propertyMaker = findMaker(space, element, propertyName);
	if (propertyMaker != null) {
	    p = propertyMaker.make(propertyList);
	} else {
	    MessageHandler.errorln("WARNING: property " + propertyName + " ignored");
	}
	return p;
    }

    protected Property.Maker findMaker(String space, String elementName, String propertyName)
    {
        Hashtable propertyTable;
	    Property.Maker propertyMaker = null;
        propertyTable = (Hashtable)elementTable.get(propertyName);
	    if(propertyTable != null) {
	        propertyMaker = (Property.Maker)propertyTable.get(propertyName);
	    }
        if(propertyMaker == null) {
        	propertyMaker = (Property.Maker)propertyListTable.get(propertyName);
        }
        return propertyMaker;
    }
}
