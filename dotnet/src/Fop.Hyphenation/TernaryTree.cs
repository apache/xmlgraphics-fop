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
/// Ternary Search Tree.
/// <para>
/// A ternary search tree is a hybrid between a binary tree and a digital search
/// tree (trie). Keys are limited to strings; a <see cref="char"/> data value is
/// stored in each leaf node and can be used as an index (or pointer) to the data.
/// Branches that contain only one key are compressed to one node by storing a
/// pointer to the trailer substring of the key. The tree can be traversed in
/// sorted order; partial (wildcard) matches and other operations are possible.
/// </para>
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.TernaryTree</c>. The original
/// uses four parallel <c>char[]</c> arrays as the node store (instead of a node
/// class) for memory efficiency, with two reserved split-char values:
/// <c>0x0000</c> as a string terminator and <c>0xFFFF</c> to mark a compressed
/// branch (since <c>0xFFFF</c> is guaranteed not to be a Unicode character).
/// This port preserves the algorithm verbatim. Java <c>Cloneable</c> is rendered
/// as the <see cref="Clone"/> method (deep copy of the node arrays and key vector).
/// </para>
/// </summary>
public class TernaryTree
{
    /// <summary>Reserved split-char value marking a compressed branch (0xFFFF, not a Unicode character).</summary>
    private const char CompressedBranch = '\uFFFF';

    /// <summary>Allocation size for arrays.</summary>
    protected const int BlockSize = 2048;

    /// <summary>
    /// Pointer to the low branch and to the rest of the key when it is stored
    /// directly in this node (Java has no unions).
    /// </summary>
    protected char[] Lo = [];

    /// <summary>Pointer to the high branch.</summary>
    protected char[] Hi = [];

    /// <summary>Pointer to the equal branch and to data when this node is a string terminator.</summary>
    protected char[] Eq = [];

    /// <summary>
    /// The character stored in this node (splitchar). Two special values are
    /// reserved: <c>0x0000</c> as string terminator and <c>0xFFFF</c> to indicate
    /// that the branch starting at this node is compressed.
    /// </summary>
    protected char[] Sc = [];

    /// <summary>Holds the trailing characters of keys when a branch is compressed.</summary>
    protected CharVector Kv = new();

    /// <summary>Root node index.</summary>
    protected char Root;

    /// <summary>Next free node index.</summary>
    protected char Freenode;

    /// <summary>Number of items in the tree.</summary>
    protected int Count;

    /// <summary>Default constructor.</summary>
    public TernaryTree() => Init();

    /// <summary>Initialize (or re-initialize) the tree.</summary>
    protected void Init()
    {
        Root = (char)0;
        Freenode = (char)1;
        Count = 0;
        Lo = new char[BlockSize];
        Hi = new char[BlockSize];
        Eq = new char[BlockSize];
        Sc = new char[BlockSize];
        Kv = new CharVector();
    }

    /// <summary>
    /// Insert a key with the given value. Branches are initially compressed,
    /// needing one node per key plus the size of the string key. They are
    /// decompressed as needed when another key with the same prefix is inserted.
    /// </summary>
    /// <param name="key">the key.</param>
    /// <param name="val">a value.</param>
    public void Insert(string key, char val)
    {
        ArgumentNullException.ThrowIfNull(key);

        // make sure we have enough room in the arrays
        int len = key.Length + 1;    // maximum number of nodes that may be generated
        if (Freenode + len > Eq.Length)
        {
            RedimNodeArrays(Eq.Length + BlockSize);
        }

        char[] strkey = new char[len--];
        key.CopyTo(0, strkey, 0, len);
        strkey[len] = (char)0;
        Root = Insert(Root, strkey, 0, val);
    }

    /// <summary>Insert a key (null-terminated char array form).</summary>
    /// <param name="key">the key.</param>
    /// <param name="start">offset into the key array.</param>
    /// <param name="val">a value.</param>
    public void Insert(char[] key, int start, char val)
    {
        ArgumentNullException.ThrowIfNull(key);
        int len = Strlen(key) + 1;
        if (Freenode + len > Eq.Length)
        {
            RedimNodeArrays(Eq.Length + BlockSize);
        }

        Root = Insert(Root, key, start, val);
    }

