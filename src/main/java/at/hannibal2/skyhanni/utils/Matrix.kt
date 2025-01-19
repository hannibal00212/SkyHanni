package at.hannibal2.skyhanni.utils

import kotlin.math.abs

class Matrix(val data: Array<DoubleArray>) {
    val width: Int
        get() {
            return data[0].size
        }
    val height: Int
        get() {
            return data.size
        }

    operator fun get(index: Int): DoubleArray {
        return this.data[index]
    }

    fun inverse(): Matrix {
        if (this.height != this.width) throw Error("Incorrect dimensions!")
        val a = this.copy()
        val b = identity(this.width)
        for (c in 0 until this.width) {
            var aBig = abs(a[c][c])
            var rBig = c
            var r = c + 1
            while (r < this.height) {
                if (abs(a[r][c]) > aBig) {
                    aBig = abs(a[r][c])
                    rBig = r
                }
                r++
            }
            if (aBig == 0.0) {
                throw Error("Cannot invert matrix")
            }
            r = rBig
            if (r != c) {
                var temp = a[c]
                a[c] = a[r]
                a[r] = temp
                temp = b[c]
                b[c] = b[r]
                b[r] = temp
            }
            val ac = a[c]
            val bc = b[c]
            for (r2 in 0 until this.height) {
                val ar = a[r2]
                val br = b[r2]
                if (r2 != c) {
                    if (ar[c] != 0.0) {
                        val f = (-ar[c]) / ac[c]
                        for (s in c until this.width) {
                            ar[s] = ar[s] + (f * ac[s])
                        }
                        for (s in 0 until this.width) {
                            br[s] = br[s] + (f * bc[s])
                        }
                    }
                } else {
                    val f = ac[c]
                    for (s in c until this.width) {
                        ar[s] = ar[s] / f
                    }
                    for (s in 0 until this.width) {
                        br[s] = br[s] / f
                    }
                }
            }
        }
        return b
    }

    fun transpose(): Matrix {
        val newDoubleArray: Array<DoubleArray> = Array(width) {
            DoubleArray(height)
        }
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                newDoubleArray[colIndex][rowIndex] = cell
            }
        }
        return Matrix(
            newDoubleArray,
        )
    }

    operator fun times(other: Matrix): Matrix {
        if (other.height != this.width) throw Error("Invalid Matrix sizes")
        val m = other.width
        val n = other.height
        val p = this.height
        val cDoubleArray = Array(p) {
            DoubleArray(m)
        }
        for (i in 0 until m) {
            for (j in 0 until p) {
                var total = 0.0
                for (k in 0 until n) {
                    total += other[k][i] * this[j][k]
                }
                cDoubleArray[j][i] = total
            }
        }
        return Matrix(
            cDoubleArray,
        )
    }

    operator fun set(index: Int, value: DoubleArray) {
        this.data[index] = value
    }

    fun copy(): Matrix {
        val newDoubleArray: Array<DoubleArray> = Array(height) {
            DoubleArray(width)
        }
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                newDoubleArray[rowIndex][colIndex] = row[colIndex]
            }
        }
        return Matrix(
            newDoubleArray,
        )
    }

    operator fun times(other: Double): Matrix {
        val newDoubleArray: Array<DoubleArray> = Array(height) {
            DoubleArray(width)
        }
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                newDoubleArray[rowIndex][colIndex] = cell * other
            }
        }
        return Matrix(
            newDoubleArray,
        )
    }

    operator fun plus(other: Matrix): Matrix {
        if (other.width != this.width || other.height != this.height) throw Error("Invalid Dimensions")
        val newDoubleArray: Array<DoubleArray> = Array(height) {
            DoubleArray(width)
        }
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                newDoubleArray[rowIndex][colIndex] = cell + other[rowIndex][colIndex]
            }
        }
        return Matrix(
            newDoubleArray,
        )
    }

    operator fun minus(other: Matrix): Matrix {
        if (other.width != this.width || other.height != this.height) throw Error("Invalid Dimensions")
        val newDoubleArray: Array<DoubleArray> = Array(height) {
            DoubleArray(width)
        }
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                newDoubleArray[rowIndex][colIndex] = cell - other[rowIndex][colIndex]
            }
        }
        return Matrix(
            newDoubleArray,
        )
    }

    override fun toString(): String {
        var output = ""
        for (rowIndex in data.indices) {
            val row = data[rowIndex]
            for (colIndex in row.indices) {
                val col = row[colIndex]
                output += " $col"
            }
            output += "\n"
        }
        return output
    }

    companion object {
        fun identity(rows: Int): Matrix {
            val newDoubleArray: Array<DoubleArray> = Array(rows) {
                DoubleArray(rows)
            }
            for (i in 0 until rows) {
                newDoubleArray[i][i] = 1.0
            }
            return Matrix(
                newDoubleArray,
            )
        }
    }
}
