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

namespace Fop.Configuration;

/// <summary>
/// Null-object implementation of <see cref="IConfiguration"/> returned in place of a
/// missing element. Every accessor yields an empty/default result instead of failing,
/// except <see cref="GetValue()"/> which throws (matching the Java original).
/// <para>Port of <c>org.apache.fop.configuration.NullConfiguration</c>.</para>
/// </summary>
internal sealed class NullConfiguration : IConfiguration
{
    /// <summary>The singleton instance.</summary>
    internal static readonly NullConfiguration Instance = new();

    private NullConfiguration()
    {
    }

    public IConfiguration GetChild(string key) => Instance;

    public IConfiguration? GetChild(string key, bool required) => Instance;

    public IConfiguration[] GetChildren(string key) => [];

    public string[] GetAttributeNames() => [];

    // NOTE: Unlike DefaultConfiguration (which returns null for a missing attribute),
    // the Java NullConfiguration returns the empty string here. Preserved verbatim.
    public string? GetAttribute(string key) => "";

    public string GetAttribute(string key, string defaultValue) => defaultValue;

    public bool GetAttributeAsBoolean(string key, bool defaultValue) => defaultValue;

    public float GetAttributeAsFloat(string key) => 0;

    public float GetAttributeAsFloat(string key, float defaultValue) => defaultValue;

    public int GetAttributeAsInteger(string key, int defaultValue) => defaultValue;

    public string GetValue() => throw new ConfigurationException("missing value");

    public string GetValue(string defaultValue) => defaultValue;

    public bool GetValueAsBoolean() => false;

    public bool GetValueAsBoolean(bool defaultValue) => defaultValue;

    public int GetValueAsInteger() => 0;

    public int GetValueAsInteger(int defaultValue) => defaultValue;

    public float GetValueAsFloat() => 0;

    public float GetValueAsFloat(float defaultValue) => defaultValue;

    public string GetLocation() => "<no-location>";
}
