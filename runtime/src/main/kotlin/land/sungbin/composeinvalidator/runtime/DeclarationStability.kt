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
  public class Runtime(private val name: String) : DeclarationStability() {
    override fun toString(): String = "Runtime($name)"
  }

  @ComposeInvalidatorCompilerApi
  public class Unknown(private val name: String) : DeclarationStability() {
    override fun toString(): String = "Uncertain($name)"
  }

  @ComposeInvalidatorCompilerApi
  public class Parameter(private val name: String) : DeclarationStability() {
    override fun toString(): String = "Parameter($name)"
  }

  @ComposeInvalidatorCompilerApi
  public class Combined(private vararg val elements: DeclarationStability) : DeclarationStability() {
    override fun toString(): String = elements.asList().joinToString(",")
  }
}
