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
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.CountingOutputStream;

public class PDFLinearization {
    private PDFDocument doc;
    private Map<PDFPage, Set<PDFObject>> pageObjsMap = new HashMap<PDFPage, Set<PDFObject>>();
    private PDFDictionary linearDict;
    private HintTable hintTable;

    public PDFLinearization(PDFDocument doc) {
        this.doc = doc;
    }

    static class HintTable extends PDFStream {
        private List<PDFPage> pages;
        int pageStartPos;
        List<Integer> sharedLengths = new ArrayList<Integer>();
        List<Integer> pageLengths = new ArrayList<Integer>();
        List<Integer> contentStreamLengths = new ArrayList<Integer>();
        List<Integer> objCount = new ArrayList<Integer>();
        Map<String, int[]> hintGroups = new HashMap<String, int[]>();

        public HintTable(PDFDocument doc) {
            super(false);
            doc.assignObjectNumber(this);
            doc.addObject(this);
            pages = doc.pageObjs;
            for (int i = 0; i < pages.size(); i++) {
                pageLengths.add(0);
                contentStreamLengths.add(0);
                objCount.add(0);
            }
            hintGroups.put("/C", new int[4]);
            hintGroups.put("/L", new int[4]);
            hintGroups.put("/I", new int[4]);
            hintGroups.put("/E", new int[4]);
            hintGroups.put("/O", new int[4]);
            hintGroups.put("/V", new int[4]);
        }

        @Override
        public PDFFilterList getFilterList() {
            return new PDFFilterList(getDocument().isEncryptionActive());
        }

        @Override
        protected void outputRawStreamData(OutputStream os) throws IOException {
            CountingOutputStream bos = new CountingOutputStream(os);

            //start header
            writeULong(1, bos); //1
            writeULong(pageStartPos, bos); //2
            writeCard16(32, bos); //3
            writeULong(0, bos); //4
            writeCard16(32, bos); //5
            writeULong(0, bos); //6
            writeCard16(0, bos); //7
            writeULong(0, bos); //8
            writeCard16(32, bos); //9
            writeCard16(0, bos); //10
            writeCard16(0, bos); //11
            writeCard16(0, bos); //12
            writeCard16(4, bos); //13
            //end header

            for (PDFPage page : pages) {
                writeULong(objCount.get(page.pageIndex) - 1, bos);
            }
            for (PDFPage page : pages) {
                writeULong(pageLengths.get(page.pageIndex), bos);
            }
            for (PDFPage page : pages) {
                writeULong(contentStreamLengths.get(page.pageIndex), bos);
            }

            writeSharedTable(bos);

            for (Map.Entry<String, int[]> group : hintGroups.entrySet()) {
                put(group.getKey(), bos.getCount());
                for (int i : group.getValue()) {
                    writeULong(i, bos);
                }
                if (group.getKey().equals("/C")) {
                    writeULong(0, bos);
                    writeCard16(0, bos);
                }
            }
        }

        private void writeSharedTable(CountingOutputStream bos) throws IOException {
            put("/S", bos.getCount());

            //Shared object hint table, header section
            writeULong(0, bos); //1
            writeULong(0, bos); //2
            writeULong(sharedLengths.size(), bos); //3
            writeULong(sharedLengths.size(), bos); //4
            writeCard16(0, bos); //5
            writeULong(0, bos); //6
            writeCard16(32, bos); //7

            for (int i : sharedLengths) {
                writeULong(i, bos);
            }
            writeULong(0, bos);
        }

        private void writeCard16(int s, OutputStream bos) throws IOException {
            byte b1 = (byte)((s >> 8) & 0xff);
            byte b2 = (byte)(s & 0xff);
            bos.write(b1);
            bos.write(b2);
        }

        private void writeULong(int s, OutputStream bos) throws IOException {
            byte b1 = (byte)((s >> 24) & 0xff);
            byte b2 = (byte)((s >> 16) & 0xff);
            byte b3 = (byte)((s >> 8) & 0xff);
            byte b4 = (byte)(s & 0xff);
            bos.write(b1);
            bos.write(b2);
            bos.write(b3);
            bos.write(b4);
        }
    }

    static class LinearPDFDictionary extends PDFDictionary {
        private int lastsize = -1;

        public LinearPDFDictionary(PDFDocument doc) {
            put("Linearized", 1);
            put("/L", 0);
            PDFArray larray = new PDFArray();
            larray.add(0);
            larray.add(0);
            put("/H", larray);
            doc.assignObjectNumber(this);
            getObjectNumber().getNumber();
            put("/O", getObjectNumber().getNumber() + 3);
            put("/E", 0);
            put("/N", doc.pageObjs.size());
            put("/T", 0);
        }

