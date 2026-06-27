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
/// Base class for all nodes in the formatting-object tree (formatting objects and text).
/// Port of the role of <c>org.apache.fop.fo.FONode</c>, simplified for the current pipeline.
/// </summary>
public abstract class FONode
{
    /// <summary>The parent formatting object, or <c>null</c> for the root.</summary>
    public FObj? Parent { get; internal set; }

    /// <summary>The local (unprefixed) element name, or a synthetic name for text nodes.</summary>
    public abstract string LocalName { get; }
}
