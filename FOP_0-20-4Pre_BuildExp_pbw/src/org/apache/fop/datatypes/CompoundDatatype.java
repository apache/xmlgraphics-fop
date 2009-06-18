/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;
import org.apache.fop.fo.Property;

public interface CompoundDatatype {
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault);

    public Property getComponent(String sCmpnName);
}
