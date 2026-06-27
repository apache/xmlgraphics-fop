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

namespace Fop.Hyphenation;

/// <summary>
/// A simple growable char vector with access to the underlying array.
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.CharVector</c>, preserving its
/// explicit capacity (block-size) growth strategy. The Java <c>Cloneable</c>
/// support is rendered as the <see cref="Clone"/> method, which performs a deep
/// copy of the backing array (matching Java's <c>array.clone()</c>).
/// </para>
/// </summary>
public class CharVector
{
    /// <summary>Capacity increment size.</summary>
    private const int DefaultBlockSize = 2048;

    private readonly int blockSize;

    /// <summary>The encapsulated array.</summary>
    private char[] array;

    /// <summary>Points to next free item.</summary>
    private int n;

    /// <summary>Construct a char vector instance with the default block size.</summary>
    public CharVector()
        : this(DefaultBlockSize)
    {
    }

    /// <summary>Construct a char vector instance.</summary>
    /// <param name="capacity">initial block size.</param>
    public CharVector(int capacity)
    {
        blockSize = capacity > 0 ? capacity : DefaultBlockSize;
        array = new char[blockSize];
        n = 0;
    }

    /// <summary>Construct a char vector instance over an existing array.</summary>
    /// <param name="a">char array to use.</param>
    public CharVector(char[] a)
    {
        ArgumentNullException.ThrowIfNull(a);
        blockSize = DefaultBlockSize;
        array = a;
        n = a.Length;
    }

    /// <summary>Construct a char vector instance over an existing array.</summary>
    /// <param name="a">char array to use.</param>
    /// <param name="capacity">initial block size.</param>
    public CharVector(char[] a, int capacity)
    {
        ArgumentNullException.ThrowIfNull(a);
        blockSize = capacity > 0 ? capacity : DefaultBlockSize;
        array = a;
        n = a.Length;
    }

    /// <summary>Reset the length of the vector, but don't clear the contents.</summary>
    public void Clear() => n = 0;

    /// <summary>
    /// Return a deep copy of this vector (backing array copied), matching the
    /// Java <c>clone()</c> behaviour.
    /// </summary>
    public CharVector Clone()
    {
        var cv = new CharVector((char[])array.Clone(), blockSize > 0 ? blockSize : DefaultBlockSize)
        {
            n = this.n,
        };
        return cv;
    }

    /// <summary>Obtain the char vector array.</summary>
    public char[] GetArray() => array;

    /// <summary>Obtain the number of items in the array.</summary>
    public int Length => n;

    /// <summary>Obtain the capacity of the array.</summary>
    public int Capacity => array.Length;

    /// <summary>Put a char at the given index.</summary>
    /// <param name="index">the index.</param>
    /// <param name="val">a char.</param>
    public void Put(int index, char val) => array[index] = val;

    /// <summary>Get the char at the given index.</summary>
    /// <param name="index">the index.</param>
    public char Get(int index) => array[index];

    /// <summary>
    /// Allocate memory in the array, like <c>malloc()</c>.
    /// </summary>
    /// <param name="size">size to allocate.</param>
    /// <returns>the previous length (offset of the allocation).</returns>
    public int Alloc(int size)
    {
        int index = n;
        int len = array.Length;
        if (n + size >= len)
        {
            char[] aux = new char[len + blockSize];
            Array.Copy(array, 0, aux, 0, len);
            array = aux;
        }

        n += size;
        return index;
    }

    /// <summary>Trim the char vector to its current length.</summary>
    public void TrimToSize()
    {
        if (n < array.Length)
        {
            char[] aux = new char[n];
            Array.Copy(array, 0, aux, 0, n);
            array = aux;
        }
    }
}
