package edu.ksu.wheatgenetics.survey.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import edu.ksu.wheatgenetics.survey.NmeaParser

class SatPlotView : View {

    private var blue = Paint(Color.BLUE)

    private var red = Paint(Color.RED)

    private var black = Paint(Color.BLACK)

    private var green = Paint(Color.GREEN)

    private var gsv = ArrayList<NmeaParser.GSV>()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        red.color = Color.RED
        blue.color = Color.BLUE
        green.color = Color.GREEN

        black.apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            strokeCap = Paint.Cap.BUTT
            strokeJoin = Paint.Join.BEVEL
            strokeMiter = 1f
            textSize = 36f
        }
        arrayOf(blue, red, green).forEach {
            it.style = Paint.Style.STROKE
            it.strokeWidth = 3f
            it.strokeCap = Paint.Cap.BUTT
            //it.strokeJoin = Paint.Join.BEVEL
            //it.strokeMiter = 1f
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val actualWidth = width - (paddingLeft + paddingRight)
        val actualHeight = height - (paddingTop + paddingBottom)

        var radius = Math.min(actualWidth, actualHeight) / 2
        val cx = paddingLeft + actualWidth / 2
        val cy = paddingTop + actualHeight / 2

        canvas.drawText("15", cx + 15f * (radius/90.0f), cy.toFloat(), black)
        canvas.drawText("30", cx + 30f * (radius/90.0f), cy.toFloat(), black)
        canvas.drawText("45", cx + 45f * (radius/90.0f), cy.toFloat(), black)
        canvas.drawText("60", cx + 60f * (radius/90.0f), cy.toFloat(), black)

        for (i in 0 until radius step (radius / 90.0).toInt()) {
            if (i % 45 == 0) {
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), i.toFloat(), black)
            }
            //canvas.drawArc(Rect(0,0,usableWidth,usableHeight).toRectF(), 0.0f, 360f, false, paint)
        }
        for (i in 0 until 360 step 1) {
            canvas.save()
            canvas.rotate(i.toFloat(), cx.toFloat(), cy.toFloat())
            if (i % 45 == 0) {
                canvas.drawLine(cx.toFloat(), cy.toFloat(), cx.toFloat() + radius, cy.toFloat(), black)
                canvas.drawText("$i", cx + radius.toFloat(), cy.toFloat(), black)
            }
            this.gsv.forEach {
                if (it.azimuthDeg.toFloat() == i.toFloat()) {
                    val x = it.elevationDeg.toFloat()
                    canvas.drawCircle(cx.toFloat() + (radius/90.0).toFloat() * x, cy.toFloat(), 15f,
                        when (it.snr.toInt()) {
                            0 -> red
                            in 1..25 -> blue
                            else -> green })
                    //canvas.drawText("(${it.elevationDeg},${it.azimuthDeg})", cx.toFloat() + (radius/90.0).toFloat() * x, cy.toFloat(), black)
                }
            }

            canvas.restore()
        }

    }

    internal fun subscribe(gsv: ArrayList<NmeaParser.GSV>) {
            this.gsv = gsv
        this.invalidate()
    }
}