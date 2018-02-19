/*
 * Copyright 2018 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package engineering.uxd.example.coordinator

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.time.LocalDateTime
import kotlin.math.abs

/**
 * Simple layout behavior that will track the state of the AppBarLayout
 * and match its offset for a corresponding footer.
 */
class FooterBarBehavior(val context: Context, attrs: AttributeSet) :
        CoordinatorLayout.Behavior<FrameLayout>(context, attrs),
        AnkoLogger {
    var value: String = attrs.getAttributeValue("http://example.com", "my_key")

    /**
     * This custom behavior depends on the [AppBarLayout] object. So make sure to return
     * `true` when the `dependency` matches [AppBarLayout].
     */
    override fun layoutDependsOn(parent: CoordinatorLayout,
                                 child: FrameLayout,
                                 dependency: View): Boolean {
        //We are only interested in watching changes in the AppBarLayout
        val dependencyMet = dependency is AppBarLayout
        info {
            "DEPENDENCY CHECK: " +
                    "\nchild=${child.javaClass.simpleName}" +
                    ", dependency=${dependency.javaClass.simpleName}" +
                    if (dependencyMet) " <- YES!!!" else ""
        }
        setTag(child, "$value, ${LocalDateTime.now()}")
        return dependencyMet
    }

    /**
     * This is called when the [AppBarLayout] `dependency` changes in any way. This provides
     * us an opportunity to make a change to the [FrameLayout] `child`.
     */
    override fun onDependentViewChanged(parent: CoordinatorLayout,
                                        child: FrameLayout,
                                        dependency: View): Boolean {
        info {
            "REACT TO DEPENDENCY CHANGE: " +
                    "\nAppBarLayout changed!!!" +
                    "\nchild=${child.javaClass.simpleName}" +
                    ", dependency=${dependency.javaClass.simpleName}" +
                    ", tag=${getTag(child)}"
        }
        val offset = -dependency.top
        child.translationY = offset.toFloat()
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout,
                                        child: FrameLayout,
                                        dependency: View) {
        info {
            "REACT TO DEPENDENCY BEING REMOVED " +
                    "\nAppBarLayout removed!!!" +
                    "\nchild=${child.javaClass.simpleName}" +
                    ", dependency=${dependency.javaClass.simpleName}" +
                    ", tag=${getTag(child)}"
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                     child: FrameLayout,
                                     directTargetChild: View,
                                     target: View,
                                     axes: Int,
                                     type: Int): Boolean {
        if (type == ViewCompat.TYPE_NON_TOUCH) info {
            "START NESTED SCROLL - NON_TOUCH"
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(
                        coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout,
                                   child: FrameLayout,
                                   target: View,
                                   dx: Int,
                                   dy: Int,
                                   consumed: IntArray,
                                   type: Int) {
//        if (type == ViewCompat.TYPE_NON_TOUCH) info {
//            "\tNESTED PRE SCROLL - NON_TOUCH dx= $dx, dy= $dy"
//        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
                                child: FrameLayout,
                                target: View,
                                dxConsumed: Int,
                                dyConsumed: Int,
                                dxUnconsumed: Int,
                                dyUnconsumed: Int,
                                type: Int) {
        // RV has hit the top / bottom edge and can't scroll anymore.
        val rvStoppedScrolling = type == ViewCompat.TYPE_NON_TOUCH && abs(dyUnconsumed) > 0

        if (rvStoppedScrolling) {
            doStoppedAnimation(dyUnconsumed, target)
            info {
                "\t\t[RV_STOP] NESTED SCROLL - NON_TOUCH " +
                        "dxC=$dxConsumed, dyC=$dyConsumed, " +
                        "dxUC=$dxUnconsumed, dyUC=$dyUnconsumed"
            }
        } else {
            info {
                "\t\t[RV_MOVE] NESTED SCROLL - NON_TOUCH " +
                        "dxC=$dxConsumed, dyC=$dyConsumed, " +
                        "dxUC=$dxUnconsumed, dyUC=$dyUnconsumed"
            }
        }
        if (!flingData.stopDetected)
            if (rvStoppedScrolling && abs(dyConsumed) == 0 && abs(dyUnconsumed) > 0) {
                flingData.stopDetected = true
                flingData.startTime = System.currentTimeMillis()
                flingData.dY = abs(dyUnconsumed)
                info {
                    "\t\t\t[DO SOMETHING] NESTED SCROLL - NON_TOUCH " +
                            "\n\t\t\t\t$flingData"
                }
                recyclerViewJustHitStop()
            }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout,
                                    child: FrameLayout,
                                    target: View,
                                    type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) info {
            "STOP NESTED SCROLL - NON_TOUCH, elapsed time = " +
                    "${System.currentTimeMillis() - flingData.startTime}"
        }
        flingData.reset()
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout,
                                  child: FrameLayout,
                                  target: View,
                                  velocityX: Float,
                                  velocityY: Float): Boolean {
        info {
            "NESTED PRE FLING, vX=$velocityX, vY=$velocityY"
        }
        return false
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout,
                               child: FrameLayout,
                               target: View,
                               velocityX: Float,
                               velocityY: Float,
                               consumed: Boolean): Boolean {
        flingData.vY = velocityY
        info {
            "NESTED FLING, vX=$velocityX, vY=$velocityY"
        }
        return false
    }

    val flingData = FlingData()

    data class FlingData(var vY: Float = 0f,
                         var dY: Int = 0,
                         var maxDy: Int = 0,
                         var stopDetected: Boolean = false,
                         var startTime: Long = 0) {
        fun reset() {
            vY = 0f
            dY = 0
            maxDy = 0
            stopDetected = false
            startTime = 0
        }
    }

    private fun recyclerViewJustHitStop() {
        context.toast(flingData.toString())
    }

    private fun doStoppedAnimation(dyUnconsumed: Int, target: View) {
        var fraction: Float = abs(dyUnconsumed.toFloat() / flingData.dY.toFloat())
        if (fraction > 1f) fraction = 1f
        val rv = target as? RecyclerView
        rv?.alpha = (1f - fraction)
        info { "\t\t\t\tNESTED fraction: $fraction" }
    }
}