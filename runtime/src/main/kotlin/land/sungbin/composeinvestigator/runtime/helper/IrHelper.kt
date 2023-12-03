/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.helper

import androidx.compose.runtime.State
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

// **This function is for the ComposeInvestigator compiler only.** It should be replaced by an IR,
// but due to the risk and complexity of writing a custom IR, we replace it with a runtime function.
@ComposeInvestigatorCompilerApi
public fun <T> State<T>.obtainStatePropertyAndAdd(
  propertyName: String,
  destination: MutableList<AffectedField>,
): State<T> = also { state ->
  destination.add(
    AffectedField.StateProperty(
      name = propertyName,
      valueString = state.value.toString(),
      valueHashCode = state.value.hashCode(),
    ),
  )
}
