package sleepfuriously.com.dollargame.model

import android.util.Log

class SetsOfIntsUtil {

    //----------------------------------
    //  constants
    //----------------------------------

    private val TAG: String = "SetsOfIntsUtil"

    /**
     * if false, blocks debug prints to Log.d
     */
    private var debug = false

    //----------------------------------
    //  data
    //----------------------------------

    //----------------------------------
    //  methods
    //----------------------------------

    /**
     * Finds a single random set of numInts integers which sum to the specified sum.
     * Integers are chosen from the range floor..ceiling, inclusive. Duplicates are allowed.
     * Returns a nullable array of those ints -- which will be null iff it's impossible to
     * find a set from the given pool.
     *
     * @param sum      The number that the items in the list will add up to.
     * @param numInts  The number of ints in the return list.
     * @param floor    The lowest possible value of any number in the list.
     * @param ceiling  The highest possible value. Must be >= floor.
     *
     * @return  An array of ints that add up to the given sum. If no possible
     *          list exists, then null will be returned.
     */
    fun findRandomSetOfIntsWithGivenSum(
        sum : Int, numInts: Int, floor: Int, ceiling: Int): Array<Int>? {

        val resultArray = findRandomSetOfIntsWithGivenSumRecurse(sum, numInts, floor, ceiling, "")

        debugOn()

        debugPrint("### RESULT: ### array size = " + resultArray.size.toString())
        for (i in 0..(resultArray.size - 1)) {
            debugPrint( i.toString() + ": " + resultArray.get(i).toString() )
        }
        debugPrint("RESULT sum = " + resultArray.sum())

        // Return a nullable array -- either the array returned from the recursive routing,
        // or null if that routine returns an empty list, meaning it was given an impossible task.
        var resultArrayNullable : Array<Int>? = null
        if( resultArray.size > 0) {
            resultArrayNullable = resultArray
        }
        return resultArrayNullable
    }

    /**
     * Same as the public non-recursive non-recursive function -- except that this
     * returns an empty list rather than null if no set of ints can satisfy the conditions.
     */
    private fun findRandomSetOfIntsWithGivenSumRecurse(
        sum : Int, numInts: Int, floor: Int, ceiling: Int, indent : String): Array<Int> {

        debugPrint("M=" + sum.toString() + " N=" + numInts.toString() +
                " R=" + floor.toString() + " S=" + ceiling.toString(), indent )
        lateinit var resultArray : Array<Int>

        if (numInts < 1) {
            debugPrint("ERROR numInts < 1", indent)
            resultArray = arrayOf<Int>()   // we'll return empty array
        } else if ( floor > ceiling ) {
            debugPrint("floor > ceiling", indent)
            resultArray = arrayOf<Int>()   // we'll return empty array
        } else if (sum < numInts * floor || sum > numInts * ceiling) {
            // If we're here, there's no possible set of of numInts ints in floor..ceiling that add up to sum.
            debugPrint("No possible set of ints satisfy the conditions.", indent)
            resultArray = arrayOf<Int>()   // we'll return empty array
        } else if (numInts == 1) {
            debugPrint("base case -- sum = " + sum.toString(), indent)
            resultArray = Array(1) { i -> sum }   // we'll return empty array
        } else {
            // Split numInts into two N's .. the first being 1 higher iff numInts is odd
            val secondNumInts = numInts / 2
            val firstNumInts = numInts - secondNumInts  // always >= secondNumInts

            debugPrint("N1=" + firstNumInts.toString() + " N2=" + secondNumInts.toString(), indent )
            val secondFloor = floor * secondNumInts
            val secondCeiling = ceiling * secondNumInts
            val firstFloor = maxOf(floor * firstNumInts, sum - secondCeiling)
            val firstCeiling = minOf(ceiling * firstNumInts, sum - secondFloor)
            val firstRange = firstFloor..firstCeiling
            val secondRange = secondFloor..secondCeiling
            debugPrint("firstRange " + firstRange.toString(), indent)
            debugPrint("secondRange " + secondRange.toString(), indent)

            val firstSum = firstRange.random()
            val secondSum = sum - firstSum
            debugPrint("Loop iter M1=" + firstSum.toString() + " M2=" + secondSum.toString(), indent)

            val firstArray = findRandomSetOfIntsWithGivenSumRecurse(
                firstSum, firstNumInts, floor, ceiling, indent + "  ")

            val secondArray = findRandomSetOfIntsWithGivenSumRecurse(
                secondSum, secondNumInts, floor, ceiling, indent + "  " )

            resultArray = firstArray + secondArray
        }
        return resultArray
    }  // end recursive function

    public fun debugOn() {
        debug = true
    }

    public fun debugOff() {
        debug = false
    }

    /**
     * If the class member var "debug" is true, prints text to Log.d, preceded by the class
     * member constant TAG and a space char. If debug is false, this does nothing.
     * @param text  The string to be printed.
     */
    private fun debugPrint (text : String) {
        if (debug) {
            Log.d(TAG, text)
        }
    }

    /**
     * If the class member var "debug" is true, prints text to Log.d, preceded by the class
     * member constant TAG, a space char, and indentChars. If debug is false, this fun does nothing.
     * @param text         The string to be printed.
     * @param indentChars  This string is printed in front of the main text.
     *                     The parameter indentChars is intended to be used as space characters to
     *                     indent the debug prints more and more as we dive into recursive functions.
     */
    private fun debugPrint (text : String, indentChars : String) {
        if (debug) {
            Log.d(TAG, " " + indentChars + text)
        }
    }

} // end of class
