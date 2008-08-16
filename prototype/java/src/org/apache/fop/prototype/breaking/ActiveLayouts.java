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

package org.apache.fop.prototype.breaking;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LayoutClass;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
public class ActiveLayouts<L extends Layout> implements Iterable<L> {

    private class ListMap<K, V> implements Iterable<V> {

        private Map<K, List<V>> map = new HashMap<K, List<V>>();

        public void put(K key, V value) {
            List<V> list = map.get(key);
            if (list == null) {
                list = new LinkedList<V>();
                map.put(key, list);
            }
            list.add(value);
        }

        public void clear() {
            map.clear();
        }

        public Iterator<Iterator<V>> keyIterator() {
            final Iterator<Map.Entry<K, List<V>>> keyIter = map.entrySet().iterator();
            return new Iterator<Iterator<V>>() {

                private List<V> nextList;

                @Override
                public boolean hasNext() {
                    while (keyIter.hasNext()) {
                        nextList = keyIter.next().getValue();
                        if (!nextList.isEmpty()) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public Iterator<V> next() {
                    return nextList.iterator();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<V> iterator() {
            final Iterator<Iterator<V>> keyIter = keyIterator();
            return new Iterator<V>() {

                private Iterator<V> currentIter;

                @Override
                public boolean hasNext() {
                    return currentIter != null && currentIter.hasNext() || keyIter.hasNext();
                }

                @Override
                public V next() {
                    if (currentIter == null || !currentIter.hasNext()) {
                        currentIter = keyIter.next();
                    }
                    return currentIter.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }

            };
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return map.toString();
        }

    }

    private ListMap<LayoutClass, L> layouts = new ListMap<LayoutClass, L>();

    public void add(L layout) {
        layouts.put(layout.getLayoutClass(), layout);
    }

    public void updateLayouts(KnuthElement e) {
        assert e.isBox() || e.isGlue();
        ListMap<LayoutClass, L> newMap = new ListMap<LayoutClass, L>();
        for (L l: layouts) {
            l.addElement(e);
            newMap.put(l.getLayoutClass(), l);
        }
        layouts = newMap;
    }

    public void clear() {
        layouts.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<L> iterator() {
        return layouts.iterator();
    }

    public Iterator<Iterator<L>> getClassIterator() {
        return layouts.keyIterator();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (L l: layouts) {
            s.append(l.toString());
            if (s.length() > 0 && s.charAt(s.length() - 1) != '\n') {
                s.append('\n');
            }
            s.append('\n');
        }
        return s.toString();
    }

}
