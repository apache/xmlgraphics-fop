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

package org.apache.fop.events;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import org.apache.fop.events.model.EventMethodModel;
import org.apache.fop.events.model.EventModel;
import org.apache.fop.events.model.EventModelParser;
import org.apache.fop.events.model.EventProducerModel;

public class DefaultEventBroadcaster implements EventBroadcaster {

    private List listeners = new java.util.ArrayList();
    
    /** {@inheritDoc} */
    public void addEventListener(EventListener listener) {
        this.listeners.add(listener);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener) {
        this.listeners.remove(listener);
    }

    /** {@inheritDoc} */
    public int getListenerCount() {
        return this.listeners.size();
    }
    
    /** {@inheritDoc} */
    public void broadcastEvent(Event event) {
        for (int i = 0, c = getListenerCount(); i < c; i++) {
            EventListener listener = (EventListener)this.listeners.get(i);
            listener.processEvent(event);
        }
    }

    private static final String EVENT_MODEL_FILENAME = "event-model.xml";
    private static EventModel eventModel;
    private Map proxies = new java.util.HashMap();
    
    static {
        loadModel(DefaultEventBroadcaster.class, EVENT_MODEL_FILENAME);
    }

    public static void loadModel(Class resourceBaseClass, String resourceName) {
        InputStream in = resourceBaseClass.getResourceAsStream(resourceName);
        if (in == null) {
            throw new MissingResourceException(
                    "File " + resourceName + " not found",
                    DefaultEventBroadcaster.class.getName(), ""); 
        }
        try {
            eventModel = EventModelParser.parse(new StreamSource(in));
        } catch (TransformerException e) {
            throw new MissingResourceException(
                    "Error reading " + resourceName + ": " + e.getMessage(),
                    DefaultEventBroadcaster.class.getName(), ""); 
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public EventProducer getEventProducerFor(Class clazz) {
        if (!EventProducer.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Class must be an implementation of the EventProducer interface: "
                    + clazz.getName());
        }
        EventProducer producer;
        producer = (EventProducer)this.proxies.get(clazz);
        if (producer == null) {
            producer = createProxyFor(clazz);
            this.proxies.put(clazz, producer);
        }
        return producer;
    }
    
    private EventProducer createProxyFor(Class clazz) {
        final EventProducerModel producerModel = eventModel.getProducer(clazz);
        if (producerModel == null) {
            throw new IllegalStateException("Event model doesn't contain the definition for "
                    + clazz.getName());
        }
        return (EventProducer)Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        String methodName = method.getName();
                        EventMethodModel methodModel = producerModel.getMethod(methodName);
                        String eventID = producerModel.getInterfaceName() + "." + methodName;
                        if (methodModel == null) {
                            throw new IllegalStateException(
                                    "Event model isn't consistent"
                                    + " with the EventProducer interface. Please rebuild FOP!"
                                    + " Affected method: "
                                    + eventID);
                        }
                        Map params = new java.util.HashMap();
                        int i = 1;
                        Iterator iter = methodModel.getParameters().iterator();
                        while (iter.hasNext()) {
                            EventMethodModel.Parameter param
                                = (EventMethodModel.Parameter)iter.next();
                            params.put(param.getName(), args[i]);
                            i++;
                        }
                        Event ev = new Event(args[0], eventID, methodModel.getSeverity(), params);
                        broadcastEvent(ev);
                        return null;
                    }
                });
    }
    
}
