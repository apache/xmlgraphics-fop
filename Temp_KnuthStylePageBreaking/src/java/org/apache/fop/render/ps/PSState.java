/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.render.ps;

import java.io.Serializable;
import java.awt.geom.AffineTransform;

/**
 * This class holds the current state of the PostScript interpreter.
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: PSState.java,v 1.2 2003/03/07 09:46:30 jeremias Exp $
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
