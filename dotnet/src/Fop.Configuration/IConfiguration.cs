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
/// A tree of configuration values, backed by an XML element. Provides typed,
/// defaulting accessors for child elements, attributes and element values.
/// <para>Port of <c>org.apache.fop.configuration.Configuration</c>.</para>
/// <para>
/// The accessor method names are kept as in the Java original (rather than
/// converting to properties) because they form parameterised accessor families
/// (<c>GetAttribute</c>, <c>GetChild</c>, <c>GetValueAs*</c>) whose defaulting
/// and exception semantics must be preserved exactly.
/// </para>
/// </summary>
public interface IConfiguration
{
    /// <summary>
    /// Returns the first child element with the given name, or the null
    /// configuration if none exists.
    /// </summary>
    IConfiguration GetChild(string key);

    /// <summary>
    /// Returns the first child element with the given name. If <paramref name="required"/>
    /// is <c>false</c> and no such child exists, returns <c>null</c>; otherwise returns
    /// the null configuration when missing.
    /// </summary>
    IConfiguration? GetChild(string key, bool required);

    /// <summary>Returns all direct child elements with the given name.</summary>
    IConfiguration[] GetChildren(string key);

    /// <summary>Returns the names of all attributes on this element.</summary>
    string[] GetAttributeNames();

    /// <summary>Returns the value of the named attribute.</summary>
    /// <exception cref="ConfigurationException">If the attribute is missing.</exception>
    string? GetAttribute(string key);

    /// <summary>Returns the value of the named attribute, or the default value if absent.</summary>
    string GetAttribute(string key, string defaultValue);

    /// <summary>Returns the named attribute interpreted as a boolean, or the default if absent.</summary>
    bool GetAttributeAsBoolean(string key, bool defaultValue);

    /// <summary>Returns the named attribute interpreted as a float.</summary>
    /// <exception cref="ConfigurationException">If the attribute is missing or not a float.</exception>
    float GetAttributeAsFloat(string key);

    /// <summary>Returns the named attribute interpreted as a float, or the default if absent.</summary>
    float GetAttributeAsFloat(string key, float defaultValue);

    /// <summary>Returns the named attribute interpreted as an integer, or the default if absent.</summary>
    int GetAttributeAsInteger(string key, int defaultValue);

    /// <summary>Returns the text value of this element.</summary>
    /// <exception cref="ConfigurationException">If there is no value.</exception>
    string GetValue();

    /// <summary>Returns the text value of this element, or the default if empty.</summary>
    string GetValue(string defaultValue);

    /// <summary>Returns the text value of this element interpreted as a boolean.</summary>
    /// <exception cref="ConfigurationException">If there is no value.</exception>
    bool GetValueAsBoolean();

    /// <summary>Returns the text value interpreted as a boolean, or the default if empty.</summary>
    bool GetValueAsBoolean(bool defaultValue);

    /// <summary>Returns the text value of this element interpreted as an integer.</summary>
    /// <exception cref="ConfigurationException">If there is no value or it is not an integer.</exception>
    int GetValueAsInteger();

    /// <summary>Returns the text value interpreted as an integer, or the default if empty.</summary>
    int GetValueAsInteger(int defaultValue);

    /// <summary>Returns the text value of this element interpreted as a float.</summary>
    /// <exception cref="ConfigurationException">If there is no value or it is not a float.</exception>
    float GetValueAsFloat();

    /// <summary>Returns the text value interpreted as a float, or the default if empty.</summary>
    float GetValueAsFloat(float defaultValue);

    /// <summary>Returns a slash-separated path describing this element's location in the tree.</summary>
    string GetLocation();
}
