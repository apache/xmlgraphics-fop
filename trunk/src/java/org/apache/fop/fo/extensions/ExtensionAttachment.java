/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.fo.extensions;

/**
 * This interface is implemented by objects that are returned by special extension element
 * through the FONode.getExtensionAttachment() method. Such objects are carried in the FO tree
 * and made available to the layout managers that support processing extension attachments or
 * support passing them on to the area tree where they can be picked up by renderers.
 * <p>
 * NOTE: Classes which implement this interface need to be Serializable!
 */
public interface ExtensionAttachment {

    /**
     * This method returns a category URI that allows a processor (layout manager or renderer)
     * to determine if it supports this object.
     * @return the category URI
     */
    String getCategory();
    
}
