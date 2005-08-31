/*
 * SimplePercentBaseContext.java
 *
 * Created on 29 August 2005, 17:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.FObj;

/**
 * Class to implement a simple lookup context for a single percent base value.
 */
public class SimplePercentBaseContext implements PercentBaseContext {
    
    private PercentBaseContext parentContext;
    private int lengthBase;
    private int lengthBaseValue;

    /**
     * @param parentContext the context to be used for all percentages other than lengthBase
     * @param lengthBase the particular percentage length base for which this context provides a value
     * @param lengthBaseValue the value to be returned for requests to the given lengthBase
     */
    public SimplePercentBaseContext(PercentBaseContext parentContext,
                             int lengthBase,
                             int lengthBaseValue) {
        this.parentContext = parentContext;
        this.lengthBase = lengthBase;
        this.lengthBaseValue = lengthBaseValue;
    }

    /**
     * Returns the value for the given lengthBase.
     * @see org.apache.fop.datatypes.PercentBaseContext#getBaseLength(int, fobj)
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        // if its for us return our value otherwise delegate to parent context
        if (lengthBase == this.lengthBase) {
            return lengthBaseValue;
        } else if (parentContext != null) {
            return parentContext.getBaseLength(lengthBase, fobj);
        }
        return -1;
    }

}
