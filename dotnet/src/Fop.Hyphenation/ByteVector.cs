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
/// A simple growable byte vector with access to the underlying array.
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.ByteVector</c>, preserving its
/// explicit capacity (block-size) growth strategy.
/// </para>
/// </summary>
public class ByteVector
{
    /// <summary>Capacity increment size.</summary>
    private const int DefaultBlockSize = 2048;

    private readonly int blockSize;

    /// <summary>The encapsulated array.</summary>
    private byte[] array;

    /// <summary>Points to next free item.</summary>
    private int n;

    /// <summary>Construct a byte vector instance with the default block size.</summary>
    public ByteVector()
        : this(DefaultBlockSize)
    {
    }

    /// <summary>Construct a byte vector instance.</summary>
    /// <param name="capacity">initial block size.</param>
    public ByteVector(int capacity)
    {
        blockSize = capacity > 0 ? capacity : DefaultBlockSize;
        array = new byte[blockSize];
        n = 0;
    }

    /// <summary>
    /// Construct a byte vector instance over an existing array.
    /// </summary>
    /// <param name="a">byte array to use.</param>
    /// <remarks>
    /// Faithful quirk: matching the Java original, the length pointer is
    /// initialized to 0 here (not <c>a.Length</c>), unlike <see cref="CharVector"/>.
    /// </remarks>
    public ByteVector(byte[] a)
    {
        ArgumentNullException.ThrowIfNull(a);
        blockSize = DefaultBlockSize;
        array = a;
        n = 0;
    }

    /// <summary>Construct a byte vector instance over an existing array.</summary>
    /// <param name="a">byte array to use.</param>
    /// <param name="capacity">initial block size.</param>
    public ByteVector(byte[] a, int capacity)
    {
        ArgumentNullException.ThrowIfNull(a);
        blockSize = capacity > 0 ? capacity : DefaultBlockSize;
        array = a;
        n = 0;
    }

    /// <summary>Obtain the byte vector array.</summary>
    public byte[] GetArray() => array;

    /// <summary>Obtain the number of items in the array.</summary>
    public int Length => n;

    /// <summary>Obtain the capacity of the array.</summary>
    public int Capacity => array.Length;

    /// <summary>Put a byte at the given index.</summary>
    /// <param name="index">the index.</param>
    /// <param name="val">a byte.</param>
    public void Put(int index, byte val) => array[index] = val;

    /// <summary>Get the byte at the given index.</summary>
    /// <param name="index">the index.</param>
    public byte Get(int index) => array[index];

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
            byte[] aux = new byte[len + blockSize];
            Array.Copy(array, 0, aux, 0, len);
            array = aux;
        }

        n += size;
        return index;
    }

    /// <summary>Trim the byte vector to its current length.</summary>
    public void TrimToSize()
    {
        if (n < array.Length)
        {
            byte[] aux = new byte[n];
            Array.Copy(array, 0, aux, 0, n);
            array = aux;
        }
    }
}
