/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.datatypes.Length;

/**
 * Store all hyphenation related properties on an FO.
 * Public "structure" allows direct member access.
 */
public class AbsolutePositionProps {
    public int absolutePosition;
    public Length top;
    public Length right;
    public Length bottom;
    public Length left;

    public AbsolutePositionProps() {}

}
