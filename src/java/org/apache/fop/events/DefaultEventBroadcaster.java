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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.util.Service;

import org.apache.fop.events.model.EventMethodModel;
import org.apache.fop.events.model.EventModel;
import org.apache.fop.events.model.EventModelFactory;
import org.apache.fop.events.model.EventProducerModel;
import org.apache.fop.events.model.EventSeverity;

/**
 * Default implementation of the EventBroadcaster interface. It holds a list of event listeners
 * and can provide {@link EventProducer} instances for type-safe event production.
 */
public class DefaultEventBroadcaster implements EventBroadcaster {

    /** Holds all registered event listeners */
    protected CompositeEventListener listeners = new CompositeEventListener();
    
    /** {@inheritDoc} */
    public void addEventListener(EventListener listener) {
        this.listeners.addEventListener(listener);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener) {
        this.listeners.removeEventListener(listener);
    }

    /** {@inheritDoc} */
    public boolean hasEventListeners() {
        return this.listeners.hasEventListeners();
    }
    
    /** {@inheritDoc} */
    public void broadcastEvent(Event event) {
        this.listeners.processEvent(event);
    }

    private static List/*<EventModel>*/ eventModels = new java.util.ArrayList();
    private Map proxies = new java.util.HashMap();
    
    static {
        Iterator iter = Service.providers(EventModelFactory.class, true);
        while (iter.hasNext()) {
            EventModelFactory factory = (EventModelFactory)iter.next();
            addEventModel(factory.createEventModel());
        }
    }

    /**
     * Adds a new {@link EventModel} to the list of registered event models.
     * @param eventModel the event model instance
     */
    public static void addEventModel(EventModel eventModel) {
        eventModels.add(eventModel);
    }
    
    /** {@inheritDoc} */
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
    
    private EventProducerModel getEventProducerModel(Class clazz) {
        for (int i = 0, c = eventModels.size(); i < c; i++) {
            EventModel eventModel = (EventModel)eventModels.get(i);
            EventProducerModel producerModel = eventModel.getProducer(clazz);
            if (producerModel != null) {
                return producerModel;
            }
        }
        return null;
    }
    
    /**
     * Creates a dynamic proxy for the given EventProducer interface that will handle the
     * conversion of the method call into the broadcasting of an event instance.
     * @param clazz a descendant interface of EventProducer
     * @return the EventProducer instance
     */
    protected EventProducer createProxyFor(Class clazz) {
        final EventProducerModel producerModel = getEventProducerModel(clazz);
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
                        
                        if (ev.getSeverity() == EventSeverity.FATAL) {
                            EventExceptionManager.throwException(ev,
                                    methodModel.getExceptionClass());
                        }
                        return null;
                    }
                });
    }
    
}
