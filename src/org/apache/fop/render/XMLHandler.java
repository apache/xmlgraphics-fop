/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render;

import org.w3c.dom.Document;

/**
 */
public interface XMLHandler {

    /**
     * Handle an external xml document inside a Foreign Object Area
     * This may throw an exception if for some reason it cannot be handled.
     * The caller is expected to deal with this exception.
     */
    public void handleXML(RendererContext context, Document doc,
                          String ns) throws Exception;
}

