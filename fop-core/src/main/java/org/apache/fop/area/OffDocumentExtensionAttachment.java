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

package org.apache.fop.area;

import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This class wraps ExtensionAttachments which cannot be transported inside the area tree but
 * need to be handled in the AreaTreeHandler. These attachments are schedules for processing
 * before the first page-sequence, i.e. at the start of the document.
 */
public class OffDocumentExtensionAttachment implements OffDocumentItem {

    private ExtensionAttachment attachment;

    /**
     * Main constructor
     * @param attachment the extension attachment to wrap.
     */
    public OffDocumentExtensionAttachment(ExtensionAttachment attachment) {
        this.attachment = attachment;
    }

    /** @return the extension attachment. */
    public ExtensionAttachment getAttachment() {
        return this.attachment;
    }

    /** {@inheritDoc} */
    public int getWhenToProcess() {
        return OffDocumentItem.IMMEDIATELY;
    }

    /** {@inheritDoc} */
    public String getName() {
        return attachment.getCategory();
    }

}
