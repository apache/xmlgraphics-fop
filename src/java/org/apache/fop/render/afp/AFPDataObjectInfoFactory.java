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

package org.apache.fop.render.afp;

import java.util.Iterator;
import java.util.Map;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

/**
 * AFP image configurator
 */
public class AFPDataObjectInfoFactory {
    private final Map dataObjectInfoFactoryMap = new java.util.HashMap();
    private final AFPState state;

    /**
     * Main constructor
     *
     * @param state the AFP state
     */
    public AFPDataObjectInfoFactory(AFPState state) {
        this.state = state;
        init();
    }

    /**
     * Initialises the configurators
     */
    private void init() {
        dataObjectInfoFactoryMap.put(
                ImageRendered.class, new AFPImageRenderedFactory(state));
        dataObjectInfoFactoryMap.put(
                ImageRawCCITTFax.class, new AFPRawCCITTFaxFactory(state));
        dataObjectInfoFactoryMap.put(
                ImageRawStream.class, new AFPImageRawStreamFactory(state));
    };

    /**
     * Returns the configurator for a given image
     *
     * @param img the image
     * @return the image configurator for the image
     */
    public AFPAbstractImageFactory getFactory(Image img) {
        Class clazz = img.getClass();
        AFPAbstractImageFactory configurator
            = (AFPAbstractImageFactory)dataObjectInfoFactoryMap.get(clazz);
        // not directly matched so try to map ancestor
        if (configurator == null) {
            Iterator it = dataObjectInfoFactoryMap.keySet().iterator();
            while (it.hasNext()) {
                Class imageClass = (Class)it.next();
                if (imageClass.isInstance(img)) {
                    return (AFPAbstractImageFactory)dataObjectInfoFactoryMap.get(imageClass);
                }
            }
        }
        return configurator;
    }
}
