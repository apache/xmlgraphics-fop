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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * GOCA Graphics Data Descriptor
 */
public class GraphicsDataDescriptor extends AbstractAFPObject {

    private int xlwind;
    private int xrwind;
    private int ybwind;
    private int ytwind;
    private int xresol;
    private int yresol;
    
    /**
     * Main constructor
     * @param xresol the x resolution of the graphics window
     * @param yresol the y resolution of the graphics window
     * @param xlwind the left edge of the graphics window 
     * @param xrwind the right edge of the graphics window
     * @param ybwind the top edge of the graphics window
     * @param ytwind the bottom edge of the graphics window
     */
    protected GraphicsDataDescriptor(int xresol, int yresol,
            int xlwind, int xrwind, int ybwind, int ytwind) {
        this.xresol = xresol;
        this.yresol = yresol;
        this.xlwind = xlwind;
        this.xrwind = xrwind;
        this.ybwind = ybwind;
        this.ytwind = ytwind;
    }    

    private static final int ABS = 2;
    private static final int IMGRES = 8;

    /**
     * {@inheritDoc}
     */
    public void write(OutputStream os) throws IOException {
        byte[] xreswind = BinaryUtils.convert(xresol * 10, 2);
        byte[] yreswind = BinaryUtils.convert(yresol * 10, 2);
        byte[] xlcoord = BinaryUtils.convert(xlwind, 2);
        byte[] xrcoord = BinaryUtils.convert(xrwind, 2);
        byte[] xbcoord = BinaryUtils.convert(ybwind, 2);
        byte[] ytcoord = BinaryUtils.convert(ytwind, 2);
        byte[] imxyres = xreswind;      
        byte[] data = new byte[] {
            0x5A,
            0x00,
            0x25,
            (byte) 0xD3,
            (byte) 0xA6,
            (byte) 0xBB,
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            
            // Drawing order subset
            (byte) 0xF7,
            7, // LENGTH
            (byte) 0xB0, // drawing order subset
            0x00, // reserved (must be zero)
            0x00, // reserved (must be zero)
            0x02, // SUBLEV
            0x00, // VERSION 0
            0x01, // LENGTH (of following field)
            0x00,  // GEOM

            // Window specification
            (byte) 0xF6,
            18, // LENGTH
            (ABS + IMGRES), // FLAGS (ABS)
            0x00, // reserved (must be zero)
            0x00, // CFORMAT (coordinate format - 16bit high byte first signed)
            0x00, // UBASE (unit base - ten inches)
            xreswind[0], // XRESOL
            xreswind[1], 
            yreswind[0], // YRESOL
            yreswind[1], 
            imxyres[0], // IMXYRES (Number of image points per ten inches
            imxyres[1], //          in X and Y directions)
            xlcoord[0], // XLWIND
            xlcoord[1],
            xrcoord[0], // XRWIND
            xrcoord[1],
            xbcoord[0], // YBWIND
            xbcoord[1],
            ytcoord[0], // YTWIND
            ytcoord[1]
        };
        os.write(data);    
    }
}
