/*
 * FObject.java
 * Created: Sun Jan 27 01:35:24 2002
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.for.FObjects;

/**
 * Base class for all Flow Objects
 */
public class FObject {

    private int foIndex;

    public FObject(int foIndex) {
        this.foIndex = foIndex;
    }

    public FObject(String foName) {
        foIndex = FObjects.getFoIndex(foName);
    }

}// FObject
