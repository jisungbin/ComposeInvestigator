/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

// TODO is this really the best approach?
internal class HandledMap {
  // TODO is this operation really O(1)?
  private val map = mutableMapOf<Int, Unit>()

  fun handle(key: Any): Boolean {
    val finalKey = key.hashCode()
    return if (map.containsKey(finalKey)) {
      false
    } else {
      map[finalKey] = Unit
      true
    }
  }

  fun handle(key: Any, key2: Any): Boolean {
    val finalKey = key.hashCode() + key2.hashCode()
    return if (map.containsKey(finalKey)) {
      false
    } else {
      map[finalKey] = Unit
      true
    }
  }

  fun handle(key: Any, key2: Any, key3: Any): Boolean {
    val finalKey = key.hashCode() + key2.hashCode() + key3.hashCode()
    return if (map.containsKey(finalKey)) {
      false
    } else {
      map[finalKey] = Unit
      true
    }
  }
}
