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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

/**
 * <p>An RtfElement that can contain other elements.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch).</p>
 */

public class RtfContainer extends RtfElement {
    private LinkedList children;  // 'final' removed by Boris Poudérous on 07/22/2002
    private RtfOptions options = new RtfOptions();
    private RtfElement lastChild;

    /** Create an RTF container as a child of given container */
    RtfContainer(RtfContainer parent, Writer w) throws IOException {
        this(parent, w, null);
    }

    /** Create an RTF container as a child of given container with given attributes */
    RtfContainer(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {
        super(parent, w, attr);
        children = new LinkedList();
    }

    /**
     * set options
     * @param opt options to set
     */
    public void setOptions(RtfOptions opt) {
        options = opt;
    }

    /**
     * add a child element to this
     * @param e child element to add
     * @throws RtfStructureException for trying to add an invalid child (??)
     */
    protected void addChild(RtfElement e)
    throws RtfStructureException {
        if (isClosed()) {
            // No childs should be added to a container that has been closed
            final StringBuffer sb = new StringBuffer();
            sb.append("addChild: container already closed (parent=");
            sb.append(this.getClass().getName());
            sb.append(" child=");
            sb.append(e.getClass().getName());
            sb.append(")");
            final String msg = sb.toString();

            // warn of this problem
            final RtfFile rf = getRtfFile();
//            if(rf.getLog() != null) {
//               rf.getLog().logWarning(msg);
//            }

            // TODO this should be activated to help detect XSL-FO constructs
            // that we do not handle properly.
            /*
            throw new RtfStructureException(msg);
             */
        }

        children.add(e);
        lastChild = e;
    }

    /**
     * @return a copy of our children's list
     */
    public List getChildren() {
        return (List)children.clone();
    }

    /**
     * @return the number of children
     */
    public int getChildCount() {
        return children.size();
    }

    private int findChildren(RtfElement aChild, int iStart) {
        for (Iterator it = this.getChildren().iterator(); it.hasNext();) {
          final RtfElement e = (RtfElement)it.next();
          if (aChild == e) {
              return iStart;
          } else if (e instanceof RtfContainer) {
              int iFound = ((RtfContainer)e).findChildren(aChild, (iStart + 1));
              if (iFound != -1) {
                  return iFound;
              }
          }
        }
        return -1;
    }

    /**
     * Find the passed child in the current container
     * @param aChild the child element
     * @return the depth (nested level) inside the current container
     */
    public int findChildren(RtfElement aChild) {
        return findChildren(aChild, 0);
    }

    /**
     * Add by Boris Poudérous on 07/22/2002
     * Set the children list
     * @param list list of child objects
     * @return true if process succeeded
     */
    public boolean setChildren(List list) {
      if (list instanceof LinkedList) {
          this.children = (LinkedList) list;
          return true;
        }

      return false;
    }

    /**
     * write RTF code of all our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent()
    throws IOException {
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.writeRtf();
        }
    }

    /** return our options */
    RtfOptions getOptions() {
        return options;
    }

    /** true if this (recursively) contains at least one RtfText object */
    boolean containsText() {
        boolean result = false;
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfText) {
                result = !e.isEmpty();
            } else if (e instanceof RtfContainer) {
                if (((RtfContainer)e).containsText()) {
                    result = true;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }

    /** debugging to given Writer */
    void dump(Writer w, int indent)
    throws IOException {
        super.dump(w, indent);
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.dump(w, indent + 1);
        }
    }

    /**
     * minimal debugging display
     * @return String representation of object contents
     */
    public String toString() {
        return super.toString() + " (" + getChildCount() + " children)";
    }

    /**
     * @return false if empty or if our options block writing
     */
    protected boolean okToWriteRtf() {
        boolean result = super.okToWriteRtf() && !isEmpty();
        if (result && !options.renderContainer(this)) {
            result = false;
        }
        return result;
    }

    /**
     * @return true if this element would generate no "useful" RTF content,
     * i.e. (for RtfContainer) true if it has no children where isEmpty() is false
     */
    public boolean isEmpty() {
        boolean result = true;
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (!e.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }
}
