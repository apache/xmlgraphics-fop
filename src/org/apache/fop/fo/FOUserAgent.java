/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.w3c.dom.*;

/**
 * The User Agent for fo.
 * This user agent is used by the processing to obtain user configurable
 * options.
 * 
 * Renderer specific extensions (that do not produce normal areas on
 * the output) will be done like so:
 * The extension will create an area, custom if necessary
 * this area will be added to the user agent with a key
 * the renderer will know keys for particular extensions
 * eg. bookmarks will be held in a special hierarchical area representing
 * the title and bookmark structure
 * These areas may contain resolveable areas that will be processed
 * with other resolveable areas
 */
public interface FOUserAgent {
public void renderXML(RendererContext ctx, Document doc, String namespace);

}

class RendererContext {
String getMimeType() {
return null;
}
}

