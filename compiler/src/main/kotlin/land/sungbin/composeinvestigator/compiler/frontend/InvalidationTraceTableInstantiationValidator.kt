/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.k2.isComposable
import androidx.compose.compiler.plugins.kotlin.lower.fastForEach
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.validate
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.smartPlus
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitorVoid
import org.jetbrains.kotlin.name.ClassId

public class InvalidationTraceTableInstantiationValidator(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val fileCheckers: Set<FirFileChecker> = setOf(NoComposableFileChecker)
  }
}

private object NoComposableFileChecker : FirFileChecker(MppCheckerKind.Common) {
  private val NO_INVESTIGATION = ClassId.topLevel(NO_INVESTIGATION_FQN)
  private val noInvestigationType by unsafeLazy {
    buildResolvedTypeRef {
      coneType = NO_INVESTIGATION.constructClassLikeType()
    }
  }

  override fun check(declaration: FirFile, context: CheckerContext, reporter: DiagnosticReporter) {
    if (declaration.hasAnnotation(NO_INVESTIGATION, context.session)) return
    var hasComposable = false

    val composableCallVisitor = object : FirDefaultVisitorVoid() {
      override fun visitElement(element: FirElement) {
        if (hasComposable) return
        element.acceptChildren(this)
      }

      override fun visitFunctionCall(call: FirFunctionCall) {
        if (call.calleeReference.toResolvedCallableSymbol()!!.isComposable(context.session))
          hasComposable = true

        if (hasComposable) return

        super.visitFunctionCall(call)
      }
    }

    declaration.declarations.fastForEach { element ->
      // fast path -- 1
      if (element.hasComposableAnnotation(context.session))
        return@check // early return if the file has composable functions

      if (element is FirFunction) // fast path -- 2
        element.body?.acceptChildren(composableCallVisitor)
      else // slow path
        element.acceptChildren(composableCallVisitor)
    }

    if (hasComposable) return

    val noInvestigationAnnotation = buildAnnotation {
      useSiteTarget = AnnotationUseSiteTarget.FILE
      annotationTypeRef = noInvestigationType
      argumentMapping = FirEmptyAnnotationArgumentMapping
    }

    declaration.replaceAnnotations(declaration.annotations.smartPlus(listOf(noInvestigationAnnotation)))

    // Validate that the new annotations do not break the file.
    declaration.validate()
  }
}
