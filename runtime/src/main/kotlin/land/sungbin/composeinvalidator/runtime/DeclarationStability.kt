/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.runtime

@ComposeInvalidatorCompilerApi
public sealed class DeclarationStability {
  @ComposeInvalidatorCompilerApi
  public class Certain(private val stable: Boolean) : DeclarationStability() {
    override fun toString(): String = if (stable) "Stable" else "Unstable"
  }

  @ComposeInvalidatorCompilerApi
  public class Runtime(private val declarationName: String) : DeclarationStability() {
    override fun toString(): String = "Runtime($declarationName)"
  }

  @ComposeInvalidatorCompilerApi
  public class Unknown(private val declarationName: String) : DeclarationStability() {
    override fun toString(): String = "Uncertain($declarationName)"
  }

  @ComposeInvalidatorCompilerApi
  public class Parameter(private val parameterName: String) : DeclarationStability() {
    override fun toString(): String = "Parameter($parameterName)"
  }

  @ComposeInvalidatorCompilerApi
  public class Combined(private val elements: List<DeclarationStability>) : DeclarationStability() {
    override fun toString(): String = elements.joinToString(",")
  }
}