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

package org.apache.fop.events.model;

import java.io.InputStream;
import java.util.MissingResourceException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import org.apache.fop.events.DefaultEventBroadcaster;

/**
 * This interface is used to instantiate (load, parse) event models.
 */
public abstract class AbstractEventModelFactory implements EventModelFactory {

    /**
     * Loads an event model and returns its instance.
     * @param resourceBaseClass base class to use for loading resources
     * @param resourceName the resource name pointing to the event model to be loaded
     * @return the newly loaded event model.
     */
    public EventModel loadModel(Class resourceBaseClass, String resourceName) {
        InputStream in = resourceBaseClass.getResourceAsStream(resourceName);
        if (in == null) {
            throw new MissingResourceException(
                    "File " + resourceName + " not found",
                    DefaultEventBroadcaster.class.getName(), ""); 
        }
        try {
            return EventModelParser.parse(new StreamSource(in));
        } catch (TransformerException e) {
            throw new MissingResourceException(
                    "Error reading " + resourceName + ": " + e.getMessage(),
                    DefaultEventBroadcaster.class.getName(), ""); 
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    
}
