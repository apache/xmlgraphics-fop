/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

import org.apache.fop.fo.FOUserAgent;

import java.util.HashMap;

/**
 * The Render Context for external handlers.
 * This provides a rendering context so that external handlers
 * can get information to be able to render to the render target.
 */
public class RendererContext {
    String mime;
    FOUserAgent userAgent;
    HashMap props = new HashMap();

    public RendererContext(String m) {
        mime = m;
    }

    public String getMimeType() {
        return mime;
    }

    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    public void setProperty(String name, Object val) {
        props.put(name, val);
    }

    public Object getProperty(String prop) {
        return props.get(prop);
    }

}

