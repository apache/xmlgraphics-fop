/*
   Copyright 2002-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.fop.datastructs;

//import Tree;

/**
 * A testbed for <tt>Node</tt>.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class TNode extends Node {

    private Object content = null;

    public TNode () {
        super();
    }

    public TNode(Tree tree, TNode parent, int index) {
        super(parent, index);
    }

    public TNode(TNode parent) {
        super(parent);
    }

    /**
     * @param parent  The parent <tt>TNode</tt> of this TNode.  If null,
     *                this must be the root node.
     * @param content An object which is the actual content of this node;
     *                the contents of the TNode.
     */

    public TNode(TNode parent, Object content) {
        super(parent);
        this.content = content;
    }

    /**
     * @param parent  The parent <tt>TNode</tt> of this TNode.  If null,
     *                this must be the root node.
     * @param index   int index of this child in the parent node.
     * @param content An object which is the actual content of this node;
     *                the contents of the TNode.
     */

    public TNode(TNode parent, int index, Object content)
        throws IndexOutOfBoundsException {
        super(parent, index);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void unsetContent() {
        this.content = null;
    }


}// TNode
