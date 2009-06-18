/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.render.Renderer;

import java.io.IOException;
import java.awt.geom.Rectangle2D;

public class Viewport extends InlineArea {
    // contents could be container, foreign object or image
    Area content;
    // an inline-level viewport area for graphic and instream foreign object
    boolean clip = false;
    // position relative to this area
    Rectangle2D contentPosition;

    public Viewport(Area child) {
        content = child;
    }

    public Area getContent() {
        return content;
    }

    public void render(Renderer renderer) {
        renderer.renderViewport(this);
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
        out.writeObject(content);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            contentPosition = new Rectangle2D.Float(in.readFloat(),
                                                    in.readFloat(), in.readFloat(), in.readFloat());
        }
        clip = in.readBoolean();
        content = (Area) in.readObject();
    }

}
