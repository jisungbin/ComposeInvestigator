# Advanced usage

ComposeInvestigator has various runtime features.

---

## Customizing Logging Behavior

ComposeInvestigator comes with a default logging behavior that reports every time a recomposition 
occurs. However, if desired, you can change it to your own custom logging behavior.

``` kotlin
ComposeInvestigatorConfig.logger = ComposableInvalidationLogger { composable, result ->
  // Your custom logging behavior
  println("The '${composable.name}' composable has been invalidated: $result")
}
```

## ComposableInvalidationTrackTable

All data added or modified while ComposeInvestigator is running is managed by a class called 
`ComposableInvalidationTrackTable`. The `ComposableInvalidationTrackTable` class is created as
a top-level singleton in every file, and the instance created this way can be accessed through 
the `currentComposableInvalidationTracker` API.

The instance of `ComposableInvalidationTrackTable` created in the current file can be retrieved
using `currentComposableInvalidationTracker`.

!!! tip
    If calling `currentComposableInvalidationTracker` at the top-level results in an error,
    try wrapping the call with `by lazy`.
    
## Tracking the Name of StateObject

To be written soon.

## Suppress ComposeInvestigator behavior 

If for some reason you need to suppress ComposeInvestigator working only to a certain range,
you can use the `@NoInvestigation` annotation.

## Disabling ComposeInvestigator

When ComposeInvestigator is running, most files will include the initialization of the 
`ComposableInvalidationTrackTable` class, which could impact production performance. Therefore, 
it is recommended to use ComposeInvestigator only in a debug environment. In production, you can
disable it as follows.

``` gradle
composeInvestigator {
  enabled = false
}
```
