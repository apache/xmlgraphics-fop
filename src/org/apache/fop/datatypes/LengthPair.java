/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;

/**
 * This datatype hold a pair of lengths, specifiying the dimensions in
 * both inline and block-progression-directions.
 * It is currently only used to specify border-separation in tables.
 */
public class LengthPair implements CompoundDatatype {

    private Property ipd;
    private Property bpd;

    // From CompoundDatatype
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault) {
        if (sCmpnName.equals("block-progression-direction"))
            bpd = cmpnValue;
        else if (sCmpnName.equals("inline-progression-direction"))
            ipd = cmpnValue;
    }

    // From CompoundDatatype
    public Property getComponent(String sCmpnName) {
        if (sCmpnName.equals("block-progression-direction"))
            return getBPD();
        else if (sCmpnName.equals("inline-progression-direction"))
            return getIPD();
        else
            return null;    // SHOULDN'T HAPPEN
    }

    public Property getIPD() {
        return this.ipd;
    }

    public Property getBPD() {
        return this.bpd;
    }

}
