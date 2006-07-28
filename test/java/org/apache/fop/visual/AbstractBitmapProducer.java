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

package org.apache.fop.visual;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for converters.
 */
public abstract class AbstractBitmapProducer implements BitmapProducer {

    /** Logger */
    protected static Log log = LogFactory.getLog(AbstractBitmapProducer.class);
    
    /**
     * Returns a new JAXP Transformer based on information in the ProducerContext.
     * @param context context information for the process
     * @return a new Transformer instance (identity or set up with a stylesheet)
     * @throws TransformerConfigurationException in case creating the Transformer fails.
     */
    protected Transformer getTransformer(ProducerContext context) 
                throws TransformerConfigurationException {
        if (context.getTemplates() != null) {
            return context.getTemplates().newTransformer();
        } else {
            return context.getTransformerFactory().newTransformer();
        }
    }
    
}
