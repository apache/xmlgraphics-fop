/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.traits;


/**
 * Store all inline "margin" related properties
 * Public "structure" allows direct member access.
 */
public class InlineProps {
    public int marginTop;
    public int marginBottom;
    public int marginLeft;
    public int marginRight;
    public SpaceVal spaceStart;
    public SpaceVal spaceEnd;

    public InlineProps() {}

}
