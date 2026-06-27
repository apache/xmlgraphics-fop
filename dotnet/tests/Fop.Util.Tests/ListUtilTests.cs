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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class ListUtilTests
{
    [Fact]
    public void GetLast_ReturnsLastElement()
        => Assert.Equal(3, ListUtil.GetLast(new List<int> { 1, 2, 3 }));

    [Fact]
    public void GetLast_DoesNotModifyList()
    {
        var list = new List<string> { "a", "b" };
        Assert.Equal("b", ListUtil.GetLast(list));
        Assert.Equal(2, list.Count);
    }

    [Fact]
    public void RemoveLast_ReturnsAndRemovesLastElement()
    {
        var list = new List<int> { 1, 2, 3 };
        Assert.Equal(3, ListUtil.RemoveLast(list));
        Assert.Equal([1, 2], list);
    }

    [Fact]
    public void GetLast_OnEmptyList_Throws()
        => Assert.Throws<ArgumentOutOfRangeException>(() => ListUtil.GetLast(new List<int>()));

    [Fact]
    public void GetLast_Null_Throws()
        => Assert.Throws<ArgumentNullException>(() => ListUtil.GetLast<int>(null!));
}
