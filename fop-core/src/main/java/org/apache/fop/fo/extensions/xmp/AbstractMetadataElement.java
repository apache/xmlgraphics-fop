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

package org.apache.fop.fo.extensions.xmp;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;

/**
 * Abstract base class for the XMP and RDF root nodes.
 */
public abstract class AbstractMetadataElement extends FONode implements ObjectBuiltListener {

    private XMPMetadata attachment;

    /**
     * Main constructor.
     * @param parent the parent formatting object
     */
    public AbstractMetadataElement(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public ContentHandlerFactory getContentHandlerFactory() {
        return new XMPContentHandlerFactory();
    }

    /** {@inheritDoc} */
    public ExtensionAttachment getExtensionAttachment() {
        if (parent instanceof FObj) {
            if (attachment == null) {
                attachment = new XMPMetadata();
            }
            return attachment;
        } else {
            return super.getExtensionAttachment();
        }
    }

    /** {@inheritDoc} */
    public void notifyObjectBuilt(Object obj) {
        attachment.setMetadata((Metadata)obj);
    }


}
