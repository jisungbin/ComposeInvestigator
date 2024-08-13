/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable

/**
 * Represents information about the Composable function's value parameter.
 *
 * @param name Parameter name
 * @param type Fully-qualified name of the parameter type
 * @param stability [Stability] information for the parameter type
 */
@Immutable
public data class ValueParameter(
  public val name: String,
  public val type: String,
  public val stability: Stability,
)

/**
 * Represents information about the Composable function's value argument.
 *
 * @param name Argument name
 * @param type Fully-qualified name of the argument type
 * @param valueString String representation of the argument value
 * @param valueHashCode Hash code of the argument value
 * @param stability [Stability] information for the argument type
 */
@Immutable
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
@Immutable
public data class ChangedArgument(public val previous: ValueArgument, public val new: ValueArgument) {
  init {
    require(previous.javaClass.name == new.javaClass.name) {
      "Affected arguments must be same type. previous=${previous.javaClass.name}, new=${new.javaClass.name}"
    }
  }
}

internal infix fun ValueArgument.changedTo(new: ValueArgument): ChangedArgument =
  ChangedArgument(previous = this, new = new)
