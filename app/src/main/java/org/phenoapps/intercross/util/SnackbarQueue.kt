package org.phenoapps.intercross.util

import android.content.Context
import android.os.Handler
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

/*
A scheduler for posting Snackbar messages so they don't overlap.
 */
class SnackbarQueue {

    private lateinit var mSnack: Snackbar

    private var isShowing: Boolean = false

    private val mHandler = Handler()

    private var mQueue: ArrayList<SnackJob> = ArrayList()

    init {

        val task = object : TimerTask() {

            override fun run() {

                try {
                    if (mQueue.isNotEmpty() && !isShowing) {
                        isShowing = true
                        val job = mQueue.removeAt(0)
                        mSnack = Snackbar.make(job.v, job.txt, Snackbar.LENGTH_LONG)
                        mSnack.setAction(job.actionText) {
                            job.action()
                        }
                        mSnack.show()

                    }
                } catch (e : IllegalArgumentException) {
                    e.printStackTrace()
                } finally {
                    mHandler.postDelayed({ isShowing = false }, 250)
                }
            }
        }

        Timer().scheduleAtFixedRate(task, 0, 1000)

    }

    fun clear() = mQueue.clear()

    fun push(job: SnackJob) = mQueue.add(job)

    data class SnackJob(val v: View, val txt: String, val actionText: String = "", val action: () -> Unit = {})
}