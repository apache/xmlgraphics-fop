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
package org.apache.fop.layout.hyphenation;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.messaging.MessageHandler;

/**
 * This class is the main entry point to the hyphenation package.
 * You can use only the static methods or create an instance.
 *
 * @author Carlos Villegas <cav@uniscope.co.jp>
 */
public class Hyphenator {
    static HashMap hyphenTrees = new HashMap();

    private HyphenationTree hyphenTree = null;
    private int remainCharCount = 2;
    private int pushCharCount = 2;
    private static boolean errorDump = false;

    public Hyphenator(String lang, String country, int leftMin,
                      int rightMin) {
        hyphenTree = getHyphenationTree(lang, country);
        remainCharCount = leftMin;
        pushCharCount = rightMin;
    }

    public static HyphenationTree getHyphenationTree(String lang,
            String country) {
        String key = lang;
        // check whether the country code has been used
        if (country != null &&!country.equals("none"))
            key += "_" + country;
            // first try to find it in the cache
        if (hyphenTrees.containsKey(key))
            return (HyphenationTree)hyphenTrees.get(key);
        if (hyphenTrees.containsKey(lang))
            return (HyphenationTree)hyphenTrees.get(lang);

        HyphenationTree hTree = getFopHyphenationTree(key);
        if (hTree == null) {
            String hyphenDir =
                Configuration.getStringValue("hyphenation-dir");
            if (hyphenDir != null) {
                hTree = getUserHyphenationTree(key, hyphenDir);
            }
        }
        // put it into the pattern cache
        if (hTree != null) {
            hyphenTrees.put(key, hTree);
        } else {
            MessageHandler.errorln("Couldn't find hyphenation pattern "
                                   + key);
        }
        return hTree;
    }

    private static InputStream getResourceStream(String key) {
        InputStream is = null;
        // Try to use Context Class Loader to load the properties file.
        try {
            java.lang.reflect.Method getCCL =
                Thread.class.getMethod("getContextClassLoader", new Class[0]);
            if (getCCL != null) {
                ClassLoader contextClassLoader =
                    (ClassLoader)getCCL.invoke(Thread.currentThread(),
                                               new Object[0]);
                is = contextClassLoader.getResourceAsStream("hyph/" + key
                                                            + ".hyp");
            }
        } catch (Exception e) {}

        if (is == null) {
            is = Hyphenator.class.getResourceAsStream("/hyph/" + key
                                                      + ".hyp");
        }

        return is;
    }

    public static HyphenationTree getFopHyphenationTree(String key) {
        HyphenationTree hTree = null;
        ObjectInputStream ois = null;
        InputStream is = null;
        try {
            is = getResourceStream(key);
            if (is == null) {
                if (key.length() == 5) {
                    is = getResourceStream(key.substring(0, 2));
                    if (is != null) {
                        MessageHandler.errorln("Couldn't find hyphenation pattern  "
                                               + key
                                               + "\nusing general language pattern "
                                               + key.substring(0, 2)
                                               + " instead.");
                    } else {
                        if (errorDump) {
                            MessageHandler.errorln("Couldn't find precompiled "
                                                   + "fop hyphenation pattern "
                                                   + key + ".hyp");
                        }
                        return null;
                    }
                } else {
                    if (errorDump) {
                        MessageHandler.errorln("Couldn't find precompiled "
                                               + "fop hyphenation pattern "
                                               + key + ".hyp");
                    }
                    return null;
                }
            }
            ois = new ObjectInputStream(is);
            hTree = (HyphenationTree)ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    MessageHandler.errorln("can't close hyphenation object stream");
                }
            }
        }
        return hTree;
    }

    /**
     * load tree from serialized file or xml file
     * using configuration settings
     */
    public static HyphenationTree getUserHyphenationTree(String key,
            String hyphenDir) {
        HyphenationTree hTree = null;
        // I use here the following convention. The file name specified in
        // the configuration is taken as the base name. First we try
        // name + ".hyp" assuming a serialized HyphenationTree. If that fails
        // we try name + ".xml", assumming a raw hyphenation pattern file.

        // first try serialized object
        File hyphenFile = new File(hyphenDir, key + ".hyp");
        if (hyphenFile.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(hyphenFile));
                hTree = (HyphenationTree)ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {}
                }
            }
            return hTree;
        } else {

            // try the raw XML file
            hyphenFile = new File(hyphenDir, key + ".xml");
            if (hyphenFile.exists()) {
                hTree = new HyphenationTree();
                if (errorDump) {
                    MessageHandler.errorln("reading " + hyphenDir + key
                                           + ".xml");
                }
                try {
                    hTree.loadPatterns(hyphenFile.getPath());
                    if (errorDump) {
                        System.out.println("Stats: ");
                        hTree.printStats();
                    }
                    return hTree;
                } catch (HyphenationException ex) {
                    if (errorDump) {
                        MessageHandler.errorln("Can't load user patterns "
                                               + "from xml file " + hyphenDir
                                               + key + ".xml");
                    }
                    return null;
                }
            } else {
                if (errorDump) {
                    MessageHandler.errorln("Tried to load "
                                           + hyphenFile.toString()
                                           + "\nCannot find compiled nor xml file for "
                                           + "hyphenation pattern" + key);
                }
                return null;
            }
        }
    }

    public static Hyphenation hyphenate(String lang, String country,
                                        String word, int leftMin,
                                        int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            MessageHandler.errorln("Error building hyphenation tree for language "
                                   + lang);
            return null;
        }
        return hTree.hyphenate(word, leftMin, rightMin);
    }

    public static Hyphenation hyphenate(String lang, String country,
                                        char[] word, int offset, int len,
                                        int leftMin, int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            MessageHandler.errorln("Error building hyphenation tree for language "
                                   + lang);
            return null;
        }
        return hTree.hyphenate(word, offset, len, leftMin, rightMin);
    }

    public void setMinRemainCharCount(int min) {
        remainCharCount = min;
    }

    public void setMinPushCharCount(int min) {
        pushCharCount = min;
    }

    public void setLanguage(String lang, String country) {
        hyphenTree = getHyphenationTree(lang, country);
    }

    public Hyphenation hyphenate(char[] word, int offset, int len) {
        if (hyphenTree == null)
            return null;
        return hyphenTree.hyphenate(word, offset, len, remainCharCount,
                                    pushCharCount);
    }

    public Hyphenation hyphenate(String word) {
        if (hyphenTree == null)
            return null;
        return hyphenTree.hyphenate(word, remainCharCount, pushCharCount);
    }

}
