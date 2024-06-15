/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

/**
 * ComposeInvestigator does not work on annotated (1) files,
 * (2) composable functions, or (3, 4) state variables.
 *
 * 1. `@file:NoInvestigation`
 * 2. `@NoInvestigation @Composable fun MyComposable()`
 * 3. `@NoInvestigation val myState = mutableStateOf(0)`
 * 4. `@NoInvestigation val myState by mutableStateOf(0)`
 *
 * If a composable function is annotated with this, state variable tracing
 * in the function body is also disabled. If you want to disable tracing
 * for a composable function, but want to enable tracing of state variables
 * in the body of that function, you must enable it directly with the
 * [registerStateObjectTracing] API.
 *
 * ```
 * @Composable @NoInvestigation fun MyComposable() {
 *   val state = remember { mutableStateOf(1).registerStateObjectTracing() }
 *   // Or, you can use delegation.
 *   val state2 by remember { mutableStateOf(1).registerStateObjectTracing() }
 * }
 * ```
 *
 * Callstack tracing always works regardless of this annotation, meaning that
 * even if a composable in the middle of a callstack is annotated with [NoInvestigation],
 * the final callstack will still show the middle composable without loss.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE)
public annotation class NoInvestigation
