/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Seshadri
/*
 * This package is to be used for all Oeprating System related activities.
 * This file manages system buffers
 */

package org.apache.fop.system;


// FOP

import org.apache.fop.fo.FONode;


// Java

import java.io.*;
import java.util.Hashtable;


public class BufferManager {


    protected FileWriter fw;
    protected FileReader fr;
    protected char cache[];    // Cache
    protected long csize;      // Cache size


    protected File buff = null;

    protected long fp = 0;

    protected long markStart =
        0;                     // used to set the current point in the stream while reading
    protected long markEnd = 0;
    protected long curMark = 0;

    // Hash of objects and their offsets within

    Hashtable offSetTable = new Hashtable();

    private class Offset {

        long fp = 0;    // File Pointer
        int length;
        char[] data;    // when no buffer is specified

        Offset(long fp, int length, char data[]) {
            this.fp = fp;
            this.length = length;
            this.data = data;
        }

    }



    public void addBufferFile(File buff) {

        if (buff != null)
            try {
                fw = new FileWriter(buff);
                fr = new FileReader(buff);
                csize = 100000;
                this.buff = buff;
            } catch (Exception e) {
                System.out.println(e);
            }

    }

    public void writeBuffer(Object obj, char arr[]) {

        int length = arr.length;

        if (buff != null) {
            offSetTable.put(obj, new Offset(this.fp, length, null));
            try {
                fw.write(arr);

                this.fp += length;
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            // Store the data in memory
            offSetTable.put(obj, new Offset(this.fp, length, arr));
        }


    }


    public void readComplete() {

        // An indication that manager can close the writable buffers and prepare
        // for reading..
        if (buff != null)
            try {

                fw.close();

                cache = new char[(int)csize];
                setupCache(curMark);

            } catch (Exception e) {
                System.out.println(e);
            }
    }




    public char[] readBuffer(Object obj) {

        Offset values = (Offset)offSetTable.get(obj);

        // Was buffering used?

        if (buff != null) {


            char ca[] = new char[values.length];

            // Check if csize is too small

            if (csize < values.length) {
                System.out.println("Cache size too small");
            }


            // Is the data outside the cache?

            if (!(values.fp >= markStart
                    && values.fp + values.length <= markEnd)) {

                setupCache(values.fp);
            }


            for (long i = values.fp - markStart, j = 0; j < values.length;
                    ++i, ++j) {

                ca[(int)j] = cache[(int)i];
            }


            return ca;
        } else {
            return values.data;
        }
    }

    protected void setupCache(long curMark) {

        try {

            FileReader fr = new FileReader(buff);
            fr.skip(curMark);

            long rem = buff.length() - curMark;
            if (rem > csize) {

                rem = csize;
            }

            fr.read(cache, 0, (int)rem);


            markStart = curMark;
            markEnd = rem - 1;

        } catch (Exception e) {
            System.out.println(e);
        }


    }


}
