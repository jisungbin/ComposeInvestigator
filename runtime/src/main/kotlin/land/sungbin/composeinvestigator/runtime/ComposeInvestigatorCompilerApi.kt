/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

@RequiresOptIn(
  "This API is designed for use with the ComposeInvestigator compiler only; " +
    "manual use may cause undesired results.",
)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
public annotation class ComposeInvestigatorCompilerApi
