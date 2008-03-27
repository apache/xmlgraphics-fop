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

/* $Id: $ */

package org.apache.fop.render.afp.goca;

import java.io.UnsupportedEncodingException;

import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AbstractPreparedAFPObject;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * A GOCA graphics string
 */
public class GraphicsString extends AbstractPreparedAFPObject {
    /** Up to 255 bytes of character data */
    private static final int MAX_STR_LEN = 255;

    /** drawn from the current position */
    private boolean fromCurrentPosition = false;
    
    /** the string to draw */
    private String str = null;
    
    /** x coordinate */
    private int x;
    
    /** y coordinate */
    private int y;

    /**
     * @param str the character string
     */
    public GraphicsString(String str) {
        this.str  = str;
        fromCurrentPosition = true;
        prepareData();
    }

    /**
     * @param str the character string
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public GraphicsString(String str, int x, int y) {
        this.str = str;
        this.x = x;
        this.y = y;
        prepareData();
    }
    
    /**
     * {@inheritDoc} 
     */
    protected void prepareData() {
        int maxStrLen = MAX_STR_LEN - (fromCurrentPosition ? 0 : 4);
        if (str.length() > maxStrLen) {
            str = str.substring(0, maxStrLen);
            log.warn("truncated character string, longer than " + maxStrLen + " chars");
        }
        byte[] strData = null;
        try {
            strData = str.getBytes(AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            GraphicsObject.log.error("unsupported encoding: " + ex.getMessage());
        }
        int len = strData.length;
        if (fromCurrentPosition) {
            data = new byte[len + 2];
            data[0] = (byte)0x83;
            data[1] = (byte)len;
            System.arraycopy(strData, 0, data, 2, strData.length);
        } else {
            len += 4; // x/y coordinates
            byte[] osx = BinaryUtils.convert(x, 2);   
            byte[] osy = BinaryUtils.convert(y, 2);
            data = new byte[len + 2];
            data[0] = (byte)0xC3;
            data[1] = (byte)len;
            data[2] = osx[0];
            data[3] = osx[1];
            data[4] = osy[0];
            data[5] = osy[1];
            System.arraycopy(strData, 0, data, 6, strData.length);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        String string = "GraphicsString(str='" + str + "'";
        if (!fromCurrentPosition) {
            string += ",x=" + x + ",y=" + y;
        }
        string += ")";
        return string;
    }
}