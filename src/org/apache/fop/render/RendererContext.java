/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

//Java
import java.util.Map;

//FOP
import org.apache.fop.fo.FOUserAgent;

/**
 * The Render Context for external handlers. This provides a rendering context
 * so that external handlers can get information to be able to render to the
 * render target.
 */
public class RendererContext {

    private String mime;
    private FOUserAgent userAgent;
    private Map props = new java.util.HashMap();

    /**
     * Contructor for this class. It takes a MIME type as parameter.
     *
     * @param m  The MIME type of the output that's generated.
     */
    public RendererContext(String m) {
        mime = m;
    }

    /**
     * Returns the MIME type associated with this RendererContext.
     *
     * @return   The MIME type (ex. application/pdf)
     */
    public String getMimeType() {
        return mime;
    }

    /**
     * Sets the user agent.
     *
     * @param ua  The user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    /**
     * Returns the user agent.
     *
     * @return   The user agent
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * Sets a property on the RendererContext.
     *
     * @param name  The key of the property
     * @param val   The value of the property
     */
    public void setProperty(String name, Object val) {
        props.put(name, val);
    }

    /**
     * Returns a property from the RendererContext.
     *
     * @param prop  The key of the property to return.
     * @return      The requested value, <code>null</code> if it doesn't exist.
     */
    public Object getProperty(String prop) {
        return props.get(prop);
    }

}

