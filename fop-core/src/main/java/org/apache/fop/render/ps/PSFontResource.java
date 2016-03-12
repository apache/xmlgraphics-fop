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

package org.apache.fop.render.ps;

import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

/**
 * A DSC resource corresponding to a font. This class handles the possible other resources
 * that a font may depend on. For example, a CID-keyed font depends on a CIDFont resource, a
 * CMap resource, and the ProcSet CIDInit resource.
 */
abstract class PSFontResource {

    static PSFontResource createFontResource(final PSResource fontResource) {
        return new PSFontResource() {

            String getName() {
                return fontResource.getName();
            }

            void notifyResourceUsageOnPage(ResourceTracker resourceTracker) {
                resourceTracker.notifyResourceUsageOnPage(fontResource);
            }
        };
    }

    static PSFontResource createFontResource(final PSResource fontResource,
            final PSResource procsetCIDInitResource, final PSResource cmapResource,
            final PSResource cidFontResource) {
        return new PSFontResource() {

            String getName() {
                return fontResource.getName();
            }

            void notifyResourceUsageOnPage(ResourceTracker resourceTracker) {
                resourceTracker.notifyResourceUsageOnPage(fontResource);
                resourceTracker.notifyResourceUsageOnPage(procsetCIDInitResource);
                resourceTracker.notifyResourceUsageOnPage(cmapResource);
                resourceTracker.notifyResourceUsageOnPage(cidFontResource);
            }
        };
    }

    /**
     * Returns the name of the font resource.
     *
     * @return the name of the font
     */
    abstract String getName();

    /**
     * Notifies the given resource tracker of all the resources needed by this font.
     *
     * @param resourceTracker
     */
    abstract void notifyResourceUsageOnPage(ResourceTracker resourceTracker);

}
