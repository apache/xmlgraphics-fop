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

namespace Fop.DataTypes;

/// <summary>
/// A simple lookup context for a single percent base value.
/// <para>Port of <c>org.apache.fop.datatypes.SimplePercentBaseContext</c>.</para>
/// </summary>
public class SimplePercentBaseContext : IPercentBaseContext
{
    private readonly IPercentBaseContext? parentContext;
    private readonly int lengthBase;
    private readonly int lengthBaseValue;

    /// <summary>Creates a new simple percent base context.</summary>
    /// <param name="parentContext">The context to be used for all percentages other than <paramref name="lengthBase"/>.</param>
    /// <param name="lengthBase">The particular percentage length base for which this context provides a value.</param>
    /// <param name="lengthBaseValue">The value to be returned for requests to the given <paramref name="lengthBase"/>.</param>
    public SimplePercentBaseContext(IPercentBaseContext? parentContext, int lengthBase, int lengthBaseValue)
    {
        this.parentContext = parentContext;
        this.lengthBase = lengthBase;
        this.lengthBaseValue = lengthBaseValue;
    }

    /// <summary>Returns the value for the given length base.</summary>
    public int GetBaseLength(int lengthBase, object? fobj)
    {
        // If it is for us return our value, otherwise delegate to the parent context.
        if (lengthBase == this.lengthBase)
        {
            return lengthBaseValue;
        }

        if (parentContext != null)
        {
            return parentContext.GetBaseLength(lengthBase, fobj);
        }

        return -1;
    }
}
