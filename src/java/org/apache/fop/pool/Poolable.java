/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.pool;

/**
 * This is a data class to encapsulate the data of an individual
 * poolable event. The current version, while defining accessor methods,
 * leaves the component data of the event as protected.
 */

public abstract class Poolable extends Sequenced {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";


    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     * @param sequence the sequence number of this object
     */
    public Poolable (int sequence) {
        super(sequence);
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared event.
     */
    public abstract Poolable clear();
       
    public String toString() {
        return "\nSeq " + id;
    }

}
