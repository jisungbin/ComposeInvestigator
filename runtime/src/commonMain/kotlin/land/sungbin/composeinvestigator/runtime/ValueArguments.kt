// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

/**
 * Represents information about the Composable function's value argument.
 *
 * @param name Argument name
 * @param type Fully-qualified name of the argument type
 * @param valueString String representation of the argument value
 * @param valueHashCode Hash code of the argument value
 * @param stability [Stability] information for the argument type
 */
public data class ValueArgument(
  public val name: String,
  public val type: String,
  public val valueString: String,
  public val valueHashCode: Int,
  public val stability: Stability,
)

/**
 * Indicates which arguments have changed as a result of the recomposition.
 *
 * @param previous Argument before recomposition
 * @param new Argument after recomposition
 */
public data class ChangedArgument(public val previous: ValueArgument, public val new: ValueArgument) {
  init {
    require(previous.name == new.name) {
      "Affected arguments must be same name. (previous=${previous.name}, new=${new.name})"
    }
  }
}

internal infix fun ValueArgument.changedTo(new: ValueArgument): ChangedArgument =
  ChangedArgument(previous = this, new = new)
