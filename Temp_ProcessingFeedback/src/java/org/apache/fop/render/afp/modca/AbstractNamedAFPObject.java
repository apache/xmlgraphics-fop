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

package org.apache.fop.render.afp.modca;
import java.io.UnsupportedEncodingException;

/**
 * This is the base class for all named data stream objects.
 * A named data stream object has an 8 byte EBCIDIC name.
 */
public abstract class AbstractNamedAFPObject extends AbstractAFPObject {
    
    /**
     * The actual name of the object
     */
    protected String name = null;
    
    /**
     * The name of the object in EBCIDIC bytes
     */
    protected byte[] nameBytes;
    
    /**
     * Constructor for the ActiveEnvironmentGroup, this takes a
     * name parameter which should be 8 characters long.
     * @param name the object name
     */
    public AbstractNamedAFPObject(String name) {
        
        this.name = name;
        if (name.length() < 8) {
            name = (name + "       ").substring(0, 8);
        } else if (name.length() > 8) {
            log.warn("Constructor:: name truncated to 8 chars" + name);
            name = name.substring(0, 8);
        }
        
        try {
            
            nameBytes = name.getBytes(AFPConstants.EBCIDIC_ENCODING);
            
        } catch (UnsupportedEncodingException usee) {
            
            nameBytes = name.getBytes();
            log.warn(
                "Constructor:: UnsupportedEncodingException translating the name "
                + name);
            
        }
        
    }
    
}
