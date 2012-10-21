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

package org.apache.fop.render.ps.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * Comment before element
 */
public class PSCommentBeforeElement extends AbstractPSCommentElement {

    protected static final String ELEMENT = "ps-comment-before";

    /**
     * Main constructor
     * @param parent parent node
     */
    public PSCommentBeforeElement(FONode parent) {
        super(parent);
    }

    /**
     * @return local name
     * @see org.apache.fop.fo.FONode#getLocalName()
     */
    public String getLocalName() {
        return ELEMENT;
    }

    /**
     * @return instance of its extension attachment object
     */
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new PSCommentBefore();
    }    
}
