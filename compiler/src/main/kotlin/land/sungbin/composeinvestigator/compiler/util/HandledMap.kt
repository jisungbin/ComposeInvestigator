/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

internal class HandledMap {
  private val map = mutableMapOf<Int, Unit>()

  fun handle(vararg key: Any): Boolean {
    val finalKey = key.contentHashCode()
    return if (map.containsKey(finalKey)) {
      false
    } else {
      map[finalKey] = Unit
      true
    }
  }
}