        public int output(OutputStream stream) throws IOException {
            int size = super.output(stream);
            int padding = lastsize - size + 32;
            if (lastsize == -1) {
                padding = 32;
                lastsize = size;
            }
            writePadding(padding, stream);
            return size + padding;
        }
    }


    private Set<PDFObject> assignNumbers() throws IOException {
        Set<PDFObject> page1Children = getPage1Children();
        if (!doc.pageObjs.isEmpty()) {
            for (int i = 1; i < doc.pageObjs.size(); i++) {
                PDFPage page = doc.pageObjs.get(i);
                Set<PDFObject> children = pageObjsMap.get(page);
                for (PDFObject c : children) {
                    if (!page1Children.contains(c) && c.hasObjectNumber()) {
                        c.getObjectNumber().getNumber();
                    }
                }
            }
            for (PDFObject o : doc.objects) {
                if (o instanceof PDFDests || o instanceof PDFOutline) {
                    for (PDFObject c : getChildren(o)) {
                        c.getObjectNumber().getNumber();
                    }
                }
                if (o instanceof PDFInfo || o instanceof PDFPageLabels) {
                    o.getObjectNumber().getNumber();
                }
            }
            for (PDFObject o : doc.objects) {
                if (!page1Children.contains(o)) {
                    o.getObjectNumber().getNumber();
                }
            }
        }
        linearDict = new LinearPDFDictionary(doc);
        for (PDFObject o : page1Children) {
            o.getObjectNumber().getNumber();
        }
        sort(doc.objects);
        return page1Children;
    }

    private void sort(List<PDFObject> objects) {
        Collections.sort(objects, new Comparator<PDFObject>() {
            public int compare(PDFObject o1, PDFObject o2) {
                return ((Integer) o1.getObjectNumber().getNumber()).compareTo(o2.getObjectNumber().getNumber());
            }
        });
    }

    private Set<PDFObject> getChildren(PDFObject o) {
        Set<PDFObject> children = new LinkedHashSet<PDFObject>();
        children.add(o);
        o.getChildren(children);
        return children;
    }

    public void outputPages(OutputStream stream) throws IOException {
        Collections.sort(doc.pageObjs, new Comparator<PDFPage>() {
            public int compare(PDFPage o1, PDFPage o2) {
                return ((Integer) o1.pageIndex).compareTo(o2.pageIndex);
            }
        });
        doc.objects.addAll(doc.trailerObjects);
        doc.trailerObjects = null;
        if (doc.getStructureTreeElements() != null) {
            doc.objects.addAll(doc.getStructureTreeElements());
            doc.structureTreeElements = null;
        }
        for (int i = 0; i < doc.objects.size() * 2; i++) {
            doc.indirectObjectOffsets.add(0L);
        }
        Set<PDFObject> page1Children = assignNumbers();
        doc.streamIndirectObject(linearDict, new ByteArrayOutputStream());
        for (PDFObject o : page1Children) {
            doc.objects.remove(o);
        }
        int sizeOfRest = doc.objects.size();

        ByteArrayOutputStream fakeHeaderTrailerStream = new ByteArrayOutputStream();
        long topTrailer = doc.position;
        doc.writeTrailer(fakeHeaderTrailerStream, sizeOfRest, page1Children.size() + 1,
                page1Children.size() + sizeOfRest + 1, Long.MAX_VALUE, 0);
        doc.position += fakeHeaderTrailerStream.size();

        ByteArrayOutputStream pageStream = new ByteArrayOutputStream();
        writeObjects(page1Children, pageStream, sizeOfRest + 1);
        long trailerOffset = doc.position;
        ByteArrayOutputStream footerTrailerStream = new ByteArrayOutputStream();
        doc.writeTrailer(footerTrailerStream, 0, sizeOfRest, sizeOfRest, 0, topTrailer);
        doc.position += footerTrailerStream.size();

        linearDict.put("/L", doc.position);

        PDFDocument.outputIndirectObject(linearDict, stream);
        CountingOutputStream realTrailer = new CountingOutputStream(stream);
        doc.writeTrailer(realTrailer, sizeOfRest, page1Children.size() + 1,
                page1Children.size() + sizeOfRest + 1, trailerOffset, 0);
        writePadding(fakeHeaderTrailerStream.size() - realTrailer.getCount(), stream);
        for (PDFObject o : page1Children) {
            PDFDocument.outputIndirectObject(o, stream);
            if (o instanceof HintTable) {
                break;
            }
        }
        stream.write(pageStream.toByteArray());
        stream.write(footerTrailerStream.toByteArray());
    }

