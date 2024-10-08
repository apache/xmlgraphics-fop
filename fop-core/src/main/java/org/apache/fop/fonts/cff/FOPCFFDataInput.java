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
package org.apache.fop.fonts.cff;

import java.io.IOException;

import org.apache.fontbox.cff.DataInputByteArray;

public class FOPCFFDataInput extends DataInputByteArray {
    private final byte[] inputBuffer;
    private int bufferPosition;

    public FOPCFFDataInput(byte[] buffer) {
        super(buffer);
        this.inputBuffer = buffer;
    }

    public boolean hasRemaining() throws IOException {
        return this.bufferPosition < this.inputBuffer.length;
    }

    public int getPosition() {
        return this.bufferPosition;
    }

    public void setPosition(int position) throws IOException {
        if (position < 0) {
            throw new IOException("position is negative");
//        } else if (position >= this.inputBuffer.length) {
//            throw new IOException("New position is out of range " + position + " >= " + this.inputBuffer.length);
        } else {
            this.bufferPosition = position;
        }
    }

    public byte readByte() throws IOException {
        if (!this.hasRemaining()) {
            throw new IOException("End off buffer reached");
        } else {
            return this.inputBuffer[this.bufferPosition++];
        }
    }

    public int readUnsignedByte() throws IOException {
        if (!this.hasRemaining()) {
            throw new IOException("End off buffer reached");
        } else {
            return this.inputBuffer[this.bufferPosition++] & 255;
        }
    }

    public int peekUnsignedByte(int offset) throws IOException {
        if (offset < 0) {
            throw new IOException("offset is negative");
        } else if (this.bufferPosition + offset >= this.inputBuffer.length) {
            throw new IOException("Offset position is out of range " + (this.bufferPosition + offset)
                    + " >= " + this.inputBuffer.length);
        } else {
            return this.inputBuffer[this.bufferPosition + offset] & 255;
        }
    }

    public byte[] readBytes(int length) throws IOException {
        if (length < 0) {
            throw new IOException("length is negative");
        } else if (this.inputBuffer.length - this.bufferPosition < length) {
            throw new IOException("Premature end of buffer reached");
        } else {
            byte[] bytes = new byte[length];
            System.arraycopy(this.inputBuffer, this.bufferPosition, bytes, 0, length);
            this.bufferPosition += length;
            return bytes;
        }
    }

    public int length() throws IOException {
        return this.inputBuffer.length;
    }
}
