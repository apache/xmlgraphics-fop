/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.apps.FOPException;

/**
 * This class holds information about text-decoration
 *
 */
public class TextState {

    protected boolean underlined;
    protected boolean overlined;
    protected boolean linethrough;

    public TextState() throws FOPException {}

    /**
     * @return true if text should be underlined
     */
    public boolean getUnderlined() {
        return underlined;
    }

    /**
     * set text as underlined
     */
    public void setUnderlined(boolean ul) {
        this.underlined = ul;
    }

    /**
     * @return true if text should be overlined
     */
    public boolean getOverlined() {
        return overlined;
    }

    public void setOverlined(boolean ol) {
        this.overlined = ol;
    }

    public boolean getLineThrough() {
        return linethrough;
    }

    public void setLineThrough(boolean lt) {
        this.linethrough = lt;
    }

}
