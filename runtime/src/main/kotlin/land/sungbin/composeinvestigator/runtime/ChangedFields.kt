/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Indicates which Composables were affected by the state change.
 *
 * @param name Composable function name
 * @param packageName Package name that the Composable function is defined
 * @param fileName File name that the Composable function is defined
 */
@Immutable
public data class ComposableInformation(
  public val name: String,
  public val packageName: String,
  public val fileName: String,
)

/** Fully-qualified name of the Composable function. */
public val ComposableInformation.fqPackageName: String
  @Stable get() = packageName.takeUnless(String::isEmpty)?.plus(".").orEmpty() + name

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
