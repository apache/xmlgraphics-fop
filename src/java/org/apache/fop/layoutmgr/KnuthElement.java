/*
 * Copyright 2004 The Apache Software Foundation.
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

public abstract class KnuthElement {

    public static final int KNUTH_BOX = 0;
    public static final int KNUTH_GLUE = 1;
    public static final int KNUTH_PENALTY = 2;

    public static final int INFINITE = 1000;

    private int type;
    private int width;
    private Position position;
    private boolean bIsAuxiliary;

    protected KnuthElement(int t, int w, Position pos, boolean bAux) {
        type = t;
        width = w;
        position = pos;
        bIsAuxiliary = bAux;
    }

    public boolean isBox() {
        return (type == KNUTH_BOX);
    }

    public boolean isGlue() {
        return (type == KNUTH_GLUE);
    }

    public boolean isPenalty() {
        return (type == KNUTH_PENALTY);
    }

    public boolean isAuxiliary() {
        return bIsAuxiliary;
    }

    public int getW() {
        return width;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position pos) {
        position = pos;
    }

    public LayoutManager getLayoutManager() {
        if (position != null) {
            return position.getLM();
        } else {
            return null;
        }
    }
}