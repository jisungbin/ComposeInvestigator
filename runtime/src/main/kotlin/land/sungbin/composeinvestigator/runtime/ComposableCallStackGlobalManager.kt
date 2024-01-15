/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import java.util.Stack

// TODO: This class must be IR generated.
public object ComposableCallStackGlobalManager {
  private val stacks = Stack<AffectedComposable>()

  public fun enter(parent: AffectedComposable, current: Any): Any =
    try {
      stacks.push(parent)
      current
    } finally {
      stacks.pop()
    }

  public fun current(): Stack<AffectedComposable> = stacks
}
