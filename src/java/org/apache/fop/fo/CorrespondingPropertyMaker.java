/*
 * Created on Jan 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;

/**
 */
public class CorrespondingPropertyMaker {
    protected Property.Maker baseMaker;
    protected int lr_tb;
    protected int rl_tb;
    protected int tb_rl;
    private boolean useParent;
    private boolean relative;
    
    CorrespondingPropertyMaker(Property.Maker baseMaker) {
        this.baseMaker = baseMaker;
        baseMaker.setCorresponding(this);
    }
    
    
    public void setCorresponding(int lr_tb, int rl_tb, int tb_rl) {
        this.lr_tb = lr_tb;
        this.rl_tb = rl_tb;
        this.tb_rl = tb_rl;
    }
    
    public void setUseParent(boolean useParent) {
        this.useParent = useParent;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }
    
    /**
     * For properties that operate on a relative direction (before, after,
     * start, end) instead of an absolute direction (top, bottom, left,
     * right), this method determines whether a corresponding property
     * is specified on the corresponding absolute direction. For example,
     * the border-start-color property in a lr-tb writing-mode specifies
     * the same thing that the border-left-color property specifies. In this
     * example, if the Maker for the border-start-color property is testing,
     * and if the border-left-color is specified in the properties,
     * this method should return true.
     * @param propertyList collection of properties to be tested
     * @return true iff 1) the property operates on a relative direction,
     * AND 2) the property has a corresponding property on an absolute
     * direction, AND 3) the corresponding property on that absolute
     * direction has been specified in the input properties
     */
    public boolean isCorrespondingForced(PropertyList propertyList) {
        if (!relative) {
            return false;
        }
        PropertyList pList;
        if (useParent) {
            pList = propertyList.getParentFObj().propertyList;
        } else {
            pList = propertyList;
        }
        int correspondingId = pList.wmMap(lr_tb, rl_tb, tb_rl);
        if (propertyList.getExplicit(correspondingId) != null)
            return true;
        return false;            
    }
    
    /**
     * Return a Property object representing the value of this property,
     * based on other property values for this FO.
     * A special case is properties which inherit the specified value,
     * rather than the computed value.
     * @param propertyList The PropertyList for the FO.
     * @return Property A computed Property value or null if no rules
     * are specified (in foproperties.xml) to compute the value.
     * @throws FOPException for invalid or inconsistent FO input
     */
    public Property compute(PropertyList propertyList) throws FOPException {
        PropertyList pList;
        if (useParent) {
            pList = propertyList.getParentPropertyList();
            if (pList == null) {
                return null;
            }
        } else {
            pList = propertyList;
        }
        int correspondingId = pList.wmMap(lr_tb, rl_tb, tb_rl);
            
        Property p = propertyList.getExplicitOrShorthand(correspondingId);
        if (p != null) {
            FObj parentFO = propertyList.getParentFObj();
            p = baseMaker.convertProperty(p, propertyList, parentFO);
        }
        return p;
    }
}

