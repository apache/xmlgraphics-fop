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

package org.apache.fop.pdf;

/**
 * Comparator class to enable comparing (and
 * hence sorting) of PDFDestination objects.
 */
public class DestinationComparator implements java.util.Comparator, java.io.Serializable {
/*  public int compare (PDFDestination dest1, PDFDestination dest2) {
    return dest1.getIDRef().compareTo(dest2.getIDRef());
  }*/

    static final long serialVersionUID = 0L;

    /** {@inheritDoc} */
    public int compare(Object obj1, Object obj2) {
        if (obj1 instanceof PDFDestination && obj2 instanceof PDFDestination) {
            PDFDestination dest1 = (PDFDestination)obj1;
            PDFDestination dest2 = (PDFDestination)obj2;
            return dest1.getIDRef().compareTo(dest2.getIDRef());
        }
        return 0;
    }
}
