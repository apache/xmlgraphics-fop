/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render;

//Java
import java.util.Map;

//FOP
import org.apache.fop.apps.FOUserAgent;

/**
 * The Render Context for external handlers. This provides a rendering context
 * so that external handlers can get information to be able to render to the
 * render target.
 */
public class RendererContext {

    private final String mime;
    private final AbstractRenderer renderer;
    private FOUserAgent userAgent;
    private final Map props = new java.util.HashMap();

    /**
     * Contructor for this class. It takes a MIME type as parameter.
     *
     * @param renderer The current renderer
     * @param m  The MIME type of the output that's generated.
     */
    public RendererContext(AbstractRenderer renderer, String m) {
        this.renderer = renderer;
        this.mime = m;
    }

    /**
     * @return Returns the renderer.
     */
    public AbstractRenderer getRenderer() {
        return renderer;
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

    /**
     * Wrap the render context to allow easier access to its values.
     *
     * @param context the renderer context
     * @return the generic renderer context wrapper
     */
    public static RendererContextWrapper wrapRendererContext(RendererContext context) {
        RendererContextWrapper wrapper = new RendererContextWrapper(context);
        return wrapper;
    }

    /**
     * Base class for a wrapper around RendererContext to access its properties in a type-safe,
     * renderer-specific way.
     */
    public static class RendererContextWrapper {

        /** The wrapped RendererContext */
        protected RendererContext context;

        /**
         * Main constructor
         * @param context the RendererContent instance
         */
        public RendererContextWrapper(RendererContext context) {
            this.context = context;
        }

        /** @return the user agent */
        public FOUserAgent getUserAgent() {
            return context.getUserAgent();
        }

        /** @return the currentXPosition */
        public int getCurrentXPosition() {
            return ((Integer)context.getProperty(RendererContextConstants.XPOS)).intValue();
        }

        /** @return the currentYPosition */
        public int getCurrentYPosition() {
            return ((Integer)context.getProperty(RendererContextConstants.YPOS)).intValue();
        }

        /** @return the width of the image */
        public int getWidth() {
            return ((Integer)context.getProperty(RendererContextConstants.WIDTH)).intValue();
        }

        /** @return the height of the image */
        public int getHeight() {
            return ((Integer)context.getProperty(RendererContextConstants.HEIGHT)).intValue();
        }

        /** @return the foreign attributes */
        public Map getForeignAttributes() {
            return (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
        }

        /** {@inheritDoc} */
        public String toString() {
            return "RendererContextWrapper{"
                + "userAgent=" + getUserAgent()
                + "x=" + getCurrentXPosition()
                + "y=" + getCurrentYPosition()
                + "width=" + getWidth()
                + "height=" + getHeight()
                + "foreignAttributes=" + getForeignAttributes()
            + "}";

        }
    }
}

