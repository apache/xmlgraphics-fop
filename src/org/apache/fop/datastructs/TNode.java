/*
 * TNode.java
 *
 * Created: Sat Oct 27 13:44:34 2001
 *
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datastructs;

//import Tree;

/**
 * A testbed for <tt>Node</tt>.
 */
public class TNode extends Node {

    private Object content = null;

    public TNode (Tree tree) throws TreeException {
        super(tree);
    }

    public TNode(Tree tree, TNode parent, int index)
        throws TreeException {
        super(tree, parent, index);
    }

    public TNode(Tree tree, TNode parent) throws TreeException {
        super(tree, parent);
    }

    /**
     * @param tree    the enclosing <tt>Tree</tt> instance.  Needed to enable
     *                the call to the superclass constructor.
     * @param parent  The parent <tt>TNode</tt> of this TNode.  If null,
     *                this must be the root node.
     * @param content An object which is the actual content of this node;
     *                the contents of the TNode.
     */

    public TNode(Tree tree, TNode parent, Object content)
        throws TreeException {
        super(tree, parent);
        this.content = content;
    }

    /**
     * @param tree    the enclosing <tt>Tree</tt> instance.  Needed to enable
     *                the call to the superclass constructor.
     * @param parent  The parent <tt>TNode</tt> of this TNode.  If null,
     *                this must be the root node.
     * @param index   int index of this child in the parent node.
     * @param content An object which is the actual content of this node;
     *                the contents of the TNode.
     */

    public TNode(Tree tree, TNode parent, int index, Object content)
        throws TreeException, IndexOutOfBoundsException {
        super(tree, parent, index);
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
