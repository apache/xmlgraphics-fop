/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.mif;

// FOP
import org.apache.fop.layout.FontMetric;

/**
 * base class for PDF font classes
 */
public abstract class Font implements FontMetric {

    /**
     * get the encoding of the font
     */
    public abstract String encoding();

    /**
     * get the base font name
     */
    public abstract String fontName();
}



