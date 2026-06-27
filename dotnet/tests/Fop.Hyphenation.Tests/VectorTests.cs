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

using Fop.Hyphenation;
using Xunit;

namespace Fop.Hyphenation.Tests;

public class ByteVectorTests
{
    [Fact]
    public void DefaultCapacityIsBlockSize()
    {
        var v = new ByteVector();
        Assert.Equal(2048, v.Capacity);
        Assert.Equal(0, v.Length);
    }

    [Fact]
    public void NonPositiveCapacityFallsBackToDefault()
    {
        Assert.Equal(2048, new ByteVector(0).Capacity);
        Assert.Equal(2048, new ByteVector(-5).Capacity);
    }

    [Fact]
    public void AllocReturnsPreviousLengthAndAdvances()
    {
        var v = new ByteVector(8);
        Assert.Equal(0, v.Alloc(3));
        Assert.Equal(3, v.Length);
        Assert.Equal(3, v.Alloc(2));
        Assert.Equal(5, v.Length);
    }

    [Fact]
    public void PutAndGetRoundTrip()
    {
        var v = new ByteVector(8);
        v.Alloc(4);
        v.Put(0, 10);
        v.Put(3, 42);
        Assert.Equal((byte)10, v.Get(0));
        Assert.Equal((byte)42, v.Get(3));
    }

    [Fact]
    public void AllocGrowsByBlockSizeWhenFull()
    {
        // block size 4; alloc 4 trips the n+size >= len condition and grows.
        var v = new ByteVector(4);
        v.Alloc(4);
        Assert.Equal(8, v.Capacity);
        Assert.Equal(4, v.Length);
    }

    [Fact]
    public void TrimToSizeShrinksToLength()
    {
        var v = new ByteVector(16);
        v.Alloc(3);
        v.TrimToSize();
        Assert.Equal(3, v.Capacity);
        Assert.Equal(3, v.Length);
    }

    [Fact]
    public void ConstructFromArrayLeavesLengthZero()
    {
        // Faithful quirk: ByteVector(byte[]) sets length to 0, not array length.
        var v = new ByteVector(new byte[] { 1, 2, 3 });
        Assert.Equal(0, v.Length);
        Assert.Equal(3, v.Capacity);
    }
}

public class CharVectorTests
{
    [Fact]
    public void DefaultCapacityIsBlockSize()
    {
        var v = new CharVector();
        Assert.Equal(2048, v.Capacity);
        Assert.Equal(0, v.Length);
    }

    [Fact]
    public void ConstructFromArraySetsLengthToArrayLength()
    {
        var v = new CharVector(new[] { 'a', 'b', 'c' });
        Assert.Equal(3, v.Length);
        Assert.Equal(3, v.Capacity);
    }

    [Fact]
    public void PutAndGetRoundTrip()
    {
        var v = new CharVector(8);
        v.Alloc(4);
        v.Put(1, 'x');
        Assert.Equal('x', v.Get(1));
    }

    [Fact]
    public void AllocGrowsByBlockSizeWhenFull()
    {
        var v = new CharVector(4);
        v.Alloc(4);
        Assert.Equal(8, v.Capacity);
    }

    [Fact]
    public void ClearResetsLengthButKeepsContents()
    {
        var v = new CharVector(8);
        v.Alloc(2);
        v.Put(0, 'z');
        v.Clear();
        Assert.Equal(0, v.Length);
        Assert.Equal('z', v.Get(0));   // contents preserved
    }

    [Fact]
    public void TrimToSizeShrinksToLength()
    {
        var v = new CharVector(16);
        v.Alloc(5);
        v.TrimToSize();
        Assert.Equal(5, v.Capacity);
    }

    [Fact]
    public void CloneIsDeepCopy()
    {
        var v = new CharVector(8);
        v.Alloc(3);
        v.Put(0, 'a');
        var copy = v.Clone();
        copy.Put(0, 'b');
        Assert.Equal('a', v.Get(0));      // original unaffected
        Assert.Equal('b', copy.Get(0));
        Assert.Equal(v.Length, copy.Length);
    }
}
