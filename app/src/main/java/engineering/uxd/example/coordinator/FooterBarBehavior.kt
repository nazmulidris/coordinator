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
import android.support.animation.FloatPropertyCompat
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Simple layout behavior that will track the state of the AppBarLayout
 * and match its offset for a corresponding footer.
 */
class FooterBarBehavior(val context: Context, attrs: AttributeSet) :
        CoordinatorLayout.Behavior<FrameLayout>(context, attrs),
        AnkoLogger {
    var value: String = attrs.getAttributeValue("example.com", "my_key")

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
                    if (dependencyMet) " <- YES!!!" else " <- NO"
        }
        setTag(child, "$value, ${SimpleDateFormat("MM/dd/y hh:mm:sa").format(Date())}")
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
            "REACT TO DEPENDENCY CHANGED: " +
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
            "REACT TO DEPENDENCY REMOVED " +
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
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            info {
                "START NESTED SCROLL - NON_TOUCH"
            }
        } else {
            info {
                "START NESTED SCROLL - TOUCH"
            }
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(
                        coordinatorLayout, child, directTargetChild, target, axes, type)
    }

//    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout,
//                                   child: FrameLayout,
//                                   target: View,
//                                   dx: Int,
//                                   dy: Int,
//                                   consumed: IntArray,
//                                   type: Int) {
//        if (type == ViewCompat.TYPE_NON_TOUCH) info {
//            "\tNESTED PRE SCROLL - NON_TOUCH dx= $dx, dy= $dy"
//        }
//    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
                                child: FrameLayout,
                                target: View,
                                dxConsumed: Int,
                                dyConsumed: Int,
                                dxUnconsumed: Int,
                                dyUnconsumed: Int,
                                type: Int) {
        val absDyUC = abs(dyUnconsumed)

        // RV can't scroll but user hasn't lifted their finger (drag, not fling)
        if (absDyUC > 0 && type == ViewCompat.TYPE_TOUCH) {
            info { "[RV__STOP...]" }
            if (flingData.overscrollDetected == false) {
                val ratio = (absDyUC.toFloat() / target.height.toFloat() * 1000f).toInt()
                val applied = when (ratio) {
                    in 0..5 -> 1f
                    in 6..10 -> 0.7f
                    in 11..15 -> 0.5f
                    in 15..30 -> 0.3f
                    else -> 0.1f
                }
                if (target is RecyclerView) {
                    target.alpha = applied
                }
                info { "[RV__DO ANIM FOR OVERSCROLL] absDyUC=$absDyUC, ratio=$ratio" }
            }
        } else {
            info { "[RV__MOVE...]" }
            target.alpha = 1f
        }

        // RV can't scroll anymore and user has lifted their finger (fling, not drag)
        if (type == ViewCompat.TYPE_NON_TOUCH && absDyUC > 0) {

            info {
                "\t\t[RV_STOP] NESTED SCROLL - NON_TOUCH " +
                        "dxC=$dxConsumed, dyC=$dyConsumed, " +
                        "dxUC=$dxUnconsumed, dyUC=$dyUnconsumed"
            }

            // This method (onNestedScroll) gets called repeatedly as this scrolling is occurring.
            // absDyUC starts at some value, then within a few ms increases in value, then hits a
            // max, then starts to reduce in value. We capture this max, and then use it to trigger
            // an animation on the RV w/ force derived from this max value (which is roughly how
            // far this view would move it were to be scrolled). We take this value and then compute
            // a ratio of the visible height of the view (that isn't scrolling) to get an idea of
            // the force that's left over from the user's fling motion.

            if (flingData.overscrollDetected == false) {
                if (absDyUC < flingData.maxDyUC) {
                    flingData.overscrollDetected = true
                    flingData.startTime = System.currentTimeMillis()
                    flingData.rvHeight = target.height
                    context.toast(flingData.toString())
                    info {
                        "\t\t\t[DO SOMETHING] NESTED SCROLL - NON_TOUCH " +
                                "\n\t\t\t\t$flingData"
                    }
                    if (target is RecyclerView)
                        applyAnimationToRV(
                                flingData.vY,
                                flingData.getRatio(),
                                target)
                } else {
                    flingData.maxDyUC = absDyUC
                }
            }

            //paintDebugOverScroll(dyUnconsumed, target)

        } else {

            info {
                "\t\t[RV_MOVE] NESTED SCROLL - NON_TOUCH " +
                        "dxC=$dxConsumed, dyC=$dyConsumed, " +
                        "dxUC=$dxUnconsumed, dyUC=$dyUnconsumed"
            }

        }

    }

    private fun applyAnimationToRV(vY: Float, ratio: Int, target: RecyclerView) {
        val forceConstant = 500f
        val forceApplied = when (ratio) {
            in 0..5 -> 500f
            in 6..10 -> 1500f
            in 11..15 -> 5000f
            in 15..30 -> 10000f
            else -> 20000f
        }
        info { "NESTED SCROLL forceApplied=$forceApplied, ratio=$ratio, vY=$vY" }
        val scaleProperty = object : FloatPropertyCompat<View>("scaleProperty") {
            var value = 0f
            override fun getValue(view: View): Float {
                return value
            }

            override fun setValue(view: View, value: Float) {
                this.value = value
                val scaleValue = (value / forceConstant) + 1f
                info { "scaleProperty: value = $value, scaleValue = $scaleValue" }
                view.scaleX = scaleValue
                view.translationY = value
            }
        }
        val force = (SpringForce()).apply {
            finalPosition = 1f
            dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
        with(SpringAnimation(target, scaleProperty)) {
            spring = force
            setStartVelocity(forceApplied)
            start()
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout,
                                    child: FrameLayout,
                                    target: View,
                                    type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            info {
                "STOP NESTED SCROLL - NON_TOUCH, elapsed time = " +
                        "${System.currentTimeMillis() - flingData.startTime}"
            }
            flingData.reset()
        }
    }

//    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout,
//                                  child: FrameLayout,
//                                  target: View,
//                                  velocityX: Float,
//                                  velocityY: Float): Boolean {
//        info {
//            "NESTED PRE FLING, vX=$velocityX, vY=$velocityY"
//        }
//        return false
//    }

    /**
     * This method is used primarily to call [FlingData.reset], so that overscroll animation
     * on the RecyclerView can work. Nothing is really done with the velocity X and Y values.
     */
    override fun onNestedFling(coordinatorLayout: CoordinatorLayout,
                               child: FrameLayout,
                               target: View,
                               velocityX: Float,
                               velocityY: Float,
                               consumed: Boolean): Boolean {
        flingData.reset()
        flingData.vY = velocityY
        info {
            "NESTED FLING, vX=$velocityX, vY=$velocityY"
        }
        return false
    }

    val flingData = FlingData()

    data class FlingData(var vY: Float = 0f,
                         var maxDyUC: Int = 0,
                         var overscrollDetected: Boolean = false,
                         var startTime: Long = 0,
                         var rvHeight: Int = 0) {
        fun reset() {
            vY = 0f
            maxDyUC = 0
            overscrollDetected = false
            startTime = 0
            rvHeight = 0
        }

        override fun toString(): String {
            return "FlingData(vY=$vY, maxDyUC=$maxDyUC, rvH=$rvHeight, \n\t\t\t\t" +
                    "          ratio=${getRatio()}%, rvStopped=$overscrollDetected)"
        }

        /*
        @return Int between 0% and 25% to indicate the amount of the view would move
        if the fling velocity was applied to scrolling the view.
         */
        fun getRatio() = (maxDyUC.toFloat() / rvHeight.toFloat() * 100).toInt()
    }

    private fun paintDebugOverscroll(dyUnconsumed: Int, target: View) {
        if (flingData.overscrollDetected) {
            var fraction: Float = abs(dyUnconsumed.toFloat() / flingData.maxDyUC.toFloat())
            if (fraction > 1f) fraction = 1f
            val rv = target as? RecyclerView
            rv?.alpha = (1f - fraction)
            info { "\t\t\t\tNESTED fraction: $fraction" }
        }
    }

}