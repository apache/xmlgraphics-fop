/*
 * $Id: Viewport.java,v 1.7 2003/03/05 16:45:42 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
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
