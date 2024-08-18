/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

/**
 * ComposeInvestigator does not work on annotated files or Composable functions.
 *
 * - File: `@file:NoInvestigation`
 * - Composable: `@NoInvestigation @Composable fun MyComposable()`
 *
 * If a file does not contain any Composables, it will automatically become `@file:NoInvestigation`.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
public annotation class NoInvestigation
