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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.ProgressInfo;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
class ActiveLayouts<L extends Layout> implements Iterable<L> {

    private class ListMap<K, V> {

        private Map<K, Set<V>> backingMap = new HashMap<K, Set<V>>();

        public void put(K key, V value) {
            Set<V> set = backingMap.get(key);
            if (set == null) {
                set = new HashSet<V>();
                backingMap.put(key, set);
            }
            set.add(value);
        }

        public void removeValue(K key, V value) {
            Set<V> set = backingMap.get(key);
            if (set != null) {
                set.remove(value);
            }
        }

        public void clear() {
            backingMap.clear();
        }

        public Iterator<Iterator<V>> keyIterator() {
            final Iterator<Map.Entry<K, Set<V>>> keyIter = backingMap.entrySet().iterator();
            return new Iterator<Iterator<V>>() {

                private Set<V> nextSet;

                public boolean hasNext() {
                    while (keyIter.hasNext()) {
                        nextSet = keyIter.next().getValue();
                        if (!nextSet.isEmpty()) {
                            return true;
                        }
                    }
                    return false;
                }

                public Iterator<V> next() {
                    return nextSet.iterator();
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }
            };
        }
    }

    private ListMap<Integer, L> blockClasses = new ListMap<Integer, L>();

    private ListMap<ProgressInfo, L> lineClasses = new ListMap<ProgressInfo, L>();

    private List<L> layouts = new LinkedList<L>();

    void setLayoutsFrom(ActiveLayouts<? extends L> other) {
        blockClasses.clear();
        lineClasses.clear();
        layouts.clear();
        for (L layout: other) {
            add(layout);
        }
    }

    void add(L layout) {
        ProgressInfo progress = layout.getProgress();
        blockClasses.put(progress.getPartNumber(), layout);
        lineClasses.put(progress.copy(), layout);
        layouts.add(layout);
    }

    void updateLayouts(KnuthElement e, Breaker<L> breaker) {
        assert e.isBox() || e.isGlue();
        ListMap<ProgressInfo, L> newMap = new ListMap<ProgressInfo, L>();
        for (L l: layouts) {
            breaker.getLayout(l).addElement(e);
            newMap.put(l.getProgress(), l);
        }
        lineClasses = newMap;
    }

    boolean isEmpty() {
        return layouts.isEmpty();
    }

    private abstract class AbstractClassIterator<K> implements Iterator<Iterator<L>> {

        private Iterator<Iterator<L>> iter;

        private final ListMap<K, L> other;

        /**
         * @param iter
         */
        AbstractClassIterator(Iterator<Iterator<L>> iter, ListMap<K, L> other) {
            this.iter = iter;
            this.other = other;
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        abstract K getKey(L l);

        public Iterator<L> next() {
            final Iterator<L> backingIter = iter.next();
            return new Iterator<L>() {

                L currentLayout;

                public boolean hasNext() {
                    return backingIter.hasNext();
                }

                public L next() {
                    currentLayout = backingIter.next();
                    return currentLayout;
                }

                public void remove() {
                    backingIter.remove();
                    other.removeValue(getKey(currentLayout), currentLayout);
                    layouts.remove(currentLayout);
                }
            };
        }

        /** {@inheritDoc} */
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    Iterator<Iterator<L>> getBlockClassIterator() {
        return new AbstractClassIterator<ProgressInfo>(blockClasses.keyIterator(), lineClasses) {

            @Override
            ProgressInfo getKey(L l) {
                return l.getProgress();
            }
        };
    }

    Iterator<Iterator<L>> getLineClassIterator() {
        return new AbstractClassIterator<Integer>(lineClasses.keyIterator(), blockClasses) {

            @Override
            Integer getKey(L l) {
                return l.getProgress().getPartNumber();
            }
        };
    }

    /** {@inheritDoc} */
    public Iterator<L> iterator() {
        return layouts.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (L l: layouts) {
            s.append(l);
            if (s.charAt(s.length() - 1) != '\n') {
                s.append('\n');
            }
            s.append('\n');
        }
        return s.toString();
    }

}
