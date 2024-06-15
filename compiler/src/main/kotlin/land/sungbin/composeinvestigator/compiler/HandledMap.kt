/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

// TODO is this really the best approach?
public class HandledMap {
  private val map = mutableMapOf<Int, Unit>()

  public fun handle(vararg key: Any): Boolean {
    val finalKey = key.contentHashCode()
    return if (map.containsKey(finalKey)) {
      false
    } else {
      map[finalKey] = Unit
      true
    }
  }
}
