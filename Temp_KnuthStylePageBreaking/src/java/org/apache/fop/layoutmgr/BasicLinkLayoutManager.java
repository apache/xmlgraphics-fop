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

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.Trait;
import org.apache.fop.area.LinkResolver;
import org.apache.fop.area.PageViewport;

/**
 * LayoutManager for the fo:basic-link formatting object
 */
public class BasicLinkLayoutManager extends InlineLayoutManager {
    private BasicLink fobj;
    
    /**
     * Create an fo:basic-link layout manager.
     *
     * @param node the formatting object that creates the area
     */
    public BasicLinkLayoutManager(BasicLink node) {
        super(node);
        fobj = node;
    }

    protected InlineParent createArea() {
        InlineParent area = super.createArea();
        setupBasicLinkArea(parentLM, area);
        return area;
    }
    
    private void setupBasicLinkArea(LayoutManager parentLM,
                                      InlineParent area) {
         if (fobj.getExternalDestination() != null) {
             area.addTrait(Trait.EXTERNAL_LINK, fobj.getExternalDestination());
         } else {
             String idref = fobj.getInternalDestination();
             PageViewport page = getPSLM().getFirstPVWithID(idref);
             if (page != null) {
                 area.addTrait(Trait.INTERNAL_LINK, page.getKey());
             } else {
                 LinkResolver res = new LinkResolver(idref, area);
                 getPSLM().addUnresolvedArea(idref, res);
             }
         }
     }
}

