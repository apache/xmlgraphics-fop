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

package org.apache.fop.render.intermediate.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Action class which references another action.
 */
public class ReferencedAction extends AbstractAction
            implements DocumentNavigationExtensionConstants {

    /**
     * Creates a new instance.
     * @param id the ID
     */
    public ReferencedAction(String id) {
        if (id == null || id.length() == 0) {
            throw new NullPointerException("ID must not be set");
        }
        setID(id);
    }

    /** {@inheritDoc} */
    public boolean isReference() {
        return true;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        //nop, handled by referencer
    }

    /** {@inheritDoc} */
    public boolean isSame(AbstractAction other) {
        throw new UnsupportedOperationException(
                "isSame() may not be called on " + getClass().getName());
    }

}
