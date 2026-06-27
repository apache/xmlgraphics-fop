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

namespace Fop.Util;

/// <summary>
/// Classification of a character for line-breaking and whitespace handling.
/// Ported from the integer character-class constants on the original
/// <c>org.apache.fop.util.CharUtilities</c>.
/// </summary>
public enum CharClass
{
    /// <summary>Unicode white space.</summary>
    UnicodeWhitespace = 0,

    /// <summary>Line feed.</summary>
    LineFeed = 1,

    /// <summary>Boundary between text runs (end of text).</summary>
    Eot = 2,

    /// <summary>Non-whitespace.</summary>
    NonWhitespace = 3,

    /// <summary>XML whitespace.</summary>
    XmlWhitespace = 4,
}
