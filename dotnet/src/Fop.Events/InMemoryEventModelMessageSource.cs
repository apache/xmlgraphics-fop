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

namespace Fop.Events;

/// <summary>
/// A simple, in-memory <see cref="IEventModelMessageSource"/>. Templates are registered per group
/// and key; culture is currently ignored (all cultures resolve to the same registered template),
/// which mirrors the single-locale usage exercised by the tests while leaving the culture argument
/// in place for future localization.
/// <para>
/// This replaces the Java XML resource bundles. The Java <c>EventFormatter</c> used a <c>null</c>
/// group id to mean "use the default bundle"; here a <c>null</c> group id maps to the same
/// dedicated default group.
/// </para>
/// </summary>
public sealed class InMemoryEventModelMessageSource : IEventModelMessageSource
{
    private const string DefaultGroup = "\0default";

    private readonly Dictionary<string, Dictionary<string, string>> groups = new();

    /// <summary>Registers (or overwrites) a template for the given group and key.</summary>
    /// <param name="groupId">The event group id (or <c>null</c> for the default group).</param>
    /// <param name="key">The lookup key.</param>
    /// <param name="template">The template string.</param>
    /// <returns>This instance, to allow fluent registration.</returns>
    public InMemoryEventModelMessageSource Add(string? groupId, string key, string template)
    {
        ArgumentNullException.ThrowIfNull(key);
        ArgumentNullException.ThrowIfNull(template);
        string group = groupId ?? DefaultGroup;
        if (!groups.TryGetValue(group, out var bundle))
        {
            bundle = new Dictionary<string, string>();
            groups[group] = bundle;
        }
        bundle[key] = template;
        return this;
    }

    /// <inheritdoc/>
    public string? GetTemplate(string? groupId, string key, CultureInfo? culture)
    {
        ArgumentNullException.ThrowIfNull(key);
        string group = groupId ?? DefaultGroup;
        return groups.TryGetValue(group, out var bundle) && bundle.TryGetValue(key, out var template)
            ? template
            : null;
    }
}
