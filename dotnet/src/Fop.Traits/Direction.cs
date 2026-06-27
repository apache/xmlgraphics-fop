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

namespace Fop.Traits;

/// <summary>
/// Direction traits, namely {inline,block}-progression-direction and shift-direction.
/// <para>
/// Port of <c>org.apache.fop.traits.Direction</c>. The Java typesafe-enum singleton becomes a C#
/// <c>enum</c> whose underlying integer value is exactly the corresponding
/// <c>org.apache.fop.fo.Constants.EN_*</c> constant. The lower-case canonical names ("lr", "rl",
/// "tb", "bt") are provided by <see cref="DirectionExtensions"/>.
/// </para>
/// </summary>
public enum Direction
{
    /// <summary>direction: left-to-right (<c>EN_LR</c>).</summary>
    Lr = 199,

    /// <summary>direction: right-to-left (<c>EN_RL</c>).</summary>
    Rl = 200,

    /// <summary>direction: top-to-bottom (<c>EN_TB</c>).</summary>
    Tb = 201,

    /// <summary>direction: bottom-to-top (<c>EN_BT</c>).</summary>
    Bt = 202,
}

/// <summary>
/// Helpers mirroring the Java <c>Direction</c> instance/static methods.
/// </summary>
public static class DirectionExtensions
{
    // Iteration order matches the Java DIRECTIONS array {LR, RL, TB, BT}.
    private static readonly Direction[] Directions = [Direction.Lr, Direction.Rl, Direction.Tb, Direction.Bt];

    /// <summary>Returns the canonical (lower-case) name of the direction.</summary>
    public static string GetName(this Direction direction) => direction switch
    {
        Direction.Lr => "lr",
        Direction.Rl => "rl",
        Direction.Tb => "tb",
        Direction.Bt => "bt",
        _ => throw new ArgumentOutOfRangeException(nameof(direction)),
    };

    /// <summary>Returns the enumeration value (one of the <c>EN_*</c> integers).</summary>
    public static int GetEnumValue(this Direction direction) => (int)direction;

    /// <summary>Determines whether the direction is vertical (top-to-bottom or bottom-to-top).</summary>
    public static bool IsVertical(this Direction direction) =>
        direction == Direction.Tb || direction == Direction.Bt;

    /// <summary>Determines whether the direction is horizontal (left-to-right or right-to-left).</summary>
    public static bool IsHorizontal(this Direction direction) =>
        direction == Direction.Lr || direction == Direction.Rl;

    /// <summary>
    /// Returns the enumeration object based on its name (case-insensitive).
    /// </summary>
    /// <exception cref="ArgumentException">if the name is not a legal direction.</exception>
    public static Direction ValueOf(string name)
    {
        foreach (Direction direction in Directions)
        {
            if (string.Equals(direction.GetName(), name, StringComparison.OrdinalIgnoreCase))
            {
                return direction;
            }
        }

        throw new ArgumentException("Illegal direction: " + name);
    }

    /// <summary>
    /// Returns the enumeration object based on its enumeration (<c>EN_*</c>) value.
    /// </summary>
    /// <exception cref="ArgumentException">if the value is not a legal direction.</exception>
    public static Direction ValueOf(int enumValue)
    {
        foreach (Direction direction in Directions)
        {
            if (direction.GetEnumValue() == enumValue)
            {
                return direction;
            }
        }

        throw new ArgumentException("Illegal direction: " + enumValue);
    }
}
