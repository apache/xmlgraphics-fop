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

package org.apache.fop.fo.expr;

import org.apache.fop.apps.FOPException;

/**
 * Class for managing exceptions that are raised in Property processing.
 */
public class PropertyException extends FOPException {
    private String propertyName;

    /**
     * Constructor
     * @param detail string containing the detail message
     */
    public PropertyException(String detail) {
        super(detail);
    }

    /**
     */
    public void setPropertyInfo(PropertyInfo propInfo) {
        setLocator(propInfo.getFO().locator);
        propertyName = propInfo.getPropertyMaker().getName();
    }

    /**
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getMessage() {
        if (propertyName != null) {
            return super.getMessage() + "; property:'" + propertyName + "'";
        } else {
            return super.getMessage();
        }
    }
}
