/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 * The normal flow reference area class.
 * This area contains a list of block areas from the flow
 */
public class Flow extends BlockParent {
    // the list of blocks created from the flow
    private int stacking = TB;
    private int width;

}

