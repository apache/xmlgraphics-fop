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

namespace Fop.Fo;

/// <summary>
/// A run of character data inside a formatting object. Text inherits its formatting from its
/// parent <see cref="FObj"/>. Port of the role of <c>org.apache.fop.fo.FOText</c>.
/// </summary>
public sealed class FOText : FONode
{
    /// <summary>Creates a text node with the given character data.</summary>
    public FOText(string text)
    {
        Text = text ?? string.Empty;
    }

    /// <summary>The character data.</summary>
    public string Text { get; }

    /// <inheritdoc/>
    public override string LocalName => "#text";

    /// <summary>The property list this text resolves against (its parent's).</summary>
    public PropertyList? Properties => Parent?.Properties;

    /// <inheritdoc/>
    public override string ToString() => $"FOText(\"{Text}\")";
}
