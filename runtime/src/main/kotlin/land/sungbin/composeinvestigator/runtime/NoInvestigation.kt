/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

/**
 * ComposeInvestigator does not work on annotated files or composable functions.
 *
 * file: `@file:NoInvestigation`
 * composable: `@NoInvestigation @Composable fun MyComposable()`
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
public annotation class NoInvestigation
