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

import org.apache.fop.fo.flow.Character;
import org.apache.fop.area.inline.InlineArea;

/**
 * LayoutManager for the fo:character formatting object
 */
public class CharacterLayoutManager extends LeafNodeLayoutManager {

    /**
     * Constructor
     *
     * @param node the fo:character formatting object
     * @todo better null checking of node
     */
    public CharacterLayoutManager(Character node) {
        super(node);
        InlineArea inline = getCharacterInlineArea(node);
        setCurrentArea(inline);
    }

    private InlineArea getCharacterInlineArea(Character node) {
        String str = node.getProperty(Character.PR_CHARACTER).getString();
        org.apache.fop.area.inline.Character ch =
            new org.apache.fop.area.inline.Character(str.charAt(0));
        return ch;
    }
}

