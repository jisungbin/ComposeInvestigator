/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.runtime

public sealed class Stability {
  public class Certain(private val stable: Boolean) : Stability() {
    override fun toString(): String = if (stable) "Stable" else "Unstable"
  }

  public class Runtime(private val declarationName: String) : Stability() {
    override fun toString(): String = "Runtime($declarationName)"
  }

  public class Unknown(private val declarationName: String) : Stability() {
    override fun toString(): String = "Uncertain($declarationName)"
  }

  public class Parameter(private val parameterName: String) : Stability() {
    override fun toString(): String = "Parameter($parameterName)"
  }

  public class Combined(private val elements: List<Stability>) : Stability() {
    override fun toString(): String = elements.joinToString(",")
  }
}