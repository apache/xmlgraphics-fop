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
/// Supplies the per-event message templates that <see cref="EventFormatter"/> renders.
/// <para>
/// The Java <c>EventFormatter</c> loaded these templates from XML resource bundles
/// (<c>org.apache.fop.utils.XMLResourceBundle</c>), keyed by the event group id and looked up by
/// the event key. Those bundle files are not part of this port, so this pluggable abstraction takes
/// their place: an implementation maps a (group id, key, culture) triple to a template string.
/// </para>
/// </summary>
public interface IEventModelMessageSource
{
    /// <summary>
    /// Returns the template registered for the given key within the given group, or <c>null</c> if
    /// no such template exists.
    /// </summary>
    /// <param name="groupId">
    /// The event group id (may be <c>null</c> to denote the default/anonymous group).
    /// </param>
    /// <param name="key">The lookup key (typically the event key, or an include reference).</param>
    /// <param name="culture">The culture to resolve the template for (may be <c>null</c>).</param>
    /// <returns>The template string, or <c>null</c> if not found.</returns>
    string? GetTemplate(string? groupId, string key, CultureInfo? culture);
}
