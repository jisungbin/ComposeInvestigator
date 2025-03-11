// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.k2.isComposable
import androidx.compose.compiler.plugins.kotlin.lower.fastForEach
import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.smartPlus
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitorVoid
import org.jetbrains.kotlin.name.ClassId

/**
 * Transformer that adds an `@file:NoInvestigation` annotation to the file if the file
 * does not have a Composable function.
 *
 * As a result, files with `@file:NoInvestigation` will *not* have a `ComposeInvestigator`
 * instantiated.
 */
public class InvalidationTraceTableInstantiationValidator(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val fileCheckers: Set<FirFileChecker> = setOf(NoComposableFileChecker)
  }
}

// TODO `@file:NoInvestigation` should also be added when all Composable
//  functions are `@NoInvestigation`.
private object NoComposableFileChecker : FirFileChecker(MppCheckerKind.Common) {
  private val NO_INVESTIGATION = ClassId.topLevel(InvestigatorNames.noInvestigation)
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

        if (element is FirAnnotationContainer)
          visitAnnotationContainer(element)
        else
          element.acceptChildren(this)
      }

      override fun visitAnnotationContainer(container: FirAnnotationContainer) {
        if (container.hasComposableAnnotation(context.session))
          hasComposable = true

        if (hasComposable) return

        container.acceptChildren(this)
      }

      override fun visitFunctionCall(call: FirFunctionCall) {
        if (call.calleeReference.toResolvedCallableSymbol()!!.isComposable(context.session))
          hasComposable = true

        if (hasComposable) return

        call.acceptChildren(this)
      }
    }

    declaration.declarations.fastForEach { element ->
      // fast path
      if (element.hasComposableAnnotation(context.session))
        return@check // early return if the file has composable functions

      // slow path
      element.acceptChildren(composableCallVisitor)
    }

    if (hasComposable) return

    val noInvestigationAnnotation = buildAnnotation {
      useSiteTarget = AnnotationUseSiteTarget.FILE
      annotationTypeRef = noInvestigationType
      argumentMapping = FirEmptyAnnotationArgumentMapping
    }

    declaration.replaceAnnotations(declaration.annotations.smartPlus(listOf(noInvestigationAnnotation)))

    // TODO Validate that the new annotations do not break the file. Blocked by KT-59621.
    // declaration.validate()
  }
}
