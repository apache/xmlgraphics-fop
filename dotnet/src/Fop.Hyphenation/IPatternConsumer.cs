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

namespace Fop.Hyphenation;

/// <summary>
/// Connects the XML pattern file parser to the hyphenation tree.
/// <para>
/// Faithful port of the interface <c>org.apache.fop.hyphenation.PatternConsumer</c>.
/// The Java <c>addException</c> took a raw <c>ArrayList</c> of alternating
/// <see cref="string"/> and <see cref="Hyphen"/> instances; that is modelled here
/// as <c>IList&lt;object&gt;</c>.
/// </para>
/// </summary>
public interface IPatternConsumer
{
    /// <summary>
    /// Add a character class. A character class defines characters that are
    /// considered equivalent for the purpose of hyphenation (e.g. "aA"). It
    /// usually means to ignore case.
    /// </summary>
    /// <param name="charGroup">character group.</param>
    void AddClass(string charGroup);

    /// <summary>
    /// Add a hyphenation exception. An exception replaces the result obtained by
    /// the algorithm for cases where it fails or the user wants to provide their
    /// own hyphenation. A hyphenated word is a list of alternating
    /// <see cref="string"/> and <see cref="Hyphen"/> instances.
    /// </summary>
    /// <param name="word">word to add as an exception.</param>
    /// <param name="hyphenatedWord">pre-hyphenated word.</param>
    void AddException(string word, IList<object> hyphenatedWord);

    /// <summary>
    /// Add hyphenation patterns.
    /// </summary>
    /// <param name="pattern">the pattern.</param>
    /// <param name="values">interletter values expressed as a string of digit characters.</param>
    void AddPattern(string pattern, string values);
}
