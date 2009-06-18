/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.traits;

import org.apache.fop.datatypes.Length;

/**
 * Store all block-level layout properties on an FO.
 * Public "structure" allows direct member access.
 */
public class BlockProps {
    public int firstIndent; // text-indent
    public int lastIndent;  // last-line-indent
    public int textAlign;
    public int textAlignLast;
    public int lineStackType; // line-stacking-strategy (enum)

    public BlockProps() {}

}
