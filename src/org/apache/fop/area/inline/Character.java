/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

/**
 * Single character inline area.
 * This inline area holds a single characater.
 */
public class Character extends InlineArea {
    private char character;

    /**
     * Create a new characater inline area with the given character.
     *
     * @param ch the character for this inline area
     */
    public Character(char ch) {
        character = ch;
    }

    // character info: font, char spacing, colour, baseline

    /**
     * Render this inline area.
     *
     * @param renderer the renderer to render this character area
     */
    public void render(Renderer renderer) {
        renderer.renderCharacter(this);
    }

    /**
     * Get the character for this inline character area.
     *
     * @return the character
     */
    public char getChar() {
        return character;
    }

}
