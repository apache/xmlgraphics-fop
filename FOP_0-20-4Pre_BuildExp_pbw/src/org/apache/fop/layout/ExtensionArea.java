/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.fo.*;

import org.apache.fop.extensions.*;
import org.apache.fop.render.*;

public class ExtensionArea extends Area {
    private ExtensionObj _extensionObj;

    public ExtensionArea(ExtensionObj obj) {
        super(null);
        _extensionObj = obj;
    }

    public FObj getExtensionObj() {
        return _extensionObj;
    }

    public void render(Renderer renderer) {
        // renderer.renderExtensionArea(this);
    }


}
