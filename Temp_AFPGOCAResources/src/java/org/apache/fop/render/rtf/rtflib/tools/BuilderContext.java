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

package org.apache.fop.render.rtf.rtflib.tools;

import java.util.Stack;

import org.apache.fop.render.rtf.rtflib.exceptions.RtfException;
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

public class BuilderContext {
    /** stack of RtfContainers */
    private final Stack containers = new Stack();

    /** stack of TableContexts */
    private final Stack tableContexts = new Stack();

    /** stack of IBuilders */
    private final Stack builders = new Stack();

    /** Rtf options */
    private IRtfOptions options;

    public BuilderContext(IRtfOptions rtfOptions) {
        options = rtfOptions;
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
    public Object getBuilder(Class builderClass,boolean required)
    throws Exception
    {
        final IBuilder result = (IBuilder)getObjectFromStack(builders,builderClass);
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
    public RtfContainer getContainer(Class containerClass, boolean required,
                              Object /*IBuilder*/ forWhichBuilder) throws RtfException {
        // TODO what to do if the desired container is not at the top of the stack?
        // close top-of-stack container?
        final RtfContainer result = (RtfContainer)getObjectFromStack(containers,
                containerClass);

        if (result == null && required) {
            throw new RtfException(
                "No RtfContainer of class '" + containerClass.getName()
                + "' available for '" + forWhichBuilder.getClass().getName() + "' builder"
               );
        }

        return result;
    }

    /** push an RtfContainer on our stack */
    public void pushContainer(RtfContainer c) {
        containers.push(c);
    }

    /**
     * In some cases an RtfContainer must be replaced by another one on the
     * stack. This happens when handling nested fo:blocks for example: after
     * handling a nested block the enclosing block must switch to a new
     * paragraph container to handle what follows the nested block.
     * TODO: what happens to elements that are "more on top" than oldC on the
     * stack? shouldn't they be closed or something?
     */
    public void replaceContainer(RtfContainer oldC, RtfContainer newC)
    throws Exception {
        // treating the Stack as a Vector allows such manipulations (yes, I hear you screaming ;-)
        final int index = containers.indexOf(oldC);
        if (index < 0) {
            throw new Exception("container to replace not found:" + oldC);
        }
        containers.setElementAt(newC, index);
    }

    /** pop the topmost RtfContainer from our stack */
    public void popContainer() {
        containers.pop();
    }

    /* push an IBuilder to our stack /
    public void pushBuilder(IBuilder b)
    {
        builders.push(b);
    }*/

    /** pop the topmost IBuilder from our stack and return previous builder on stack
     *  @return null if builders stack is empty

    public IBuilder popBuilderAndGetPreviousOne()
    {
        IBuilder result = null;
        builders.pop();
        if(!builders.isEmpty()) {
            result = (IBuilder)builders.peek();
        }
        return result;
    }
    */
    /** return the current TableContext */
    public TableContext getTableContext() {
        return (TableContext)tableContexts.peek();
    }

    /** push a TableContext to our stack */
    public void pushTableContext(TableContext tc) {
        tableContexts.push(tc);
    }

    /** pop a TableContext from our stack */
    public void popTableContext() {
        tableContexts.pop();
    }

}