    /// <summary>The actual insertion function, recursive version.</summary>
    private char Insert(char p, char[] key, int start, char val)
    {
        int len = Strlen(key, start);
        if (p == 0)
        {
            // this means there is no branch, this node will start a new branch.
            // Instead of doing that, we store the key somewhere else and create
            // only one node with a pointer to the key
            p = Freenode++;
            Eq[p] = val;           // holds data
            Count++;
            Hi[p] = (char)0;
            if (len > 0)
            {
                Sc[p] = CompressedBranch;    // indicates branch is compressed
                Lo[p] = (char)Kv.Alloc(len + 1);    // use 'lo' to hold pointer to key
                Strcpy(Kv.GetArray(), Lo[p], key, start);
            }
            else
            {
                Sc[p] = (char)0;
                Lo[p] = (char)0;
            }

            return p;
        }

        if (Sc[p] == CompressedBranch)
        {
            // branch is compressed: need to decompress
            // this will generate garbage in the external key array
            // but we can do some garbage collection later
            char pp = Freenode++;
            Lo[pp] = Lo[p];    // previous pointer to key
            Eq[pp] = Eq[p];    // previous pointer to data
            Lo[p] = (char)0;
            if (len > 0)
            {
                Sc[p] = Kv.Get(Lo[pp]);
                Eq[p] = pp;
                Lo[pp]++;
                if (Kv.Get(Lo[pp]) == 0)
                {
                    // key completely decompressed leaving garbage in key array
                    Lo[pp] = (char)0;
                    Sc[pp] = (char)0;
                    Hi[pp] = (char)0;
                }
                else
                {
                    // we only got first char of key, rest is still there
                    Sc[pp] = CompressedBranch;
                }
            }
            else
            {
                // In this case we can save a node by swapping the new node
                // with the compressed node
                Sc[pp] = CompressedBranch;
                Hi[p] = pp;
                Sc[p] = (char)0;
                Eq[p] = val;
                Count++;
                return p;
            }
        }

        char s = key[start];
        if (s < Sc[p])
        {
            Lo[p] = Insert(Lo[p], key, start, val);
        }
        else if (s == Sc[p])
        {
            if (s != 0)
            {
                Eq[p] = Insert(Eq[p], key, start + 1, val);
            }
            else
            {
                // key already in tree, overwrite data
                Eq[p] = val;
            }
        }
        else
        {
            Hi[p] = Insert(Hi[p], key, start, val);
        }

        return p;
    }

    /// <summary>Compares two null-terminated char arrays.</summary>
    public static int Strcmp(char[] a, int startA, char[] b, int startB)
    {
        for (; a[startA] == b[startB]; startA++, startB++)
        {
            if (a[startA] == 0)
            {
                return 0;
            }
        }

        return a[startA] - b[startB];
    }

    /// <summary>Compares a string with a null-terminated char array.</summary>
    public static int Strcmp(string str, char[] a, int start)
    {
        ArgumentNullException.ThrowIfNull(str);
        int i;
        int d;
        int len = str.Length;
        for (i = 0; i < len; i++)
        {
            d = str[i] - a[start + i];
            if (d != 0)
            {
                return d;
            }

            if (a[start + i] == 0)
            {
                return d;
            }
        }

        if (a[start + i] != 0)
        {
            return -a[start + i];
        }

        return 0;
    }

    /// <summary>Copies a null-terminated char sequence from <paramref name="src"/> to <paramref name="dst"/>.</summary>
    public static void Strcpy(char[] dst, int di, char[] src, int si)
    {
        while (src[si] != 0)
        {
            dst[di++] = src[si++];
        }

        dst[di] = (char)0;
    }

    /// <summary>Returns the length of a null-terminated char array starting at <paramref name="start"/>.</summary>
    public static int Strlen(char[] a, int start)
    {
        int len = 0;
        for (int i = start; i < a.Length && a[i] != 0; i++)
        {
            len++;
        }

        return len;
    }

    /// <summary>Returns the length of a null-terminated char array.</summary>
    public static int Strlen(char[] a) => Strlen(a, 0);

    /// <summary>Find a key.</summary>
    /// <param name="key">the key.</param>
    /// <returns>the stored value, or -1 if not found.</returns>
    public int Find(string key)
    {
        ArgumentNullException.ThrowIfNull(key);
        int len = key.Length;
        char[] strkey = new char[len + 1];
        key.CopyTo(0, strkey, 0, len);
        strkey[len] = (char)0;

        return Find(strkey, 0);
    }

    /// <summary>Find a key (null-terminated char array form).</summary>
    /// <param name="key">the key.</param>
    /// <param name="start">offset into the key array.</param>
    /// <returns>the stored value, or -1 if not found.</returns>
    public int Find(char[] key, int start)
    {
        ArgumentNullException.ThrowIfNull(key);
        int d;
        char p = Root;
        int i = start;
        char c;

        while (p != 0)
        {
            if (Sc[p] == CompressedBranch)
            {
                if (Strcmp(key, i, Kv.GetArray(), Lo[p]) == 0)
                {
                    return Eq[p];
                }

                return -1;
            }

            c = key[i];
            d = c - Sc[p];
            if (d == 0)
            {
                if (c == 0)
                {
                    return Eq[p];
                }

                i++;
                p = Eq[p];
            }
            else if (d < 0)
            {
                p = Lo[p];
            }
            else
            {
                p = Hi[p];
            }
        }

        return -1;
    }

