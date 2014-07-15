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

package org.apache.fop.afp.modca;

/**
 * Base class for AFP descriptor objects
 */
public abstract class AbstractDescriptor extends AbstractTripletStructuredObject {

    /** width of this descriptor */
    protected int width;
    /** height of this descriptor */
    protected int height;
    /** width resolution of this descriptor */
    protected int widthRes;
    /** height resolution of this descriptor */
    protected int heightRes;

    /**
     * Default constructor
     */
    public AbstractDescriptor() {
    }

    /**
     * Constructor a PresentationTextDescriptor for the specified
     * width and height.
     *
     * @param width The width of the page.
     * @param height The height of the page.
     * @param widthRes The width resolution of the page.
     * @param heightRes The height resolution of the page.
     */
    public AbstractDescriptor(int width, int height, int widthRes, int heightRes) {
        this.width = width;
        this.height = height;
        this.widthRes = widthRes;
        this.heightRes = heightRes;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "width=" + width
        + ", height=" + height
        + ", widthRes=" + widthRes
        + ", heightRes=" + heightRes;
    }

    /**
     * Returns the width
     *
     * @return the width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height
     *
     * @return the height
     */
    public int getHeight() {
        return this.height;
    }

}
