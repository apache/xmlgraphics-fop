/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

public class Word extends InlineArea {
    /**
     * The word for this word area.
     */
    protected String word;
    private int iWSadjust = 0;

    /**
     * Create a word area.
     */
    public Word() {
    }

    /**
     * Render the word to the renderer.
     *
     * @param renderer the renderer to render this word
     */
    public void render(Renderer renderer) {
        renderer.renderWord(this);
    }

    /**
     * Set the word.
     *
     * @param w the word string
     */
    public void setWord(String w) {
        word = w;
    }

    /**
     * Get the word string.
     *
     * @return the word string
     */
    public String getWord() {
        return word;
    }

    /**
     * Get word space adjust.
     *
     * @return the word space adjustment
     */
    public int getWSadjust() {
        return iWSadjust;
    }

    /**
     * Set word space adjust.
     *
     * @param iWSadjust the word space adjustment
     */
    public void setWSadjust(int iWSadjust) {
        this.iWSadjust = iWSadjust;
    }
}