    /// <summary>Returns <c>true</c> if the key is present.</summary>
    public bool Knows(string key) => Find(key) >= 0;

    // redimension the arrays
    private void RedimNodeArrays(int newsize)
    {
        int len = newsize < Lo.Length ? newsize : Lo.Length;
        char[] na = new char[newsize];
        Array.Copy(Lo, 0, na, 0, len);
        Lo = na;
        na = new char[newsize];
        Array.Copy(Hi, 0, na, 0, len);
        Hi = na;
        na = new char[newsize];
        Array.Copy(Eq, 0, na, 0, len);
        Eq = na;
        na = new char[newsize];
        Array.Copy(Sc, 0, na, 0, len);
        Sc = na;
    }

    /// <summary>The number of keys in the tree.</summary>
    public int Size => Count;

    /// <summary>
    /// Return a deep copy of this tree, matching the Java <c>clone()</c> behaviour
    /// (the node arrays and the key vector are copied).
    /// </summary>
    public TernaryTree Clone()
    {
        var t = (TernaryTree)MemberwiseClone();
        t.Lo = (char[])Lo.Clone();
        t.Hi = (char[])Hi.Clone();
        t.Eq = (char[])Eq.Clone();
        t.Sc = (char[])Sc.Clone();
        t.Kv = Kv.Clone();
        return t;
    }

    /// <summary>
    /// Recursively insert the median first and then the median of the lower and
    /// upper halves, and so on, in order to get a balanced tree. The array of keys
    /// is assumed to be sorted in ascending order.
    /// </summary>
    /// <param name="k">array of keys.</param>
    /// <param name="v">array of values.</param>
    /// <param name="offset">where to insert.</param>
    /// <param name="n">count to insert.</param>
    protected void InsertBalanced(string[] k, char[] v, int offset, int n)
    {
        int m;
        if (n < 1)
        {
            return;
        }

        m = n >> 1;

        Insert(k[m + offset], v[m + offset]);
        InsertBalanced(k, v, offset, m);

        InsertBalanced(k, v, offset + m + 1, n - m - 1);
    }

    /// <summary>Balance the tree for best search performance.</summary>
    public void Balance()
    {
        int i = 0;
        int n = Count;
        string[] k = new string[n];
        char[] v = new char[n];
        var iter = new TernaryTreeIterator(this);
        while (iter.HasMoreElements())
        {
            v[i] = iter.GetValue();
            k[i++] = iter.NextElement();
        }

        Init();
        InsertBalanced(k, v, 0, n);
    }

    /// <summary>
    /// Balance the tree and compact the key vector, removing duplicate trailing
    /// substrings (deduplication uses a temporary <see cref="TernaryTree"/>).
    /// </summary>
    public void TrimToSize()
    {
        // first balance the tree for best performance
        Balance();

        // redimension the node arrays
        RedimNodeArrays(Freenode);

        // ok, compact kv array
        var kx = new CharVector();
        kx.Alloc(1);
        var map = new TernaryTree();
        Compact(kx, map, Root);
        Kv = kx;
        Kv.TrimToSize();
    }

    private void Compact(CharVector kx, TernaryTree map, char p)
    {
        int k;
        if (p == 0)
        {
            return;
        }

        if (Sc[p] == CompressedBranch)
        {
            k = map.Find(Kv.GetArray(), Lo[p]);
            if (k < 0)
            {
                k = kx.Alloc(Strlen(Kv.GetArray(), Lo[p]) + 1);
                Strcpy(kx.GetArray(), k, Kv.GetArray(), Lo[p]);
                map.Insert(kx.GetArray(), k, (char)k);
            }

            Lo[p] = (char)k;
        }
        else
        {
            Compact(kx, map, Lo[p]);
            if (Sc[p] != 0)
            {
                Compact(kx, map, Eq[p]);
            }

            Compact(kx, map, Hi[p]);
        }
    }

    /// <summary>Returns an iterator over the keys in the tree.</summary>
    public TernaryTreeIterator Keys() => new(this);

