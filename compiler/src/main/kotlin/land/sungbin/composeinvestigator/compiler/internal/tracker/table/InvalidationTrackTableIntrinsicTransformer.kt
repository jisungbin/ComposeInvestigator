/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.table

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_NAME_FQN
import land.sungbin.composeinvestigator.compiler.internal.CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN
import land.sungbin.composeinvestigator.compiler.internal.ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME
import land.sungbin.composeinvestigator.compiler.internal.ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME
import land.sungbin.composeinvestigator.compiler.internal.UNKNOWN_STRING
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.TrackerWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.set
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.composeinvestigator.compiler.util.irString
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getPropertySetter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

internal class InvalidationTrackTableIntrinsicTransformer(
  private val context: IrPluginContext,
  private val table: IrInvalidationTrackTable,
  @Suppress("unused") private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext(), IrPluginContext by context {
  private val currentTableGetterSymbol = referenceProperties(CallableId.fromFqName(CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN)).single().owner.getter!!

  private val _tableSymbol = referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
  private val currentComposableNameGetterSymbol = _tableSymbol.getPropertyGetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner
  private val currentComposableNameSetterSymbol = _tableSymbol.getPropertySetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner
  private val currentComposableKeyNameGetterSymbol = _tableSymbol.getPropertyGetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME.asString())!!.owner

  private val composableNameSymbol = referenceClass(ClassId.topLevel(COMPOSABLE_NAME_FQN))!!.owner

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  override fun visitCall(expression: IrCall): IrExpression =
    when (expression.symbol.owner.kotlinFqName) {
      currentTableGetterSymbol.kotlinFqName -> table.propGetter()
      currentComposableNameGetterSymbol.kotlinFqName -> {
        IrConstructorCallImpl.fromSymbolOwner(
          type = composableNameSymbol.defaultType,
          constructorSymbol = composableNameSymbol.symbol.constructors.single(),
        ).apply {
          putValueArgument(
            0,
            lastReachedComposable()
              ?.let { composable ->
                IrAffectedComposable.getComposableName(irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.irAffectedComposable)
              }
              ?: irString(SpecialNames.UNKNOWN_STRING)
          )
        }
      }
      currentComposableNameSetterSymbol.kotlinFqName -> {
        lastReachedComposable()?.let { composable ->
          val userProvideName = expression
            .getValueArgument(0).cast<IrConstructorCall>()
            .getValueArgument(0).safeAs<IrConst<String>>()?.value
            ?: error("Currently, only string hardcodes are supported as arguments to ComposableName. (${expression.dumpKotlinLike()})")

          val previousKey = irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable]!!
          val newAffectedComposable = IrAffectedComposable.copyWith(previousKey.irAffectedComposable, composableName = irString(userProvideName))

          irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable] = previousKey.copy(irAffectedComposable = newAffectedComposable)
        }

        IrGetObjectValueImpl(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = irBuiltIns.unitClass.defaultType,
          symbol = irBuiltIns.unitClass.owner.symbol,
        )
      }
      currentComposableKeyNameGetterSymbol.kotlinFqName -> {
        lastReachedComposable()
          ?.let { composable -> irString(irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.keyName) }
          ?: irString(SpecialNames.UNKNOWN_STRING)
      }
      else -> super.visitCall(expression)
    }

  private fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
      ?.irElement?.safeAs<IrSimpleFunction>()
}
