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

package org.apache.fop.svg.text;

import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.util.List;

import org.apache.batik.bridge.GlyphLayout;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.fonts.Font;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;

public class ComplexGlyphLayout extends GlyphLayout {

    public ComplexGlyphLayout(AttributedCharacterIterator aci,
        int [] charMap, Point2D offset, FontRenderContext frc) {
        super(aci, charMap, offset, frc);
    }

    @Override
    protected int getAciIndex(int aciIndex, int loopIndex) {
        if (gv instanceof FOPGVTGlyphVector) {
            List associations = ((FOPGVTGlyphVector) gv).getAssociations();
            // this method is called at the end of the cycle, therefore we still have the index of the current cycle
            // since we are trying to determine the aci index for the next interation, we need to add 1 to the index
            // the parent method does that automatically when it tries to get the character count
            int nextIndex = loopIndex + 1;
            if (nextIndex < associations.size() && associations.get(nextIndex) instanceof CharAssociation) {
                CharAssociation association = (CharAssociation) associations.get(nextIndex);
                return association.getStart();
            }
        }

        //will only be used on the last iteration. the loop will stop after this and the value will not be used
        return super.getAciIndex(aciIndex, loopIndex);
    }

    public static final boolean mayRequireComplexLayout(AttributedCharacterIterator aci) {
        boolean rv = false;
        GVTAttributedCharacterIterator.TextAttribute attrFont = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
        int indexSave = aci.getIndex();
        aci.first();
        do {
            GVTFont gvtFont = (GVTFont) aci.getAttribute(attrFont);
            if (gvtFont == null) {
                continue;
            } else {
                if (gvtFont instanceof FOPGVTFont) {
                    Font f = ((FOPGVTFont) gvtFont).getFont();
                    if (f.performsSubstitution() || f.performsPositioning()) {
                        rv = true;
                        break;
                    }
                }
                aci.setIndex(aci.getRunLimit(attrFont));
            }
        } while (aci.next() != AttributedCharacterIterator.DONE);
        aci.setIndex(indexSave);
        return rv;
    }

}
