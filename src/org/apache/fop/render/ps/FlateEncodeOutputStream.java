/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.IOException;

/**
 * This class applies a FlateEncode filter to the stream. It is basically the
 * normal DeflaterOutputStream except now also implementing the Finalizable
 * interface.
 *
 * @author <a href="mailto:jeremias.maerki@outline.ch">Jeremias Maerki</a>
 * @version $Id$
 */
public class FlateEncodeOutputStream extends java.util.zip.DeflaterOutputStream
            implements Finalizable {


    /** @see java.util.zip.DeflaterOutputStream **/
    public FlateEncodeOutputStream(OutputStream out) {
        super(out);
    }


    /** @see org.apache.fop.render.ps.Finalizable **/
    public void finalizeStream() throws IOException {
        finish();
        flush();
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        }
    }

}


