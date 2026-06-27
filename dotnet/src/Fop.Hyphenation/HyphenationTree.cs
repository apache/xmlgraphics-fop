// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Text;

namespace Fop.Hyphenation;

/// <summary>
/// Stores the hyphenation patterns (interletter values), character classes and exceptions in an
/// efficient way for fast lookup, and provides the method to hyphenate a word (the Liang algorithm).
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.HyphenationTree</c>. It extends
/// <see cref="TernaryTree"/> (which holds the patterns) and implements <see cref="IPatternConsumer"/>
/// so a pattern loader can populate it. The original work was authored by Carlos Villegas.
/// </para>
/// </summary>
public class HyphenationTree : TernaryTree, IPatternConsumer
{
    /// <summary>Reserved split-char value marking a compressed branch (0xFFFF, not a Unicode character).</summary>
    private const char CompressedBranch = '\uFFFF';

    /// <summary>Value space: stores the packed interletter values.</summary>
    protected ByteVector Vspace { get; }

    /// <summary>Stores the hyphenation exceptions (word -&gt; list of alternating string/Hyphen).</summary>
    protected Dictionary<string, IList<object>> Stoplist { get; }

    /// <summary>Stores the character classes (equivalence map).</summary>
    protected TernaryTree Classmap { get; }

    /// <summary>Temporary map to store interletter values during pattern loading.</summary>
    private TernaryTree? ivalues;

    /// <summary>Default constructor.</summary>
    public HyphenationTree()
    {
        Stoplist = new Dictionary<string, IList<object>>(23, StringComparer.Ordinal);
        Classmap = new TernaryTree();
        Vspace = new ByteVector();
        Vspace.Alloc(1);    // this reserves index 0, which we don't use
    }

    /// <summary>
    /// Packs the values by storing them in 4 bits, two values into a byte. Value range is 0 to 9; zero
    /// is used as terminator, so 1 is added to each value.
    /// </summary>
    /// <param name="values">a string of digits from '0' to '9' representing the interletter values.</param>
    /// <returns>the index into the value space array where the packed values are stored.</returns>
    protected int PackValues(string values)
    {
        int n = values.Length;
        int m = (n & 1) == 1 ? (n >> 1) + 2 : (n >> 1) + 1;
        int offset = Vspace.Alloc(m);
        byte[] va = Vspace.GetArray();
        for (int i = 0; i < n; i++)
        {
            int j = i >> 1;
            byte v = (byte)((values[i] - '0' + 1) & 0x0f);
            if ((i & 1) == 1)
            {
                va[j + offset] = (byte)(va[j + offset] | v);
            }
            else
            {
                va[j + offset] = (byte)(v << 4);    // big endian
            }
        }

        va[m - 1 + offset] = 0;    // terminator
        return offset;
    }

    /// <summary>Unpack values into a digit string.</summary>
    /// <param name="k">the value-space index.</param>
    protected string UnpackValues(int k)
    {
        var buf = new StringBuilder();
        byte v = Vspace.Get(k++);
        while (v != 0)
        {
            char c = (char)((v >> 4) - 1 + '0');
            buf.Append(c);
            c = (char)(v & 0x0f);
            if (c == 0)
            {
                break;
            }

            c = (char)(c - 1 + '0');
            buf.Append(c);
            v = Vspace.Get(k++);
        }

        return buf.ToString();
    }

    /// <summary>Find a pattern's interletter-value digit string, or the empty string when absent.</summary>
    /// <param name="pat">a pattern.</param>
    public string FindPattern(string pat)
    {
        int k = Find(pat);
        return k >= 0 ? UnpackValues(k) : string.Empty;
    }

    /// <summary>
    /// String compare; returns 0 if equal or if <paramref name="t"/> is a prefix-substring of
    /// <paramref name="s"/> (matching the Java <c>hstrcmp</c> semantics used by pattern search).
    /// </summary>
    protected static int Hstrcmp(char[] s, int si, char[] t, int ti)
    {
        for (; s[si] == t[ti]; si++, ti++)
        {
            if (s[si] == 0)
            {
                return 0;
            }
        }

        if (t[ti] == 0)
        {
            return 0;
        }

        return s[si] - t[ti];
    }

