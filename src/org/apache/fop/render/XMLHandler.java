/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

import org.w3c.dom.Document;

/**
 * This interface is implemented by classes that can handle a certain type
 * of foreign objects.
 */
public interface XMLHandler {

    /**
     * Handle an external xml document inside a Foreign Object Area. <p>
     *
     * This may throw an exception if for some reason it cannot be handled. The
     * caller is expected to deal with this exception.</p>
     *
     * @param context        The RendererContext (contains the user agent)
     * @param doc            A DOM containing the foreign object to be
     *      processed
     * @param ns             The Namespace of the foreign object
     * @exception Exception  If an error occurs during processing.
     */
    void handleXML(RendererContext context, Document doc, String ns)
        throws Exception;

}

