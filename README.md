# coordinator

This project is all about `CoordinatorLayout` and nested scrolling.

# Resources

- [CoordinatorLayout and Behaviors (medium article)](https://goo.gl/oLH8pm)

# Info about the code

The main layout XML file has a `FrameLayout` view group that has a behavior 
attached to it. It also has a key-value pair that is defined, which 
can be retrieved in the code.

```xml
<CoordinatorLayout ...>
    <RecyclerView ...>...</RecyclerView>
    <AppBarLayout ...>
        <Toolbar ...>...</Toolbar>
    </AppBarLayout>
    <FrameLayout xmlns:my_app="http://example.com"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        my_app:my_key="my_value"
        app:layout_behavior="engineering.uxd.example.coordinator.FooterBarBehavior">
</CoordinatorLayout>
```

The `FooterBarBehavior` class extends `CoordinatorLayout.Behavior<FrameLayout>` and it implements 
two methods:
1. layoutDependsOn(parent: CoordinatorLayout,child: FrameLayout,dependency: View): Boolean
2. onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean

The first method checks to see if any of the children contained in the `CoordinatorLayout` are
affected by this custom behavior.

The second method then provides a way for the custom behavior to take some action on the 
dependent view, in reaction to some change that occurred in a child of the `CooordinatorLayout`
that this behavior depends on.