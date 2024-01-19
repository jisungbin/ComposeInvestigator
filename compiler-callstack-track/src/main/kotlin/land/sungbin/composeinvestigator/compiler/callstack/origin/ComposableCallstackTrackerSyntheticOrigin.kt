package land.sungbin.composeinvestigator.compiler.callstack.origin

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl

public data object ComposableCallstackTrackerSyntheticOrigin : IrDeclarationOriginImpl(
  "GENERATED_COMPOSABLE_CALLSTACK_TRACKER_MEMBER",
  isSynthetic = true,
)
