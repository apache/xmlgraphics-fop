/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.mathml;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.XMLObj;

/**
 * Catch all MathML object as default element.
 */
public class MathMLObj extends XMLObj {

    public MathMLObj(FONode parent) {
        super(parent);
    }

    public String getNameSpace() {
        return MathMLElementMapping.URI;
    }
}

