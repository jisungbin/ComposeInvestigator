/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.ComposableFunction
import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.lower.fastForEach
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_NAME_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.argument
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitorVoid
import org.jetbrains.kotlin.name.ClassId

// TODO add top-level usage checkers
public class InvalidationTraceTableApiChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val functionCheckers = setOf(InvalidationTraceTableApiUsageCheck)
  }

  override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
    override val functionCallCheckers = setOf(ComposableNameExpressionChecker)
  }
}

private object InvalidationTraceTableApiUsageCheck : FirFunctionChecker(MppCheckerKind.Common) {
  // TODO we need to fix the real reason for the duplicate diagnostics.
  private val handled = mutableMapOf<KtSourceElement?, MutableSet<AbstractKtDiagnosticFactory>>()

  override fun check(declaration: FirFunction, context: CheckerContext, reporter: DiagnosticReporter) {
    val hasNoInvestigation = context.containingFile!!.hasAnnotation(ClassId.topLevel(NO_INVESTIGATION_FQN), context.session)
    val isComposableScope = when (declaration) {
      is FirAnonymousFunction -> declaration.typeRef.coneType.functionTypeKind(context.session) === ComposableFunction
      else -> declaration.hasComposableAnnotation(context.session)
    }

    val visitor = TraceTableApiAccessVisiter(hasNoInvestigation, isComposableScope, context, reporter)
    declaration.body?.statements?.fastForEach { statement -> statement.accept(visitor) }
  }

  private class TraceTableApiAccessVisiter(
    private val hasNoInvestigation: Boolean,
    @Suppress("unused") private val isComposableScope: Boolean,
    private val context: CheckerContext,
    private val reporter: DiagnosticReporter,
  ) : FirDefaultVisitorVoid() {
    override fun visitElement(element: FirElement) {
      element.acceptChildren(this)
    }

    override fun visitPropertyAccessExpression(expression: FirPropertyAccessExpression) {
      if (
        (expression.dispatchReceiver?.resolvedType ?: expression.extensionReceiver?.resolvedType ?: expression.resolvedType)
          .classId?.asSingleFqName() != COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
      )
        return

      if (hasNoInvestigation) {
        if (handled.getOrPut(expression.source, ::mutableSetOf).add(ComposeInvestigatorErrors.API_ACCESS_IN_NO_INVESTIGATION_FILE))
          reporter.reportOn(expression.source, ComposeInvestigatorErrors.API_ACCESS_IN_NO_INVESTIGATION_FILE, context)
      }

// TODO need to rewrite logic to check if current function is in Composable scope.
//      if (
//        expression.calleeReference
//          .toResolvedPropertySymbol()
//          ?.hasAnnotation(ClassId.topLevel(COMPOSABLE_SCOPE_FQN), context.session)
//        != true
//      )
//        return
//
//      if (!isComposableScope) {
//        if (handled.getOrPut(expression.source, ::mutableSetOf).add(ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_SCOPE_CALL))
//          reporter.reportOn(expression.source, ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_SCOPE_CALL, context)
//      }
    }
  }
}

private object ComposableNameExpressionChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
  override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
    if (expression.resolvedType.classId?.asSingleFqName() != COMPOSABLE_NAME_FQN) return
    if (expression.argument !is FirLiteralExpression) {
      reporter.reportOn(expression.source, ComposeInvestigatorErrors.UNSUPPORTED_COMPOSABLE_NAME, context)
    }
  }
}
