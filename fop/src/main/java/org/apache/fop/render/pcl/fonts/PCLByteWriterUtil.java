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

package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PCLByteWriterUtil {

    public byte[] padBytes(byte[] in, int length) {
        return padBytes(in, length, 0);
    }

    public byte[] padBytes(byte[] in, int length, int value) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            if (i < in.length) {
                out[i] = in[i];
            } else {
                out[i] = (byte) value;
            }
        }
        return out;
    }

    public byte[] signedInt(int s) {
        byte b1 = (byte) (s >> 8);
        byte b2 = (byte) s;
        return new byte[]{b1, b2};
    }

    public byte signedByte(int s) {
        return (byte) s;
    }

    public byte[] unsignedLongInt(int s) {
        return unsignedLongInt((long) s);
    }

    public byte[] unsignedLongInt(long s) {
        byte b1 = (byte) ((s >> 24) & 0xff);
        byte b2 = (byte) ((s >> 16) & 0xff);
        byte b3 = (byte) ((s >> 8) & 0xff);
        byte b4 = (byte) (s & 0xff);
        return new byte[]{b1, b2, b3, b4};
    }

    public byte[] unsignedInt(int s) {
        byte b1 = (byte) ((s >> 8) & 0xff);
        byte b2 = (byte) (s & 0xff);
        return new byte[]{b1, b2};
    }

    public int unsignedByte(int b) {
        return (byte) b & 0xFF;
    }

    public int maxPower2(int value) {
        int test = 2;
        while (test < value) {
            test *= 2;
        }
        return test;
    }

    public int log(int x, int base) {
        return (int) (Math.log(x) / Math.log(base));
    }

    public byte[] toByteArray(int[] s) {
        byte[] values = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            values[i] = (byte) s[i];
        }
        return values;
    }

    public byte[] insertIntoArray(int index, byte[] insertTo, byte[] data) throws IOException {
        byte[] preBytes = Arrays.copyOf(insertTo, index);
        byte[] postBytes = Arrays.copyOfRange(insertTo, index, insertTo.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(preBytes);
        baos.write(data);
        baos.write(postBytes);
        return baos.toByteArray();
    }

    public byte[] updateDataAtLocation(byte[] data, byte[] update, int offset) {
        int count = 0;
        for (int i = offset; i < offset + update.length; i++) {
            data[i] = update[count++];
        }
        return data;
    }

    /**
     * Writes a PCL escape command to the output stream.
     * @param cmd the command (without the ESCAPE character)
     * @throws IOException In case of an I/O error
     */
    public byte[] writeCommand(String cmd) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(27); // ESC
        baos.write(cmd.getBytes("US-ASCII"));
        return baos.toByteArray();
    }
}
