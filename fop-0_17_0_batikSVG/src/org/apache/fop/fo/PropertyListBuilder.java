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
    
  /** Name of font-size property attribute to set first. */
    private static final String FONTSIZEATTR = "font-size";
    
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
	  try {
	    p = propertyMaker.compute(propertyList);
          } catch (FOPException e) {
    	     MessageHandler.errorln("ERROR: exception occurred while computing" +
    	         " value of property '" + propertyName +"': " + e.getMessage());
          }
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
    
    public PropertyList makeList(String elementName, Attributes attributes, PropertyList parentPropertyList, FObj parentFO) throws FOPException {
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

        /* Store names of properties already set. */
	StringBuffer propsDone = new StringBuffer(256);
	propsDone.append(' ');

	/* If font-size is set on this FO, must set it first, since
	 * other attributes specified in terms of "ems" depend on it.
	 * When we do "shorthand" properties, must handle the "font"
	 * property as well to see if font-size is set.
	 */
	String fontsizeval=attributes.getValue(FONTSIZEATTR);
	if (fontsizeval != null) {
	  Property.Maker propertyMaker = findMaker(table, FONTSIZEATTR);
	  if (propertyMaker != null) {
	    try {
	      p.put(FONTSIZEATTR, propertyMaker.make(p,fontsizeval,parentFO));
	    } catch (FOPException e) { }
	  }
	  // Put in the "done" list even if error or no Maker.
	  propsDone.append(FONTSIZEATTR + ' ');
	}	

	for (int i = 0; i < attributes.getLength(); i++) {
	    String attributeName = attributes.getQName(i);
	    /* Handle "compound" properties, ex. space-before.minimum */
	    int sepchar = attributeName.indexOf('.');
	    String propName = attributeName;
	    String subpropName = null;
	    Property propVal = null;
	    if (sepchar > -1) {
		propName = attributeName.substring(0,sepchar);
		subpropName = attributeName.substring(sepchar+1);
	    }
	    else if (propsDone.toString().indexOf(' '+propName+' ') != -1) {
		// Already processed this property (base property
		// for a property with sub-components or font-size)
		continue;
	    }

	    Property.Maker propertyMaker =findMaker(table, propName);

	    if (propertyMaker != null) {
	      try {
		if (subpropName != null) {
		    Property baseProp = p.getExplicit(propName);
		    if (baseProp == null) {
			// See if it is specified later in this list
			String baseValue = attributes.getValue(propName);
			if (baseValue != null) {
			    baseProp = propertyMaker.make(p, baseValue, parentFO);
			    propsDone.append(propName + ' ');
			}
			//else baseProp = propertyMaker.makeCompound(p, parentFO);
		    }
		    propVal = propertyMaker.make(baseProp, subpropName, p,
						 attributes.getValue(i),parentFO);
		}
		else {
		    propVal = propertyMaker.make(p,attributes.getValue(i),parentFO);
		}
		if (propVal != null) {
		    p.put(propName,propVal);
		}
	      } catch (FOPException e) { /* Do other props. */  }
	    } else {
	      if (! attributeName.startsWith("xmlns"))
		MessageHandler.errorln("WARNING: property '" +
				       attributeName + "' ignored");
	    }
	}
	
	return p;
    }

  public Property getSubpropValue(String space, String element,
				  String propertyName, Property p,
				  String subpropName) {
    Property.Maker maker = findMaker(space, element, propertyName);
    if (maker != null) {
      return maker.getSubpropValue(p, subpropName);
    }
    else return null;
  }
    
  
    public boolean isCorrespondingForced(PropertyList propertyList, String space,
    	 String element, String propertyName) {
	Property.Maker propertyMaker = findMaker(space, element, propertyName);
	if (propertyMaker != null) {
	    return propertyMaker.isCorrespondingForced(propertyList);
	} else {
	    MessageHandler.errorln("WARNING: no Maker for " + propertyName);
	}
	return false;
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
      return findMaker((Hashtable)elementTable.get(elementName),
		       propertyName);
    }

  /**
   * Convenience function to return the Maker for a given property
   * given the Hashtable containing properties specific to this element.
   * If table is non-null and
   * @param elemTable Element-specific properties or null if none.
   * @param propertyName Name of property.
   * @return A Maker for this property.
   */
  private Property.Maker findMaker(Hashtable elemTable, String propertyName) {
    Property.Maker propertyMaker = null;
    if (elemTable != null) {
      propertyMaker = (Property.Maker)elemTable.get(propertyName);
    }
    if (propertyMaker == null) {
      propertyMaker = (Property.Maker)propertyListTable.get(propertyName);
    }
    return propertyMaker;
  }

}
