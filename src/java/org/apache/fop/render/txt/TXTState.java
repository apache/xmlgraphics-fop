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

package org.apache.fop.render.txt;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.fop.area.CTM;

/**
 * This keeps information about the current state when writing to txt, i.e. 
 * manages coordinate transformation matrices for getting absolute coordinates.
 */
public class TXTState {

    /** Keeps all coordinate transformation matrices during rendering. */
    private LinkedList stackCTM = new LinkedList();

    /**
     * Current result coordinate transformation matrix. It's product of 
     * all matrices in order, saved in <code>stackCTM</code>.
     */
    private CTM resultCTM = new CTM();

    /**
     * Constructs a newly allocated <code>TXTState</code> object.
     */
    public TXTState() {
    }

    /**
     * Updates result coordinate transformation matrix 
     * (i.e. <code>resultCTM</code>), multipliing it by given matrix.
     * 
     * @param ctm CTM
     */
    private void updateResultCTM(CTM ctm) {
        resultCTM = resultCTM.multiply(ctm);
    }

    /**
     * Recalculate current result coordinate transformation matrix.
     */
    private void calcResultCTM() {
        resultCTM = new CTM();
        for (Iterator i = stackCTM.iterator(); i.hasNext();) {
            updateResultCTM((CTM) i.next());
        }
    }

    /**
     * Push the current coordinate transformation matrix onto the stack and 
     * reevaluate <code>resultCTM</code>.
     * 
     * @param ctm  instance of CTM
     */
    public void push(CTM ctm) {
        stackCTM.addLast(ctm);
        updateResultCTM(ctm);
    }

    /**
     * Pop the coordinate transformation matrix from the stack and reevaluate
     * <code>resultCTM</code>.
     */
    public void pop() {
        stackCTM.removeLast();
        calcResultCTM();
    }
    
    /**
     * Modifies coordinate transformation matrix in such a way, so 
     * x-shift and y-shift will be transformed in text positions.
     * 
     * @param ctm CTM to modify
     * @return instance of CTM
     */
    public CTM refineCTM(CTM ctm) {
        double[] da = ctm.toArray();
        // refine x-shift
        da[4] = Helper.roundPosition((int) da[4], TXTRenderer.CHAR_WIDTH);
        // refine y-shift
        da[5] = Helper.roundPosition((int) da[5], TXTRenderer.CHAR_HEIGHT);
        
        return new CTM(da[0], da[1], da[2], da[3], da[4], da[5]);
    }

    /**
     * Transforms <code>point</code> using <code>ctm</code>.
     * 
     * @param p Point
     * @param ctm CTM
     * @return transformed Point
     */
    public Point transformPoint(Point p, CTM ctm) {
        Rectangle2D r = new Rectangle2D.Double(p.x, p.y, 0, 0);
        CTM nctm = refineCTM(ctm);
        r = nctm.transform(r);
        return new Point((int) r.getX(), (int) r.getY());
    }

    /**
     * Transforms point (x, y) using <code>resultCTM</code>.
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @return transformed Point
     */
    public Point transformPoint(int x, int y) {
        return transformPoint(new Point(x, y), resultCTM);
    }

    /**
     * @return current result coordinate transformation matrix
     */
    public CTM getResultCTM() {
        return resultCTM;
    }
}
