package edu.ksu.wheatgenetics.survey.views

import android.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet

import com.google.android.material.bottomnavigation.BottomNavigationView

class CurvedBottomNavigationView : BottomNavigationView {
    private var mPath: Path = Path()
    private var mPaint: Paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#FF5722")
        setBackgroundColor(Color.TRANSPARENT)
    }

    private val mRadius = 256 / 2
    // the coordinates of the first curve
    private val mFirstCurveStartPoint = Point()
    private val mFirstCurveEndPoint = Point()
    private val mFirstCurveControlPoint1 = Point()
    private val mFirstCurveControlPoint2 = Point()

    //the coordinates of the second curve
    private var mSecondCurveStartPoint = Point()
    private val mSecondCurveEndPoint = Point()
    private val mSecondCurveControlPoint1 = Point()
    private val mSecondCurveControlPoint2 = Point()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // the coordinates (x,y) of the start point before curve
        mFirstCurveStartPoint.set(width / 2 - mRadius * 2 - mRadius / 3, 0)
        // the coordinates (x,y) of the end point after curve
        mFirstCurveEndPoint.set(width / 2, mRadius + mRadius / 4)
        // same thing for the second curve
        mSecondCurveStartPoint = mFirstCurveEndPoint
        mSecondCurveEndPoint.set(width / 2 + mRadius * 2 + mRadius / 3, 0)

        // the coordinates (x,y)  of the 1st control point on a cubic curve
        mFirstCurveControlPoint1.set(mFirstCurveStartPoint.x + mRadius + mRadius / 4, mFirstCurveStartPoint.y)
        // the coordinates (x,y)  of the 2nd control point on a cubic curve
        mFirstCurveControlPoint2.set(mFirstCurveEndPoint.x - mRadius * 2 + mRadius, mFirstCurveEndPoint.y)

        mSecondCurveControlPoint1.set(mSecondCurveStartPoint.x + mRadius * 2 - mRadius, mSecondCurveStartPoint.y)
        mSecondCurveControlPoint2.set(mSecondCurveEndPoint.x - (mRadius + mRadius / 4), mSecondCurveEndPoint.y)

        mPath.reset()
        mPath.moveTo(0f, 0f)
        mPath.lineTo(mFirstCurveStartPoint.x.toFloat(), mFirstCurveStartPoint.y.toFloat())

        mPath.cubicTo(mFirstCurveControlPoint1.x.toFloat(), mFirstCurveControlPoint1.y.toFloat(),
                mFirstCurveControlPoint2.x.toFloat(), mFirstCurveControlPoint2.y.toFloat(),
                mFirstCurveEndPoint.x.toFloat(), mFirstCurveEndPoint.y.toFloat())

        mPath.cubicTo(mSecondCurveControlPoint1.x.toFloat(), mSecondCurveControlPoint1.y.toFloat(),
                mSecondCurveControlPoint2.x.toFloat(), mSecondCurveControlPoint2.y.toFloat(),
                mSecondCurveEndPoint.x.toFloat(), mSecondCurveEndPoint.y.toFloat())

        mPath.lineTo(width.toFloat(), 0f)
        mPath.lineTo(width.toFloat(), height.toFloat())
        mPath.lineTo(0f, height.toFloat())
        mPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath, mPaint)
    }
}
