/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.CompoundDatatype;

/**
 * @author me
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * This class extends Property.Maker with support for sub-properties.  
 */
public class CompoundPropertyMaker extends Property.Maker {
    /**
     *  The list of subproperty makers supported by this compound maker.
     */ 
    private Property.Maker[] subproperties = 
                    new Property.Maker[Constants.COMPOUND_COUNT];

    /**
     *  The first subproperty maker which has a setByShorthand of true.
     */
    private Property.Maker shorthandMaker = null;

    /**
     * Construct an instance of a CompoundPropertyMaker for the given property.
     * @param propId The Constant ID of the property to be made.
     */
    public CompoundPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * @see org.apache.fop.fo.Property#useGeneric(Property.Maker)
     */
    public void useGeneric(Property.Maker generic) {
        super.useGeneric(generic);
        if (generic instanceof CompoundPropertyMaker) {
            CompoundPropertyMaker compoundGeneric = (CompoundPropertyMaker) generic;
            for (int i = 0; i < Constants.COMPOUND_COUNT; i++) {
                Property.Maker submaker = compoundGeneric.subproperties[i];
                if (submaker != null) {
                    addSubpropMaker((Property.Maker) submaker.clone());
                }
            }
        }
    }
    
    /**
     * Add a subproperty to this maker.
     * @param subproperty
     */
    public void addSubpropMaker(Property.Maker subproperty) {
        // Place the base propId in the propId of the subproperty.
        subproperty.propId &= Constants.COMPOUND_MASK;
        subproperty.propId |= this.propId;
        
        subproperties[getSubpropIndex(subproperty.getPropId())] = subproperty;

        // Store the first subproperty with a setByShorthand. That subproperty
        // will be used for converting a value set on the base property.
        if (shorthandMaker == null && subproperty.setByShorthand) {
            shorthandMaker = subproperty;
        }
    }
    
    
    /**
     * Return a Maker object which is used to set the values on components
     * of compound property types, such as "space".
     * Overridden by property maker subclasses which handle
     * compound properties.
     * @param subprop The Constants ID of the component for which a Maker is to
     * returned, for example CP_OPTIMUM, if the FO attribute is
     * space.optimum='10pt'.
     * @return the Maker object specified
     */
    public Property.Maker getSubpropMaker(int subpropId) {
        return subproperties[getSubpropIndex(subpropId)];
    }
    
    /**
     * Calculate the real value of a subproperty by unmasking and shifting
     * the value into the range [0 - (COMPOUND_COUNT-1)].
     * The value is used as index into the subproperties array. 
     * @param propId the property id of the sub property.
     * @return the array index.
     */
    private int getSubpropIndex(int subpropId) {
        return ((subpropId & Constants.COMPOUND_MASK) >>
                                    Constants.COMPOUND_SHIFT)-1;
    }