    /// <summary>
    /// An iterator over the keys of a <see cref="TernaryTree"/>, exposing both the
    /// current key and its stored value. Faithful port of the inner
    /// <c>TernaryTree.Iterator</c> (a Java <c>Enumeration</c>).
    /// </summary>
    public sealed class TernaryTreeIterator
    {
        private const char CompressedBranch = '\uFFFF';

        private readonly TernaryTree tree;

        /// <summary>Current node index.</summary>
        private int cur;

        /// <summary>Current key.</summary>
        private string? curkey;

        /// <summary>Node stack.</summary>
        private readonly Stack<Item> ns;

        /// <summary>Key stack implemented with a <see cref="StringBuilder"/>.</summary>
        private readonly StringBuilder ks;

        /// <summary>Construct an iterator and rewind it to the first key.</summary>
        public TernaryTreeIterator(TernaryTree tree)
        {
            ArgumentNullException.ThrowIfNull(tree);
            this.tree = tree;
            cur = -1;
            ns = new Stack<Item>();
            ks = new StringBuilder();
            Rewind();
        }

        /// <summary>Rewind the iterator to the first key.</summary>
        public void Rewind()
        {
            ns.Clear();
            ks.Length = 0;
            cur = tree.Root;
            Run();
        }

        /// <summary>Return the current key and advance to the next.</summary>
        public string NextElement()
        {
            string res = curkey!;
            cur = Up();
            Run();
            return res;
        }

        /// <summary>Return the value associated with the current key.</summary>
        public char GetValue() => cur >= 0 ? tree.Eq[cur] : (char)0;

        /// <summary>Returns <c>true</c> if there are more elements.</summary>
        public bool HasMoreElements() => cur != -1;

        /// <summary>Traverse upwards.</summary>
        private int Up()
        {
            var i = new Item();
            int res = 0;

            if (ns.Count == 0)
            {
                return -1;
            }

            if (cur != 0 && tree.Sc[cur] == 0)
            {
                return tree.Lo[cur];
            }

            bool climb = true;

            while (climb)
            {
                i = ns.Pop();
                i.Child++;
                switch (i.Child)
                {
                    case 1:
                        if (tree.Sc[i.Parent] != 0)
                        {
                            res = tree.Eq[i.Parent];
                            ns.Push(i.Clone());
                            ks.Append(tree.Sc[i.Parent]);
                        }
                        else
                        {
                            i.Child++;
                            ns.Push(i.Clone());
                            res = tree.Hi[i.Parent];
                        }

                        climb = false;
                        break;

                    case 2:
                        res = tree.Hi[i.Parent];
                        ns.Push(i.Clone());
                        if (ks.Length > 0)
                        {
                            ks.Length--;    // pop
                        }

                        climb = false;
                        break;

                    default:
                        if (ns.Count == 0)
                        {
                            return -1;
                        }

                        climb = true;
                        break;
                }
            }

            return res;
        }

        /// <summary>Traverse the tree to find the next key.</summary>
        private int Run()
        {
            if (cur == -1)
            {
                return -1;
            }

            bool leaf = false;
            while (true)
            {
                // first go down on low branch until leaf or compressed branch
                while (cur != 0)
                {
                    if (tree.Sc[cur] == CompressedBranch)
                    {
                        leaf = true;
                        break;
                    }

                    ns.Push(new Item((char)cur, 0));
                    if (tree.Sc[cur] == 0)
                    {
                        leaf = true;
                        break;
                    }

                    cur = tree.Lo[cur];
                }

                if (leaf)
                {
                    break;
                }

                // nothing found, go up one node and try again
                cur = Up();
                if (cur == -1)
                {
                    return -1;
                }
            }

            // The current node should be a data node and
            // the key should be in the key stack (at least partially)
            var buf = new StringBuilder(ks.ToString());
            if (tree.Sc[cur] == CompressedBranch)
            {
                int p = tree.Lo[cur];
                while (tree.Kv.Get(p) != 0)
                {
                    buf.Append(tree.Kv.Get(p++));
                }
            }

            curkey = buf.ToString();
            return 0;
        }

        /// <summary>A node-stack entry tracking a parent node and which child branch is being visited.</summary>
        private sealed class Item
        {
            /// <summary>Parent node index.</summary>
            public char Parent;

            /// <summary>
            /// Child branch counter. In the Java original this was a <c>char</c> that
            /// only ever holds the small values 0..3; modelled here as <c>int</c> so
            /// it can drive an integer-labelled <c>switch</c> cleanly.
            /// </summary>
            public int Child;

            public Item()
            {
                Parent = (char)0;
                Child = 0;
            }

            public Item(char parent, int child)
            {
                Parent = parent;
                Child = child;
            }

            public Item Clone() => new(Parent, Child);
        }
    }
}
