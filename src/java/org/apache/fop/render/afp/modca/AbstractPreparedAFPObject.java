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

/* $Id: $ */

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A base class that carries out early preparation of structured field data
 * for the AFP object (so the data length can be pre-calculated)
 */
public abstract class AbstractPreparedAFPObject extends AbstractNamedAFPObject
implements PreparedAFPObject {

    /** structured field data to be written */
    protected byte[] data = null;

    /**
     * Default constructor
     */
    public AbstractPreparedAFPObject() {
    }

    /**
     * Named constructor
     * @param name the name of this AFP object
     */
    public AbstractPreparedAFPObject(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os); // write triplets
        if (this.data != null) {
            os.write(this.data);
        }
    }

    /**
     * @return the start data length of this structured field
     */
    protected int getStartDataLength() {
        return 0;
    }
    
    /**
     * @return the data length of the structured field data of this AFP object
     */
    public int getDataLength() {
        if (this.data != null) {
            return this.data.length;
        }
        return 0;
    }
    
    /**
     * @return the structured field length
     */
    protected int getLength() {
        return getStartDataLength() + getTripletDataLength() + getDataLength();
    }
    
    /**
     * Sets the data
     * @param data the data
     */
    protected void setData(byte[] data) {
        this.data = data;
    }
}