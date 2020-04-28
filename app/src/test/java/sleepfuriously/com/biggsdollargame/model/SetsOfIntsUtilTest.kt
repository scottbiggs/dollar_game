package sleepfuriously.com.biggsdollargame.model

import org.junit.Assert.*
import org.junit.Test

import sleepfuriously.com.biggsdollargame.model.SetsOfIntsUtil

/**
 * Tests the SetsOfIntsUtil methods.
 */
class SetsOfIntsUtilTest {

    @Test
    fun findRandomSetOfIntsWithGivenSum() {

        val sum = 1
        val numToSum = 20
        val floor = -3
        val ceiling = 3

        val test = SetsOfIntsUtil()
        val nums = test.findRandomSetOfIntsWithGivenSum(sum, numToSum, floor, ceiling)

        // check that each are with the range
        nums?.forEach {
            assertTrue((it >= floor) && (it <= ceiling))
        }

        // check that the sum is correct
        var testSum = 0
        nums?.forEach {
            testSum += it
        }
        assertEquals(sum, testSum)
    }

}