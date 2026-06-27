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
/// Base class for formatting objects: a node with resolved properties and child nodes.
/// Port of the role of <c>org.apache.fop.fo.FObj</c>, simplified for the current pipeline.
/// </summary>
public abstract class FObj : FONode
{
    private readonly List<FONode> children = new();

    /// <summary>Creates a formatting object with the given resolved property list.</summary>
    protected FObj(PropertyList properties)
    {
        Properties = properties ?? throw new ArgumentNullException(nameof(properties));
    }

    /// <summary>The resolved property list for this object.</summary>
    public PropertyList Properties { get; }

    /// <summary>The child nodes (formatting objects and text), in document order.</summary>
    public IReadOnlyList<FONode> Children => children;

    /// <summary>Appends a child node and sets its parent.</summary>
    public void AddChild(FONode child)
    {
        ArgumentNullException.ThrowIfNull(child);
        child.Parent = this;
        children.Add(child);
    }

    /// <summary>Enumerates the direct children that are formatting objects.</summary>
    public IEnumerable<FObj> ChildObjects => children.OfType<FObj>();

    // ----- Convenience accessors over the resolved property list -----

    /// <summary>The resolved font family.</summary>
    public string FontFamily => Properties.FontFamily;

    /// <summary>The resolved font size in millipoints.</summary>
    public double FontSizeMpt => Properties.FontSizeMpt;

    /// <summary>The resolved numeric font weight (100-900).</summary>
    public int FontWeight => Properties.FontWeight;

    /// <summary>The resolved font style.</summary>
    public FontStyle FontStyle => Properties.FontStyle;

    /// <summary>The resolved text alignment.</summary>
    public TextAlign TextAlign => Properties.TextAlign;

    /// <summary>The resolved line height in millipoints.</summary>
    public double LineHeightMpt => Properties.LineHeightMpt;

    /// <inheritdoc/>
    public override string ToString() => $"{GetType().Name}({LocalName})";
}
