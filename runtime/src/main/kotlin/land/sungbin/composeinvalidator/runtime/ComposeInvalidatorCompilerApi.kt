/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.runtime

@RequiresOptIn(
  "This API is designed for use with the ComposeInvalidator compiler only; " +
    "manual use may cause undesired results.",
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class ComposeInvalidatorCompilerApi
