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

import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;

/**
 * LayoutManager for the fo:external-graphic formatting object
 */
public class ExternalGraphicLayoutManager extends LeafNodeLayoutManager {

    ExternalGraphic graphic = null;

    /**
     * Constructor
     *
     * @param node the fo:external-graphic formatting object that creates the area
     */
    public ExternalGraphicLayoutManager(ExternalGraphic node) {
        super(node);

        graphic = node;
        InlineArea area = getExternalGraphicInlineArea();
        setCurrentArea(area);
        setAlignment(graphic.getProperty(PR_VERTICAL_ALIGN).getEnum());
        setLead(graphic.getViewHeight());
    }

     /**
      * Get the inline area for this external grpahic.
      * This creates the image area and puts it inside a viewport.
      *
      * @return the viewport containing the image area
      */
     public InlineArea getExternalGraphicInlineArea() {
         Image imArea = new Image(graphic.getURL());
         Viewport vp = new Viewport(imArea);
         vp.setWidth(graphic.getViewWidth());
         vp.setHeight(graphic.getViewHeight());
         vp.setClip(graphic.getClip());
         vp.setContentPosition(graphic.getPlacement());
         vp.setOffset(0);

         // Common Border, Padding, and Background Properties
         CommonBorderAndPadding bap = graphic.getPropertyManager().getBorderAndPadding();
         CommonBackground bProps = graphic.getPropertyManager().getBackgroundProps();
         TraitSetter.addBorders(vp, bap);
         TraitSetter.addBackground(vp, bProps);

         return vp;
     }
}

