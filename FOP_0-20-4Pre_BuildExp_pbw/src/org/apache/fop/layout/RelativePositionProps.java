/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

/**
 * Store all hyphenation related properties on an FO.
 * Public "structure" allows direct member access.
 */
public class RelativePositionProps {
    public int marginTop;
    public int marginBottom;
    public int marginLeft;
    public int marginRight;
    public int spaceBefore;
    public int spaceAfter;
    public int startIndent;
    public int endIndent;

    public RelativePositionProps() {}

}