    /// <summary>Get the unpacked interletter values for a value-space index as a byte array.</summary>
    /// <param name="k">the value-space index.</param>
    protected byte[] GetValues(int k)
    {
        var buf = new StringBuilder();
        byte v = Vspace.Get(k++);
        while (v != 0)
        {
            char c = (char)((v >> 4) - 1);
            buf.Append(c);
            c = (char)(v & 0x0f);
            if (c == 0)
            {
                break;
            }

            c = (char)(c - 1);
            buf.Append(c);
            v = Vspace.Get(k++);
        }

        byte[] res = new byte[buf.Length];
        for (int i = 0; i < res.Length; i++)
        {
            res[i] = (byte)buf[i];
        }

        return res;
    }

    /// <summary>
    /// Search for all possible partial matches of <paramref name="word"/> starting at
    /// <paramref name="index"/> and update the interletter values <paramref name="il"/>. The patterns
    /// are stored in the ternary tree, so this is done efficiently without testing every pattern.
    /// </summary>
    /// <param name="word">null-terminated word to match.</param>
    /// <param name="index">start index from word.</param>
    /// <param name="il">interletter values array to update.</param>
    protected void SearchPatterns(char[] word, int index, byte[] il)
    {
        byte[] values;
        int i = index;
        char p;
        char q;
        char sp = word[i];
        p = Root;

        while (p > 0 && p < Sc.Length)
        {
            if (Sc[p] == CompressedBranch)
            {
                if (Hstrcmp(word, i, Kv.GetArray(), Lo[p]) == 0)
                {
                    values = GetValues(Eq[p]);    // data pointer is in eq[]
                    int j = index;
                    foreach (byte value in values)
                    {
                        if (j < il.Length && value > il[j])
                        {
                            il[j] = value;
                        }

                        j++;
                    }
                }

                return;
            }

            int d = sp - Sc[p];
            if (d == 0)
            {
                if (sp == 0)
                {
                    break;
                }

                sp = word[++i];
                p = Eq[p];
                q = p;

                // look for a pattern ending at this position by searching for the null char
                // (splitchar == 0)
                while (q > 0 && q < Sc.Length)
                {
                    if (Sc[q] == CompressedBranch)
                    {
                        // stop at compressed branch
                        break;
                    }

                    if (Sc[q] == 0)
                    {
                        values = GetValues(Eq[q]);
                        int j = index;
                        foreach (byte value in values)
                        {
                            if (j < il.Length && value > il[j])
                            {
                                il[j] = value;
                            }

                            j++;
                        }

                        break;
                    }

                    // actually the code should be q = sc[q] < 0 ? hi[q] : lo[q];
                    // but Java chars (and C# chars) are unsigned.
                    q = Lo[q];
                }
            }
            else
            {
                p = d < 0 ? Lo[p] : Hi[p];
            }
        }
    }

    /// <summary>
    /// Hyphenate a word and return a <see cref="Hyphenation"/> object, or <c>null</c> when it is not
    /// hyphenated. Words containing non-letter characters (per the class map) are split and each part
    /// hyphenated independently.
    /// </summary>
    /// <param name="word">the word to be hyphenated.</param>
    /// <param name="remainCharCount">minimum characters before the hyphenation point.</param>
    /// <param name="pushCharCount">minimum characters after the hyphenation point.</param>
    public Hyphenation? Hyphenate(string word, int remainCharCount, int pushCharCount)
    {
        ArgumentNullException.ThrowIfNull(word);
        char[] w = word.ToCharArray();
        if (IsMultiPartWord(w, w.Length))
        {
            List<char[]> words = SplitOnNonCharacters(w);
            return new Hyphenation(new string(w),
                GetHyphPointsForWords(words, remainCharCount, pushCharCount));
        }

        return Hyphenate(w, 0, w.Length, remainCharCount, pushCharCount);
    }

    private bool IsMultiPartWord(char[] w, int len)
    {
        int wordParts = 0;
        for (int i = 0; i < len; i++)
        {
            char[] c = new char[2];
            c[0] = w[i];
            int nc = Classmap.Find(c, 0);
            if (nc > 0)
            {
                if (wordParts > 1)
                {
                    return true;
                }

                wordParts = 1;
            }
            else
            {
                if (wordParts == 1)
                {
                    wordParts++;
                }
            }
        }

        return false;
    }

