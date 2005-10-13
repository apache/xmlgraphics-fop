/*
 * Copyright 1999-2004 The Apache Software Foundation.
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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */
package org.apache.fop.render.rtf.rtflib.rtfdoc;

//Java
import java.io.IOException;

//FOP
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;

/**
 * Class to handle list styles.
 */
public class RtfListStyle {
    private RtfListItem rtfListItem;
    
    public void setRtfListItem(RtfListItem item) {
        rtfListItem = item;
    }
    
    public RtfListItem getRtfListItem() {
        return rtfListItem;
    }

    public RtfList getRtfList() {
        return rtfListItem.getParentList();
    }

    /**
     * Gets call before a RtfListItem has to be written.
     */
    public void writeListPrefix(RtfListItem item)
    throws IOException {
    }
    /**
     * Gets call before a paragraph, which is contained by a RtfListItem has to be written.
     */
    public void writeParagraphPrefix(RtfElement element)
    throws IOException {
    }

    /**
     * Gets call when the list table has to be written.
     */        
    public void writeLevelGroup(RtfElement element)
    throws IOException {
    }
}