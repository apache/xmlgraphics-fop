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

namespace Fop.Util.Text;

/// <summary>
/// A part composed of child parts. It models conditional sub-groups (<c>[...]</c>) and
/// multi-section choices separated by <c>|</c>. Port of the nested
/// <c>AdvancedMessageFormat.CompositePart</c>.
/// </summary>
internal sealed class CompositePart : IPart
{
    private List<IPart> parts = new();
    private readonly bool conditional;
    private bool hasSections;

    public CompositePart(bool conditional) => this.conditional = conditional;

    private CompositePart(List<IPart> parts)
    {
        this.parts.AddRange(parts);
        conditional = true;
    }

    public void AddChild(IPart part)
    {
        ArgumentNullException.ThrowIfNull(part, "part");
        if (hasSections)
        {
            var composite = (CompositePart)parts[^1];
            composite.AddChild(part);
        }
        else
        {
            parts.Add(part);
        }
    }

    public void NewSection()
    {
        if (!hasSections)
        {
            List<IPart> p = parts;
            // Dropping into a different mode...
            parts = new List<IPart> { new CompositePart(p) };
            hasSections = true;
        }
        parts.Add(new CompositePart(true));
    }

    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        if (hasSections)
        {
            foreach (var part in parts)
            {
                if (part.IsGenerated(parameters))
                {
                    part.Write(sb, parameters);
                    break;
                }
            }
        }
        else
        {
            if (IsGenerated(parameters))
            {
                foreach (var part in parts)
                {
                    part.Write(sb, parameters);
                }
            }
        }
    }

    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters)
    {
        if (hasSections)
        {
            foreach (var part in parts)
            {
                if (part.IsGenerated(parameters))
                {
                    return true;
                }
            }
            return false;
        }

        if (conditional)
        {
            foreach (var part in parts)
            {
                if (!part.IsGenerated(parameters))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public override string ToString() => "[" + string.Join(", ", parts) + "]";
}
