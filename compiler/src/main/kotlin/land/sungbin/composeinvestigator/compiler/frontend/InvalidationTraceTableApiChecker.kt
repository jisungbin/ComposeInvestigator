// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.ComposableFunction
import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.InvestigatorFqNames
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.findClosest
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.unreachableBranch

/**
 * Checker to validate use cases of the `ComposeInvestigator` runtime API.
 *
 * - [ComposeInvestigatorApiCallChecker]: Using an API annotated with `@ComposableScope` outside
 * of a Composable function, or using the `ComposeInvestigator` runtime API in a file that does
 * not create a `ComposeInvestigator`, will raise an error.
 */
public class InvalidationTraceTableApiChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
    override val propertyAccessExpressionCheckers = setOf(ComposeInvestigatorApiCallChecker)
  }
}

private object ComposeInvestigatorApiCallChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {
  private val NO_INVESTIGATION = ClassId.topLevel(InvestigatorFqNames.NoInvestigation)
  private val COMPOSABLE_SCOPE = ClassId.topLevel(InvestigatorFqNames.ComposableScope)

  override fun check(expression: FirPropertyAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {
    // TODO When accessed by `it`, such as `traceTable.let { it.action() }`,
    //  `Symbol` is `FirValueParameterSymbol`. These variants need to be handled separately.
    val callee = expression.calleeReference.toResolvedPropertySymbol() ?: return

    if (
      callee.callableId.asSingleFqName() == InvestigatorFqNames.currentComposeInvestigator &&
      context.isNoInvestigationFile()
    )
      reporter.reportOn(expression.source, ComposeInvestigatorErrors.ILLEGAL_API_ACCESS_IN_NO_INVESTIGATION_FILE, context)

    checkComposableScopeCall(expression, callee, context, reporter)
  }

  // FIXME The annotation for callee was not being looked up correctly.
  private fun checkComposableScopeCall(
    expression: FirPropertyAccessExpression,
    callee: FirPropertySymbol,
    context: CheckerContext,
    reporter: DiagnosticReporter,
  ) {
    if (
      !callee.hasAnnotation(COMPOSABLE_SCOPE, context.session) &&
      callee.getterSymbol?.hasAnnotation(COMPOSABLE_SCOPE, context.session) != true &&
      callee.setterSymbol?.hasAnnotation(COMPOSABLE_SCOPE, context.session) != true
    )
      return

    if (!context.isComposableScope())
      reporter.reportOn(expression.source, ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_SCOPE_API_ACCESS, context)
  }

  private fun CheckerContext.isNoInvestigationFile() =
    containingFile!!.hasAnnotation(NO_INVESTIGATION, session)

  fun CheckerContext.isComposableScope(): Boolean =
    when (val declaration = findClosest<FirFunction>() ?: return false) {
      is FirAnonymousFunction -> declaration.typeRef.coneType.functionTypeKind(session) === ComposableFunction
      is FirSimpleFunction -> declaration.hasComposableAnnotation(session)
      else -> unreachableBranch(declaration::class.simpleName)
    }
}
