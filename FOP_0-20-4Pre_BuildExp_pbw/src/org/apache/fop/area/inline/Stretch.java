/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.MinOptMax;

public class Stretch extends InlineArea {
    MinOptMax contentIPD;

    public void setAllocationIPD(MinOptMax mom) {
        contentIPD = mom;
    }

    public MinOptMax getAllocationIPD() {
        // Should also account for any borders and padding in the
        // inline progression dimension
        if (contentIPD != null) {
            return contentIPD;
        }
        return super.getAllocationIPD();
    }
}
