package at.hannibal2.skyhanni.utils

import kotlin.math.pow

class PolynomialFitter(private val degree: Int) {
    private val xPointMatrix: ArrayList<DoubleArray> = ArrayList()
    private val yPoints: ArrayList<DoubleArray> = ArrayList()
    fun addPoint(x: Double, y: Double) {
        yPoints.add(doubleArrayOf(y))
        val xArray = DoubleArray(degree + 1)
        for (i in xArray.indices) {
            xArray[i] = x.pow(i)
        }
        xPointMatrix.add(xArray)
    }

    fun fit(): DoubleArray {
        val xMatrix = Matrix(xPointMatrix.toTypedArray())
        val yMatrix = Matrix(yPoints.toTypedArray())
        return ((xMatrix.transpose() * xMatrix).inverse() * xMatrix.transpose() * yMatrix).transpose()[0]
    }
}
