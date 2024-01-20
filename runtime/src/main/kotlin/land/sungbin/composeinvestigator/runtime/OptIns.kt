/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

@RequiresOptIn(
  "This API is designed for use with the ComposeInvestigator compiler only; " +
    "manual use may cause undesired results.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
public annotation class ComposeInvestigatorCompilerApi

@RequiresOptIn(
  "This is an experimental API for ComposeInvestigator and is likely to change" +
    "before becoming stable.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalComposeInvestigatorApi
