/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

/**
 * This interface is used for special FilteredOutputStream classes that won't
 * be closed (since this causes the target OutputStream to be closed, too) but
 * where flush() is not enough, for example because a final marker has to be
 * written to the target stream.
 *
 * @author    <a href="mailto:jeremias.maerki@outline.ch">Jeremias Maerki</a>
 * @version   $Id$
 */
public interface Finalizable {

    /**
     * This method can be called instead of close() on a subclass of
     * FilteredOutputStream when a final marker has to be written to the target
     * stream, but close() cannot be called.
     *
     * @exception java.io.IOException  In case of an IO problem
     */
    public void finalizeStream()
        throws java.io.IOException;

}
