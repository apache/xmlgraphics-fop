/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.render.rtf;

import java.util.Stack;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfOptions;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;


/**  A BuilderContext holds context information when building an RTF document
 *
 *  @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 *  @author putzi
 *  @author Peter Herweg <pherweg@web.de>
 *
 *  This class was originally developed by Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  for the JFOR project and is now integrated into FOP.
 */


class BuilderContext {
    /** stack of RtfContainers */
    private final Stack m_containers = new Stack();

    /** stack of TableContexts */
    private final Stack m_tableContexts = new Stack();

    /** stack of IBuilders */
    private final Stack m_builders = new Stack();

    /** Rtf options */
    IRtfOptions m_options;

    BuilderContext(IRtfOptions rtfOptions) {
        m_options = rtfOptions;
    }

    /** find first object of given class from top of stack s
     *  @return null if not found
     */
    private Object getObjectFromStack(Stack s, Class desiredClass) {
        Object result = null;
        final Stack copy = (Stack)s.clone();
        while (!copy.isEmpty()) {
            final Object o = copy.pop();
            if (desiredClass.isAssignableFrom(o.getClass())) {
                result = o;
                break;
            }
        }
        return result;
    }

    /* find the "nearest" IBuilder of given class /
    Object getBuilder(Class builderClass,boolean required)
    throws Exception
    {
        final IBuilder result = (IBuilder)getObjectFromStack(m_builders,builderClass);
        if(result == null && required) {
            throw new Exception(
                "IBuilder of class '" + builderClass.getName() + "' not found on builders stack"
               );
        }
        return result;
    }*/

    /** find the "nearest" container that implements the given interface on our stack
     *  @param required if true, ConverterException is thrown if no container found
     *  @param forWhichBuilder used in error message if container not found
     */
    RtfContainer getContainer(Class containerClass, boolean required,
                              Object /*IBuilder*/ forWhichBuilder) throws Exception {
        // TODO what to do if the desired container is not at the top of the stack?
        // close top-of-stack container?
        final RtfContainer result = (RtfContainer)getObjectFromStack(m_containers,
                containerClass);

        if (result == null && required) {
            throw new Exception(
                "No RtfContainer of class '" + containerClass.getName()
                + "' available for '" + forWhichBuilder.getClass().getName() + "' builder"
               );
        }

        return result;
    }

    /** push an RtfContainer on our stack */
    void pushContainer(RtfContainer c) {
        m_containers.push(c);
    }

    /**
     * In some cases an RtfContainer must be replaced by another one on the
     * stack. This happens when handling nested fo:blocks for example: after
     * handling a nested block the enclosing block must switch to a new
     * paragraph container to handle what follows the nested block.
     * TODO: what happens to elements that are "more on top" than oldC on the
     * stack? shouldn't they be closed or something?
     */
    void replaceContainer(RtfContainer oldC, RtfContainer newC)
    throws Exception {
        // treating the Stack as a Vector allows such manipulations (yes, I hear you screaming ;-)
        final int index = m_containers.indexOf(oldC);
        if (index < 0) {
            throw new Exception("container to replace not found:" + oldC);
        }
        m_containers.setElementAt(newC, index);
    }

    /** pop the topmost RtfContainer from our stack */
    void popContainer() {
        m_containers.pop();
    }

    /* push an IBuilder to our stack /
    void pushBuilder(IBuilder b)
    {
        m_builders.push(b);
    }*/

    /** pop the topmost IBuilder from our stack and return previous builder on stack
     *  @return null if builders stack is empty

    IBuilder popBuilderAndGetPreviousOne()
    {
        IBuilder result = null;
        m_builders.pop();
        if(!m_builders.isEmpty()) {
            result = (IBuilder)m_builders.peek();
        }
        return result;
    }
    */
    /** return the current TableContext */
    TableContext getTableContext() {
        return (TableContext)m_tableContexts.peek();
    }

    /** push a TableContext to our stack */
    void pushTableContext(TableContext tc) {
        m_tableContexts.push(tc);
    }

    /** pop a TableContext from our stack */
    void popTableContext() {
        m_tableContexts.pop();
    }

}