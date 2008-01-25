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

package org.apache.fop.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.fop.events.model.EventMethodModel;
import org.apache.fop.events.model.EventModel;
import org.apache.fop.events.model.EventProducerModel;
import org.apache.fop.events.model.EventSeverity;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DefaultDocletTagFactory;
import com.thoughtworks.qdox.model.DocletTagFactory;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

public class EventProducerCollector {

    private static final String CLASSNAME_EVENT_PRODUCER = "org.apache.fop.events.EventProducer";
    private static final Map PRIMITIVE_MAP;
    
    static {
        Map m = new java.util.HashMap();
        m.put("boolean", Boolean.class);
        m.put("byte", Byte.class);
        m.put("char", Character.class);
        m.put("short", Short.class);
        m.put("int", Integer.class);
        m.put("long", Long.class);
        m.put("float", Float.class);
        m.put("double", Double.class);
        PRIMITIVE_MAP = Collections.unmodifiableMap(m);
    }
    
    private DocletTagFactory tagFactory;
    private EventModel model = new EventModel();

    public EventProducerCollector() {
        this.tagFactory = createDocletTagFactory();
    }

    protected DocletTagFactory createDocletTagFactory() {
        return new DefaultDocletTagFactory();
    }

    public void scanFile(File src, String filename)
            throws IOException, EventConventionException, ClassNotFoundException {
        JavaDocBuilder builder = new JavaDocBuilder(this.tagFactory);
        builder.addSource(src);
        JavaClass[] classes = builder.getClasses();
        for (int i = 0, c = classes.length; i < c; i++) {
            JavaClass clazz = classes[i];
            if (clazz.isInterface() && implementsInterface(clazz, CLASSNAME_EVENT_PRODUCER)) {
                processJavaClass(clazz, filename);
            }
        }
    }

    private boolean implementsInterface(JavaClass clazz, String intf) {
        JavaClass[] classes = clazz.getImplementedInterfaces();
        for (int i = 0, c = classes.length; i < c; i++) {
            JavaClass cl = classes[i];
            if (cl.getFullyQualifiedName().equals(intf)) {
                return true;
            }
        }
        return false;
    }

    protected void processJavaClass(JavaClass clazz, String javaFilename)
                throws EventConventionException, ClassNotFoundException {
        EventProducerModel prodMeta = new EventProducerModel(clazz.getFullyQualifiedName());
        JavaMethod[] methods = clazz.getMethods();
        for (int i = 0, c = methods.length; i < c; i++) {
            JavaMethod method = methods[i];
            
            //Check EventProducer conventions
            if (!method.getReturns().isVoid()) {
                throw new EventConventionException("All methods of interface "
                        + clazz.getFullyQualifiedName() + " must have return type 'void'!");
            }
            String methodSig = clazz.getFullyQualifiedName() + "." + method.getCallSignature();
            JavaParameter[] params = method.getParameters();
            if (params.length < 1) {
                throw new EventConventionException("The method " + methodSig
                        + " must have at least one parameter: 'Object source'!");
            }
            Type firstType = params[0].getType();
            if (firstType.isPrimitive() || !"source".equals(params[0].getName())) {
                throw new EventConventionException("The first parameter of the method " + methodSig
                        + " must be: 'Object source'!");
            }
            
            //build method model
            EventMethodModel methodMeta = new EventMethodModel(
                    method.getName(), EventSeverity.INFO);
            if (params.length > 1) {
                for (int j = 1, cj = params.length; j < cj; j++) {
                    JavaParameter p = params[j];
                    Class type;
                    JavaClass pClass = p.getType().getJavaClass();
                    if (p.getType().isPrimitive()) {
                        type = (Class)PRIMITIVE_MAP.get(pClass.getName());
                        if (type == null) {
                            throw new UnsupportedOperationException(
                                    "Primitive datatype not supported: " + pClass.getName());
                        }
                    } else {
                        String className = pClass.getFullyQualifiedName();
                        type = Class.forName(className);
                    }
                    methodMeta.addParameter(type, p.getName());
                }
            }
            prodMeta.addMethod(methodMeta);
        }
        this.model.addProducer(prodMeta);
    }

    public EventModel getModel() {
        return this.model;
    }
    
    public void saveModelToXML(File modelFile) throws IOException {
        getModel().saveToXML(modelFile);
    }
    
}
