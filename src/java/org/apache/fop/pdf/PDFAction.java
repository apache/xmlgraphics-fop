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
 
package org.apache.fop.pdf;

/**
 * class representing an action object.
 */
public abstract class PDFAction extends PDFObject {


    /**
     * represent the action to call
     * this method should be implemented to return the action which gets
     * called by the Link Object.  This could be a reference to another object
     * or the specific destination of the link
     *
     * @return the action to place next to /A within a Link
     */
    public abstract String getAction();


}
