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

using System.Globalization;
using System.Text;
using Fop.Util.Text;

namespace Fop.Events;

/// <summary>
/// A message part that looks up another template by a key taken from a field's value, resolving it
/// against the same message source/group as the surrounding event.
/// <para>Port of the nested <c>EventFormatter.LookupFieldPart</c>.</para>
/// </summary>
internal sealed class LookupFieldPart(string fieldName) : IPart
{
    private readonly string fieldName = fieldName;

    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters) => GetKey(parameters) != null;

    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        string? groupId = parameters.TryGetValue("groupID", out var g) ? g as string : null;
        CultureInfo? culture = parameters.TryGetValue("locale", out var l) ? l as CultureInfo : null;
        string? key = GetKey(parameters);
        if (key != null)
        {
            string? template = EventFormatter.MessageSource.GetTemplate(groupId, key, culture);
            if (template != null)
            {
                sb.Append(template);
            }
        }
    }

    private string? GetKey(IReadOnlyDictionary<string, object?> parameters) =>
        parameters.TryGetValue(fieldName, out var value) ? value as string : null;

    public override string ToString() => "{" + fieldName + ", lookup}";
}

/// <summary>
/// Part factory for lookups (the <c>lookup</c> format).
/// <para>Port of the nested <c>EventFormatter.LookupFieldPartFactory</c>.</para>
/// </summary>
public sealed class LookupFieldPartFactory : IPartFactory
{
    /// <inheritdoc/>
    public IPart NewPart(string fieldName, string? values) => new LookupFieldPart(fieldName);

    /// <inheritdoc/>
    public string Format => "lookup";
}
