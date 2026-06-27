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

namespace Fop.Util;

/// <summary>
/// Provides helper functions for <see cref="IList{T}"/>.
/// <para>Port of <c>org.apache.fop.util.ListUtil</c>.</para>
/// </summary>
/// <remarks>
/// The Java original also exposes <c>getLastListElement(List)</c>, which depends on
/// <c>org.apache.fop.layoutmgr.ListElement</c>. That type belongs to the (not yet ported)
/// layout-manager subsystem, so the helper is intentionally omitted here.
/// </remarks>
public static class ListUtil
{
    /// <summary>
    /// Retrieves the last element from a list.
    /// </summary>
    /// <typeparam name="T">the type of objects stored in the list.</typeparam>
    /// <param name="list">the list to work on.</param>
    /// <returns>the last element.</returns>
    public static T GetLast<T>(IList<T> list)
    {
        ArgumentNullException.ThrowIfNull(list);
        return list[list.Count - 1];
    }

    /// <summary>
    /// Retrieves and removes the last element from a list.
    /// </summary>
    /// <typeparam name="T">the type of objects stored in the list.</typeparam>
    /// <param name="list">the list to work on.</param>
    /// <returns>the previous last element.</returns>
    public static T RemoveLast<T>(IList<T> list)
    {
        ArgumentNullException.ThrowIfNull(list);
        int lastIndex = list.Count - 1;
        T last = list[lastIndex];
        list.RemoveAt(lastIndex);
        return last;
    }
}