    private List<char[]> SplitOnNonCharacters(char[] word)
    {
        List<int> breakPoints = GetNonLetterBreaks(word);
        if (breakPoints.Count == 0)
        {
            return [];
        }

        var words = new List<char[]>();
        for (int ibreak = 0; ibreak < breakPoints.Count; ibreak++)
        {
            char[] newWord = GetWordFromCharArray(word, ibreak == 0 ? 0 : breakPoints[ibreak - 1],
                breakPoints[ibreak]);
            words.Add(newWord);
        }

        if (word.Length - breakPoints[^1] - 1 > 1)
        {
            char[] newWord = GetWordFromCharArray(word, breakPoints[^1], word.Length);
            words.Add(newWord);
        }

        return words;
    }

    private List<int> GetNonLetterBreaks(char[] word)
    {
        char[] c = new char[2];
        var breakPoints = new List<int>();
        bool foundLetter = false;
        for (int i = 0; i < word.Length; i++)
        {
            c[0] = word[i];
            if (Classmap.Find(c, 0) < 0)
            {
                if (foundLetter)
                {
                    breakPoints.Add(i);
                }
            }
            else
            {
                foundLetter = true;
            }
        }

        return breakPoints;
    }

    private static char[] GetWordFromCharArray(char[] word, int startIndex, int endIndex)
    {
        char[] newWord = new char[endIndex - (startIndex == 0 ? startIndex : startIndex + 1)];
        int iChar = 0;
        for (int i = startIndex == 0 ? 0 : startIndex + 1; i < endIndex; i++)
        {
            newWord[iChar++] = word[i];
        }

        return newWord;
    }

    private int[] GetHyphPointsForWords(List<char[]> nonLetterWords, int remainCharCount, int pushCharCount)
    {
        int[] breaks = [];
        for (int iNonLetterWord = 0; iNonLetterWord < nonLetterWords.Count; iNonLetterWord++)
        {
            char[] nonLetterWord = nonLetterWords[iNonLetterWord];
            Hyphenation? curHyph = Hyphenate(nonLetterWord, 0, nonLetterWord.Length,
                iNonLetterWord == 0 ? remainCharCount : 1,
                iNonLetterWord == nonLetterWords.Count - 1 ? pushCharCount : 1);
            if (curHyph == null)
            {
                continue;
            }

            int[] hyphPoints = curHyph.GetHyphenationPoints();
            int[] combined = new int[breaks.Length + hyphPoints.Length];
            int foreWordsSize = CalcForeWordsSize(nonLetterWords, iNonLetterWord);
            for (int i = 0; i < hyphPoints.Length; i++)
            {
                hyphPoints[i] += foreWordsSize;
            }

            Array.Copy(breaks, 0, combined, 0, breaks.Length);
            Array.Copy(hyphPoints, 0, combined, breaks.Length, hyphPoints.Length);
            breaks = combined;
        }

        return breaks;
    }

    private static int CalcForeWordsSize(List<char[]> nonLetterWords, int iNonLetterWord)
    {
        int result = 0;
        for (int i = 0; i < iNonLetterWord; i++)
        {
            result += nonLetterWords[i].Length + 1;
        }

        return result;
    }

