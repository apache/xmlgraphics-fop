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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.LinkResolver;

/**
 * An inline area produced by an fo:basic-link element. This class implements a different
 * behavior to what is prescribed by the XSL-FO 1.1 Recommendation. With the standard
 * behavior, there is no easy way to make a link cover e.g. a whole image.
 *
 * <p>See following bug report at W3C's:
 * http://www.w3.org/Bugs/Public/show_bug.cgi?id=11672</p>
 */
public class BasicLinkArea extends InlineParent {

    private static final long serialVersionUID = 5183753430412208151L;

    private LinkResolver resolver;

    @Override
    public void setParentArea(Area parentArea) {
        super.setParentArea(parentArea);
        /*
         * Perform necessary modifications to make this area encompass all of its children
         * elements, so as to have a useful active area. We assume that this method is
         * called after all of the children areas have been added to this area.
         */
        /* Make this area start at its beforest child. */
        setBlockProgressionOffset(getBlockProgressionOffset() + minChildOffset);
        /* Update children offsets accordingly. */
        for (InlineArea inline : inlines) {
            inline.setBlockProgressionOffset(inline.getBlockProgressionOffset() - minChildOffset);
        }
        setBPD(getVirtualBPD());
    }

    /**
     * Establish (or remove) back-pointer to link resolver.
     * @param resolver the link resolver that will resolve this basic link or null
     */
    public void setResolver(LinkResolver resolver) {
        assert (resolver == null) || (this.resolver == null);
        this.resolver = resolver;
    }

    /**
     * Obtain back-pointer to link resolver.
     * @return resolver the link resolver that will resolve this basic link or null
     */
    public LinkResolver getResolver() {
        return this.resolver;
    }
}
