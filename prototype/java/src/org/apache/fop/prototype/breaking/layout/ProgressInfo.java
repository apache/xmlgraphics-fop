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

package org.apache.fop.prototype.breaking.layout;

/**
 * TODO javadoc
 */
public class ProgressInfo {

    private int totalLength;

    private int totalStretch;

    private int totalShrink;

    private int partNumber;

    ProgressInfo() { }

    public/*TODO*/ ProgressInfo(int min, int opt, int max, int partNum) {
        this.totalLength = opt;
        this.totalStretch = max - opt;
        this.totalShrink = opt - min;
        this.partNumber = partNum;
    }

    ProgressInfo(ProgressInfo o) {
        this.totalLength = o.totalLength;
        this.totalStretch = o.totalStretch;
        this.totalShrink = o.totalShrink;
        this.partNumber = o.partNumber;
    }

    public ProgressInfo copy() {
        return new ProgressInfo(this);
    }

    public int getTotalLength() {
        return totalLength;
    }

    /**
     * @return the totalStretch
     */
    public int getTotalStretch() {
        return totalStretch;
    }

    /**
     * @return the totalShrink
     */
    public int getTotalShrink() {
        return totalShrink;
    }

    public int getPartNumber() {
        return partNumber;
    }

    void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    void add(int length) {
        totalLength += length;
    }

    void add(int length, int stretch, int shrink) {
        add(length);
        totalStretch += stretch;
        totalShrink += shrink;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        assert o instanceof ProgressInfo;
        ProgressInfo p = (ProgressInfo) o;
        return this.totalLength == p.totalLength && this.totalStretch == p.totalStretch
                && this.totalShrink == p.totalShrink && this.partNumber == p.partNumber;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Integer.valueOf(totalLength).hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return partNumber + ":" + totalLength + "+" + totalStretch + "−" + totalShrink;
    }
}
