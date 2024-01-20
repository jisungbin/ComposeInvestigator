/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.util

// putIfAbsent is available from API 24 (project minSdk is 21)
internal fun <K, V> MutableMap<K, V>.putIfNotPresent(key: K, value: V) {
  if (!containsKey(key)) put(key, value)
}
