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

namespace Fop.Util.Text;

/// <summary>
/// Reports a position within a source document, mirroring the subset of the SAX
/// <c>org.xml.sax.Locator</c> interface that <see cref="LocatorFormatter"/> needs.
/// <para>
/// FOP's full SAX <c>Locator</c> has not been ported yet; this minimal abstraction lets
/// <see cref="LocatorFormatter"/> render a <c>line:column</c> position without depending on a SAX
/// stack.
/// </para>
/// </summary>
public interface ILocator
{
    /// <summary>The line number of the current position (1-based), or -1 if unknown.</summary>
    int LineNumber { get; }

    /// <summary>The column number of the current position (1-based), or -1 if unknown.</summary>
    int ColumnNumber { get; }
}
