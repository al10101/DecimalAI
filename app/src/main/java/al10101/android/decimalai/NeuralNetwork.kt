package al10101.android.decimalai

import al10101.android.decimalai.utils.MODEL_TAG
import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.exp

class NeuralNetwork(context: Context) {

    // The image must have this resolution for the NN to work, since these were dimensions of the
    // used to train the model
    val xPixels = 20
    val yPixels = 20

    // Parameters to get the architecture of the NN
    val inputs = 400            // NN trained with 20x20 pixels images
    private val neurons = 25    // 1 hidden layer
    private val outputs = 10    // 10 different digits, from 0 to 9

    // To remember where the +1 is coming from, we label the following variables
    private val bias = 1
    private val biasF = floatArrayOf(1f)

    // There are two matrices containing the weights, to work specifically with the parameters written above
    private val theta1 = FloatArray( neurons * (inputs + bias) )
    private val theta2 = FloatArray( outputs * (neurons + bias) )

    init {

        // The weights stored here are specifically trained to work with the architecture of the NN
        // for this particular problem, so we already know the dimensions of the matrices and there is
        // no need to read them from the file whatsoever. The values were obtained from
        // https://github.com/al10101/NN-with-Numpy-Keras-Pytorch
        readMatrixFile(context, "theta1.txt", theta1)
        readMatrixFile(context, "theta2.txt", theta2)

        Log.d(MODEL_TAG, "theta1= ${theta1[0]}, ${theta1[1]}, ${theta1[2]}, ${theta1[3]}, ..., ${theta1.last()} (${theta1.size} elements)")
        Log.d(MODEL_TAG, "theta2= ${theta2[0]}, ${theta2[1]}, ${theta2[2]}, ${theta2[3]}, ..., ${theta2.last()} (${theta2.size} elements)")

    }

    /**
     * Computes the prediction following the forward propagation algorithm. It is customized
     * for this very specific NN architecture
     * @param x is the input from the user interaction: 20x20 pixels containing the handwritten digit
     * @return the prediction in the form of a vector of probabilities
     */
    fun forward(x: FloatArray): FloatArray {

        // The first activation function is just the input concatenated to the bias
        val a1 = biasF + x

        // The second activation is inside the hidden matrix. First matrix multiplication and then
        // sigmoid function
        val a2 = FloatArray(neurons)
        matMul(theta1, a1, a2, neurons, inputs + bias, 1)
        sigmoid(a2)

        // The third activation is the output corresponding to the set of probabilities from which
        // we will later choose the largest. Do not forget to add the bias
        val ba2 = biasF + a2
        val a3 = FloatArray(outputs)
        matMul(theta2, ba2, a3, outputs, neurons + bias, 1)
        sigmoid(a3)

        return a3

    }

    private fun readMatrixFile(context: Context, filename: String, theta: FloatArray) {

        var targetIndex = 0

        // Read each line and add the elements to the theta
        val lines = BufferedReader(InputStreamReader(context.assets.open(filename))).readLines()
        for (line in lines) {
            // Trim the initial and final whitespaces and split using the whitespaces around numbers
            val weights = line.trim().split("\\s+".toRegex())
            for (w in weights) {
                theta[targetIndex++] = w.toFloat()
            }
        }

    }

    /**
     * Multiplies two matrices. The matrix R is formed as A B = R.
     * @param aMat is the first factor (n x m).
     * @param bMat is the second factor (m x l).
     * @param rMat is the product matrix (n x l). It is overwritten as the output.
     */
    private fun matMul(aMat:FloatArray, bMat:FloatArray, rMat:FloatArray, n:Int, m:Int, l:Int) {

        // Initialize the stride counters
        var ik = - m - 1
        var ir = -1
        var ji: Int
        var ib: Int

        for (k in 0 until l) {

            // Use the row stride
            ik += m

            for (j in 0 until n) {
                ir ++
                ji = j - n
                ib = ik

                rMat[ir] = 0f

                for (i in 0 until m) {
                    ji += n
                    ib ++
                    rMat[ir] += aMat[ji] * bMat[ib]
                }

            }

        }

    }

    /**
     * Apply the sigmoid function to every element of z
     * @param z is the array containing the elements to which we apply the sigmoid function. It is overwritten as the output
     */
    private fun sigmoid(z: FloatArray) {
        for (i in z.indices) {
            z[i] = 1f / (1f - exp(-z[i]))
        }
    }

}