    private Set<PDFObject> getPage1Children() throws IOException {
        Set<PDFObject> page1Children = new LinkedHashSet<PDFObject>();
        if (!doc.pageObjs.isEmpty()) {
            PDFPage page1 = doc.pageObjs.get(0);
            page1Children.add(doc.getRoot());
            hintTable = new HintTable(doc);
            page1Children.add(hintTable);
            page1Children.add(page1);
            page1.getChildren(page1Children);
            doc.objects.remove(doc.getPages());
            doc.objects.add(0, doc.getPages());
            pageObjsMap.put(page1, page1Children);

            for (int i = 1; i < doc.pageObjs.size(); i++) {
                PDFPage page = doc.pageObjs.get(i);
                pageObjsMap.put(page, getChildren(page));
            }
        }
        return page1Children;
    }

    private static void writePadding(int padding, OutputStream stream) throws IOException {
        for (int i = 0; i < padding; i++) {
            stream.write(" ".getBytes("UTF-8"));
        }
    }

    private void writeObjects(Set<PDFObject> children1, OutputStream pageStream, int sizeOfRest) throws IOException {
        writePage1(children1, pageStream);
        linearDict.put("/E", doc.position);
        for (PDFPage page : doc.pageObjs) {
            if (page.pageIndex != 0) {
                writePage(page, pageStream);
            }
        }
        while (!doc.objects.isEmpty()) {
            PDFObject o = doc.objects.remove(0);
            if (o instanceof PDFOutline) {
                writeObjectGroup("/O", getChildren(o), pageStream);
            } else if (o instanceof PDFDests) {
                writeObjectGroup("/E", getChildren(o), pageStream);
            } else if (o instanceof PDFInfo) {
                writeObjectGroup("/I", getChildren(o), pageStream);
            } else if (o instanceof PDFPageLabels) {
                writeObjectGroup("/L", getChildren(o), pageStream);
            } else if (o instanceof PDFStructTreeRoot) {
                writeObjectGroup("/C", getChildren(o), pageStream);
            } else {
                doc.streamIndirectObject(o, pageStream);
            }
        }
        linearDict.put("/T", doc.position + 8 + String.valueOf(sizeOfRest).length());
    }

    private void writeObjectGroup(String name, Set<PDFObject> objects, OutputStream pageStream)
            throws IOException {
        List<PDFObject> children = new ArrayList<PDFObject>(objects);
        sort(children);

        int[] values = hintTable.hintGroups.get(name);
        values[0] = children.iterator().next().getObjectNumber().getNumber();
        values[1] = (int) doc.position;
        values[2] = children.size();
        for (PDFObject o : children) {
            values[3] += doc.streamIndirectObject(o, pageStream);
            doc.objects.remove(o);
        }
    }

    private void writePage1(Set<PDFObject> children1, OutputStream pageStream) throws IOException {
        hintTable.pageStartPos = (int) doc.position;
        OutputStream stream = new ByteArrayOutputStream();

        Set<PDFObject> sharedChildren = getSharedObjects();

        int page1Len = 0;
        int objCount = 0;
        int sharedCount = 0;
        for (PDFObject o : children1) {
            if (o instanceof HintTable) {
                PDFArray a = (PDFArray) linearDict.get("/H");
                a.set(0, doc.position);
                doc.streamIndirectObject(o, stream);
                a.set(1, doc.position - (Double)a.get(0));
                stream = pageStream;
            } else {
                int len = doc.streamIndirectObject(o, stream);
                if (o instanceof PDFStream && hintTable.contentStreamLengths.get(0) == 0) {
                    hintTable.contentStreamLengths.set(0, len);
                }
                if (!(o instanceof PDFRoot)) {
                    page1Len += len;
                    objCount++;
                }
                if (sharedChildren.contains(o)) {
                    hintTable.sharedLengths.set(sharedCount, len);
                    sharedCount++;
                }
            }
        }
        hintTable.pageLengths.set(0, page1Len);
        hintTable.objCount.set(0, objCount);
    }

    private Set<PDFObject> getSharedObjects() {
        Set<PDFObject> pageSharedChildren = getChildren(doc.pageObjs.get(0));
        for (int i = 0; i < pageSharedChildren.size(); i++) {
            hintTable.sharedLengths.add(0);
        }
        return pageSharedChildren;
    }

    private void writePage(PDFPage page, OutputStream pageStream) throws IOException {
        Set<PDFObject> children = pageObjsMap.get(page);
        int pageLen = 0;
        int objCount = 0;
        for (PDFObject c : children) {
            if (doc.objects.contains(c)) {
                int len = doc.streamIndirectObject(c, pageStream);
                if (c instanceof PDFStream) {
                    hintTable.contentStreamLengths.set(page.pageIndex, len);
                }
                pageLen += len;
                doc.objects.remove(c);
                objCount++;
            }
        }
        hintTable.pageLengths.set(page.pageIndex, pageLen);
        hintTable.objCount.set(page.pageIndex, objCount);
    }
}