    /// <summary>
    /// Hyphenate a word and return its hyphenation, or <c>null</c> when not hyphenated. See the Java
    /// original for the index-bookkeeping invariants (word-start/end markers, ignore-at-beginning).
    /// </summary>
    /// <param name="w">char array that contains the word.</param>
    /// <param name="offset">offset to the first character in the word.</param>
    /// <param name="len">length of the word.</param>
    /// <param name="remainCharCount">minimum characters before the hyphenation point.</param>
    /// <param name="pushCharCount">minimum characters after the hyphenation point.</param>
    public Hyphenation? Hyphenate(char[] w, int offset, int len, int remainCharCount, int pushCharCount)
    {
        ArgumentNullException.ThrowIfNull(w);
        int i;
        char[] word = new char[len + 3];

        // normalize word
        char[] c = new char[2];
        int iIgnoreAtBeginning = 0;
        int iLength = len;
        bool bEndOfLetters = false;
        for (i = 1; i <= len; i++)
        {
            c[0] = w[offset + i - 1];
            int nc = Classmap.Find(c, 0);
            if (nc < 0)
            {
                // found a non-letter character ...
                if (i == 1 + iIgnoreAtBeginning)
                {
                    // ... before any letter character
                    iIgnoreAtBeginning++;
                }
                else
                {
                    // ... after a letter character
                    bEndOfLetters = true;
                }

                iLength--;
            }
            else
            {
                if (!bEndOfLetters)
                {
                    word[i - iIgnoreAtBeginning] = (char)nc;
                }
                else
                {
                    return null;
                }
            }
        }

        len = iLength;
        if (len < remainCharCount + pushCharCount)
        {
            // word is too short to be hyphenated
            return null;
        }

        int[] result = new int[len + 1];
        int k = 0;

        // check exception list first
        string sw = new string(word, 1, len);
        if (Stoplist.TryGetValue(sw, out IList<object>? hw))
        {
            // assume only simple hyphens (Hyphen.pre="-", Hyphen.post = Hyphen.no = null)
            int j = 0;
            for (i = 0; i < hw.Count; i++)
            {
                object o = hw[i];
                if (o is string s)
                {
                    j += s.Length;
                    if (j >= remainCharCount && j < len - pushCharCount)
                    {
                        result[k++] = j + iIgnoreAtBeginning;
                    }
                }
            }
        }
        else
        {
            // use the algorithm to get hyphenation points
            word[0] = '.';                  // word start marker
            word[len + 1] = '.';            // word end marker
            word[len + 2] = (char)0;        // null terminated
            byte[] il = new byte[len + 3];  // initialized to zero
            for (i = 0; i < len + 1; i++)
            {
                SearchPatterns(word, i, il);
            }

            // hyphenation points are located where the interletter value is odd
            for (i = 0; i < len; i++)
            {
                if ((il[i + 1] & 1) == 1 && i >= remainCharCount && i <= len - pushCharCount)
                {
                    result[k++] = i + iIgnoreAtBeginning;
                }
            }
        }

        if (k > 0)
        {
            // trim result array
            int[] res = new int[k];
            Array.Copy(result, 0, res, 0, k);
            return new Hyphenation(new string(w, offset, len), res);
        }

        return null;
    }

    /// <summary>
    /// Add a character class to the tree. A character class defines the valid word characters for
    /// hyphenation and a normalization: the first char of the group is the equivalence char that the
    /// other chars in the group map to.
    /// </summary>
    /// <param name="charGroup">a character class (group).</param>
    public void AddClass(string charGroup)
    {
        ArgumentNullException.ThrowIfNull(charGroup);
        if (charGroup.Length > 0)
        {
            char equivChar = charGroup[0];
            char[] key = new char[2];
            key[1] = (char)0;
            for (int i = 0; i < charGroup.Length; i++)
            {
                key[0] = charGroup[i];
                Classmap.Insert(key, 0, equivChar);
            }
        }
    }

    /// <summary>Add a hyphenation exception (alternating strings and <see cref="Hyphen"/> objects).</summary>
    /// <param name="word">the normalized word.</param>
    /// <param name="hyphenatedWord">the pre-hyphenated word.</param>
    public void AddException(string word, IList<object> hyphenatedWord)
    {
        ArgumentNullException.ThrowIfNull(word);
        ArgumentNullException.ThrowIfNull(hyphenatedWord);
        Stoplist[word] = hyphenatedWord;
    }

    /// <summary>
    /// Add a pattern to the tree. The interletter values are packed and de-duplicated through the
    /// temporary <c>ivalues</c> map (which is allocated by the pattern loader before parsing).
    /// </summary>
    /// <param name="pattern">the hyphenation pattern.</param>
    /// <param name="values">interletter weight values; should contain only digit characters '0'..'9'.</param>
    public void AddPattern(string pattern, string values)
    {
        ArgumentNullException.ThrowIfNull(pattern);
        ArgumentNullException.ThrowIfNull(values);
        ivalues ??= new TernaryTree();
        int k = ivalues.Find(values);
        if (k <= 0)
        {
            k = PackValues(values);
            ivalues.Insert(values, (char)k);
        }

        Insert(pattern, (char)k);
    }

    /// <summary>
    /// Loads hyphenation patterns from an XML source (per the FOP <c>hyphenation.dtd</c>) into this
    /// tree via <see cref="PatternParser"/>, then trims the internal structures to size.
    /// </summary>
    /// <param name="reader">an <see cref="System.Xml.XmlReader"/> positioned at the document start.</param>
    public void LoadPatterns(System.Xml.XmlReader reader)
    {
        ArgumentNullException.ThrowIfNull(reader);
        var pp = new PatternParser(this);
        ivalues = new TernaryTree();

        pp.Parse(reader);

        // patterns/values should now be in the tree; optimize a bit.
        TrimToSize();
        Vspace.TrimToSize();
        Classmap.TrimToSize();

        // get rid of the auxiliary map
        ivalues = null;
    }
}
