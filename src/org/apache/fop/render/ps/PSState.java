/*
 * $Id$
 * Copyright (C) 2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.Serializable;
import java.awt.geom.AffineTransform;

/**
 * This class holds the current state of the PostScript interpreter.
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public class PSState implements Serializable, Cloneable {

    private AffineTransform transform = new AffineTransform();
 
 
 
    /**
     * Returns the transform.
     * @return the current transformation matrix
     */
    public AffineTransform getTransform() {
        return this.transform;
    }

    /**
     * Concats the given transformation matrix with the current one.
     * @param transform The new transformation matrix
     */
    public void concatMatrix(AffineTransform transform) {
        this.transform.concatenate(transform);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) { 
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
    
}
