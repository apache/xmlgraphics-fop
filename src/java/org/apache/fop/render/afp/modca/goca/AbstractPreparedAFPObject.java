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

package org.apache.fop.render.afp.modca.goca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.modca.AbstractAFPObject;
import org.apache.fop.render.afp.modca.PreparedAFPObject;

/**
 * A base class that carries out early preparation of structured field data
 * for the AFP object (so the data length can be pre-calculated)
 */
public abstract class AbstractPreparedAFPObject extends AbstractAFPObject
implements PreparedAFPObject {

    /** structured field data to be written */
    protected byte[] data = null;
        
    /**
     * {@inheritDoc}
     */
    public void writeDataStream(OutputStream os) throws IOException {
        if (this.data != null) {
            os.write(this.data);
        }
    }

    /**
     * @return the data length of this prepared AFP object
     */
    public int getDataLength() {
        if (this.data != null) {
            return this.data.length;
        }
        return 0;
    }    
}