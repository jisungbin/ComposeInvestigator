/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_NAME_FQN
import land.sungbin.composeinvestigator.compiler.CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME
import land.sungbin.composeinvestigator.compiler.UNKNOWN_STRING
import land.sungbin.composeinvestigator.compiler.VerboseLogger
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.analysis.set
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.struct.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.struct.propGetter
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

public class InvalidationTrackTableIntrinsicTransformer(
  private val context: IrPluginContext,
  private val table: IrInvalidationTrackTable,
  @Suppress("unused") private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext(), IrPluginContext by context {
  private val currentTableGetterSymbol =
    referenceProperties(CallableId.fromFqName(CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN)).single().owner.getter!!

  private val _tableSymbol = referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
  private val currentComposableNameGetterSymbol =
    _tableSymbol.getPropertyGetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner
  private val currentComposableNameSetterSymbol =
    _tableSymbol.getPropertySetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner
  private val currentComposableKeyNameGetterSymbol =
    _tableSymbol.getPropertyGetter(ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME.asString())!!.owner

  private val composableNameSymbol = referenceClass(ClassId.topLevel(COMPOSABLE_NAME_FQN))!!.owner

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  override fun visitCall(expression: IrCall): IrExpression =
    when (expression.symbol.owner.kotlinFqName) {
      currentTableGetterSymbol.kotlinFqName -> table.propGetter(startOffset = expression.startOffset, endOffset = expression.endOffset)
      currentComposableNameGetterSymbol.kotlinFqName -> {
        IrConstructorCallImpl.fromSymbolOwner(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = composableNameSymbol.defaultType,
          constructorSymbol = composableNameSymbol.symbol.constructors.single(),
        ).apply {
          putValueArgument(
            0,
            lastReachedComposable()
              ?.let { composable ->
                IrAffectedComposable.getComposableName(irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.affectedComposable)
              }
              ?: irString(SpecialNames.UNKNOWN_STRING),
          )
        }
      }
      currentComposableNameSetterSymbol.kotlinFqName -> {
        lastReachedComposable()?.let { composable ->
          val userProvideName = expression
            .getValueArgument(0).cast<IrConstructorCall>()
            .getValueArgument(0).safeAs<IrConst<String>>()?.value
            ?: error("Currently, only string hardcodes are supported as arguments to ComposableName. (${expression.dumpKotlinLike()})")

          val previousKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!
          val newAffectedComposable = IrAffectedComposable.copyWith(previousKey.affectedComposable, composableName = irString(userProvideName))

          irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] = previousKey.copy(affectedComposable = newAffectedComposable)
        }

        IrGetObjectValueImpl(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = irBuiltIns.unitClass.defaultType,
          symbol = irBuiltIns.unitClass.owner.symbol,
        )
      }
      currentComposableKeyNameGetterSymbol.kotlinFqName -> {
        lastReachedComposable()?.let { composable ->
          irString(
            irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.keyName,
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
          )
        } ?: irString(SpecialNames.UNKNOWN_STRING, startOffset = expression.startOffset, endOffset = expression.endOffset)
      }
      else -> super.visitCall(expression)
    }

  private fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
      ?.irElement?.safeAs<IrSimpleFunction>()
}
