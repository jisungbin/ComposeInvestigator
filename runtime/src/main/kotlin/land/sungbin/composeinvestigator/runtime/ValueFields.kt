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