    /**
     * For compound properties which can take enumerate values.
     * Delegate the enumeration check to one of the subpropeties. 
     * @param value the string containing the property value
     * @return the Property encapsulating the enumerated equivalent of the
     * input value
     */
    protected Property checkEnumValues(String value) {
        if (shorthandMaker != null) {
            return shorthandMaker.checkEnumValues(value);
        }
        return null;
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param subpropId  The subproperty id of the property being retrieved.
     *        Is 0 when retriving a base property.
     * @param propertylist The PropertyList object being built for this FO.
     * @param bTryInherit true if inherited properties should be examined.
     * @param bTryDefault true if the default value should be returned. 
     */
    public Property get(int subpropId, PropertyList propertyList,
                        boolean bTryInherit, boolean bTryDefault)
        throws FOPException
    {
        Property p = super.get(subpropId, propertyList, bTryInherit, bTryDefault);
        if (subpropId != 0 && p != null) {
            p = getSubprop(p, subpropId);
        }
        return p;
    }
   
    /**
     * Return a Property object based on the passed Property object.
     * This method is called if the Property object built by the parser
     * isn't the right type for this compound property.
     * @param p The Property object return by the expression parser
     * @param propertyList The PropertyList object being built for this FO.
     * @param fo The current FO whose properties are being set.
     * @return A Property of the correct type or null if the parsed value
     * can't be converted to the correct type.
     * @throws FOPException for invalid or inconsistent FO input
     */
    protected Property convertProperty(Property p,
                                    PropertyList propertyList,
                                    FObj fo) throws FOPException {
        if (!EnumProperty.class.isAssignableFrom(p.getClass())) {
            // delegate to the subprop maker to do conversions
            p = shorthandMaker.convertProperty(p, propertyList, fo);
        }
        
        if (p != null) {
            Property prop = makeCompound(propertyList, fo);
            CompoundDatatype pval = (CompoundDatatype) prop.getObject();
            for (int i = 0; i < Constants.COMPOUND_COUNT; i++) {
                Property.Maker submaker = subproperties[i];
                if (submaker != null && submaker.setByShorthand) {
                    pval.setComponent(submaker.getPropId() & Constants.COMPOUND_MASK, p, false);
                }
            }
            return prop;
        }
        return null;
    }

    /**
     * Make a compound property with default values. 
     * @param propertyList The PropertyList object being built for this FO.
     * @return the Property object corresponding to the parameters
     * @throws FOPException for invalid or inconsisten FO input
     */
    public Property make(PropertyList propertyList) throws FOPException {
        return makeCompound(propertyList, propertyList.getParentFObj());       
    }
    
    /**
     * Create a Property object from an attribute specification.
     * @param propertyList The PropertyList object being built for this FO.
     * @param value The attribute value.
     * @param fo The current FO whose properties are being set.
     * @return The initialized Property object.
     * @throws FOPException for invalid or inconsistent FO input
     */    
    public Property make(PropertyList propertyList, String value,
                         FObj fo) throws FOPException {
        Property p = super.make(propertyList, value, fo);
        p = convertProperty(p, propertyList, fo);
        return p; 
    }
    
    /**
     * Return a property value for a compound property. If the property
     * value is already partially initialized, this method will modify it.
     * @param baseProp The Property object representing the compound property,
     * for example: SpaceProperty.
     * @param subpropId The Constants ID of the subproperty (component)
     *        whose value is specified.
     * @param propertyList The propertyList being built.
     * @param fo The FO whose properties are being set.
     * @param value the value of the
     * @return baseProp (or if null, a new compound property object) with
     * the new subproperty added
     * @throws FOPException for invalid or inconsistent FO input
     */
    public Property make(Property baseProp, int subpropId,
                         PropertyList propertyList, String value,
                         FObj fo) throws FOPException {
        if (baseProp == null) {
            baseProp = makeCompound(propertyList, fo);
        }

        Property.Maker spMaker = getSubpropMaker(subpropId);

        if (spMaker != null) {
            Property p = spMaker.make(propertyList, value, fo);
            if (p != null) {
                return setSubprop(baseProp, subpropId & Constants.COMPOUND_MASK, p);
            }
        } else {
            //getLogger().error("compound property component "
            //                       + partName + " unknown.");
        }
        return baseProp;
    }
    
    /**
     * Create a empty compound property and fill it with default values for
     * the subproperties.
     * @param propertyList The propertyList being built.
     * @param parentFO The parent FO for the FO whose property is being made.
     * @return a Property subclass object holding a "compound" property object
     *         initialized to the default values for each component.
     * @throws FOPException
     */
    protected Property makeCompound(PropertyList propertyList, FObj parentFO)
        throws FOPException
    {
        Property p = makeNewProperty();
        CompoundDatatype data = (CompoundDatatype) p.getObject();
        for (int i = 0; i < Constants.COMPOUND_COUNT; i++) {
            Property.Maker submaker = subproperties[i];
            if (submaker != null) {
                Property subprop = submaker.make(propertyList, submaker.defaultValue, parentFO);
                data.setComponent(submaker.getPropId() & Constants.COMPOUND_MASK, subprop, true);
            }
        }
        return p;
    }    
}