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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import java.io.IOException;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * Inline viewport area.
 * This is an inline-level viewport area for inline container,
 * external graphic and instream foreign object. This viewport
 * holds the area and positions it.
 */
public class Viewport extends InlineArea {
    // contents could be container, foreign object or image
    private Area content;
    // clipping for the viewport
    private boolean clip = false;
    // position of the cild area relative to this area
    private Rectangle2D contentPosition;

    /**
     * Create a new viewport area with the content area.
     *
     * @param child the child content area of this viewport
     */
    public Viewport(Area child) {
        content = child;
    }

    /**
     * Set the clip of this viewport.
     *
     * @param c true if this viewport should clip
     */
    public void setClip(boolean c) {
        clip = c;
    }

    /**
     * Get the clip of this viewport.
     *
     * @return true if this viewport should clip
     */
    public boolean getClip() {
        return clip;
    }

    /**
     * Set the position and size of the content of this viewport.
     *
     * @param cp the position and size to place the content
     */
    public void setContentPosition(Rectangle2D cp) {
        contentPosition = cp;
    }

    /**
     * Get the position and size of the content of this viewport.
     *
     * @return the position and size to place the content
     */
    public Rectangle2D getContentPosition() {
        return contentPosition;
    }

    /**
     * Get the content area for this viewport.
     *
     * @return the content area
     */
    public Area getContent() {
        return content;
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeBoolean(contentPosition != null);
        if (contentPosition != null) {
            out.writeFloat((float) contentPosition.getX());
            out.writeFloat((float) contentPosition.getY());
            out.writeFloat((float) contentPosition.getWidth());
            out.writeFloat((float) contentPosition.getHeight());
        }
        out.writeBoolean(clip);
        out.writeObject(props);
        out.writeObject(content);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            contentPosition = new Rectangle2D.Float(in.readFloat(),
                                                    in.readFloat(),
                                                    in.readFloat(),
                                                    in.readFloat());
        }
        clip = in.readBoolean();
        props = (HashMap) in.readObject();
        content = (Area) in.readObject();
    }

}
