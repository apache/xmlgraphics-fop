/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.*;


public class Label extends ExtensionObj {
    private String _label = "";

    public Label(FObj parent) {
        super(parent);
    }

    protected void addCharacters(char data[], int start, int end) {
        _label += new String(data, start, end - start);
    }

    public String toString() {
        return _label;
    }


}
