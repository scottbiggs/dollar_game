package sleepfuriously.com.dollargame.model;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Latest attempt at solving the problem of generating values for the
 * nodes.
 *
 * I have the theory all set up--it should work now.  And it should be
 * pretty fast and use nearly no memory.
 *
 * Metaphor:    This uses the ice-cream shop metaphor.  Your bowl will hold
 *              precisely so many scoops of ice cream (bowlSize), and the
 *              store has a certain number of flavors to choose from (numFlavors).
 *
 * USAGE:
 *  Call {@link #getRandomSum(int, int, int, int)}, supplying the bowlSize, the
 *  sum, the bottom-most number (floor) and the top-most number (ceiling).  You'll
 *  get back an array of lenght bowlSize, filled with that many numbers chosen
 *  from your specifications, and they will all add up to sum.  That's it.
 *
 *  Oh yeah, there's a public method, test() that goes through and tests everything.
 *  It'll throw some exceptions if there's a real error, otherwise it'll print a ton
 *  of debug statements.
 */
public class TheNewRandomSum {

    //----------------------------------
    //  constants
    //----------------------------------

    private static final String TAG = TheNewRandomSum.class.getSimpleName();


    //----------------------------------
    //  data
    //----------------------------------


    //----------------------------------
    //  methods
    //----------------------------------

    /**
     * Creates a list of ints that add to the given sum.  This is the main
     * point of this class.  This list will be generated as a random draw
     * from all the possible lists (combinations) that satisfy the sum.
     *
     * @param bowlSize  The number of ints in the return list.
     *
     * @param sum       The number that the items in the list will add up to.
     *
     * @param floor     The lowest possible value of any number in the list.
     *
     * @param ceiling   The highest possible value.
     *
     * @return  A list of ints that add up to the given sum.  If no possible
     *          list exists, then an empty list is returned.
     *          If the length + (ceiling - floor) > 32, then empty list is
     *          returned as well (too big!).
     */
    public static int[] getRandomSum(int bowlSize, int sum, int floor, int ceiling) {

        int[] emptyArray = new int[0];

        // generate all the possible ints and put 'em into an array
        int[] possibilities = makePossibleIntsArray(floor, ceiling);

        if ((possibilities == null) || (possibilities.length == 0)) {
            // nothing to do, so exit with nothing
            return emptyArray;
        }
        int numFlavors = possibilities.length;


        // Create the long that will be used as a boolean array
        int booleanArraySize = bowlSize + numFlavors - 1;
        if (booleanArraySize > 32) {
            // too many possibilities for this program to handle.
            Log.e(TAG, "Too many possibilities in getRandomSum(); booleanArraySize = " + booleanArraySize);
            return emptyArray;
        }

        // how many possible combinations are there for our settings?
        long numCombinations = numCombinations(bowlSize, numFlavors);
        Log.d(TAG, "getRandomSum(): combinations: " + numCombinations);

        // generate the first combination for the boolean array.  This should have
        // bowlSize number of bits set.
        long booleanArray = getFirstBooleanArray(bowlSize);


        // go through all combinations; first time is simply to count them.
        int count = 0;
        int found = 0;
        int[] possibilityArray = new int[bowlSize];
        do {
            convertBooleanArrayToCombination(bowlSize, booleanArray, possibilities, possibilityArray);
            if (arrayAddsUp(possibilityArray, sum)) {
                found++;
            }
            booleanArray = nextSnoob(booleanArray);
            count++;
        } while (count < numCombinations);

        Log.d(TAG, "getRandomSum(): finished first loop. found = " + found);

        // Pick a random number from 0 to found.  This will be our target.
        if (found == 0) {
            Log.e(TAG, "no found possibilities!");
            return emptyArray;
        }
        int target = new Random().nextInt(found);


        // loop again, this time stopping once we reach the target found object.
        booleanArray = getFirstBooleanArray(bowlSize);
        found = 0;
        count = 0;
        do {
            convertBooleanArrayToCombination(bowlSize, booleanArray, possibilities, possibilityArray);
            if (arrayAddsUp(possibilityArray, sum)) {
                if (found == target) {
                    // this is IT!  get our array and return it immediately.
                    return possibilityArray;
                }
                found++;
            }

            booleanArray = nextSnoob(booleanArray);
            count++;
        } while (count < numCombinations);

        Log.e(TAG, "Error in getRandomSum()!  Randomly chosen list not found!");
        return null;    // should cause an error--that'll get their attention
    }


    /**
     * Adds the items in the array.  If that equals the given sum,
     * return true.  Else return false.
     */
    private static boolean arrayAddsUp(int[] array, int sum) {
        int accumulator = 0;
        for (int item : array) {
            accumulator += item;
        }
        return accumulator == sum;
    }


    /**
     * Operating on the given boolean array of instructions, this creates an
     * array of elements from the element list.  This is the final part of
     * making a combination of elements.
     *
     * @param bowlSize  Tells how long the final list will be.
     *
     * @param booleanArray  A long that's treated as a binary array.  The bits
     *                      going right-to-left indicated instructions on how
     *                      to place items from the elements param into the
     *                      final list.
     *
     * @param elementsArray A list of all possible items that could be used for the
     *                      final list.
     *
     * @param chooseArray   The return value.  This needs to be an int array initialized
     *                      to bowlSize.  It'll be filled with the proper items from
     *                      elementsArray in the order specified by the booleanArray.
     */
    private static void convertBooleanArrayToCombination(int bowlSize, long booleanArray,
                                                         int[] elementsArray,
                                                         int[] chooseArray) {

        // Strategy:
        //  Start with our boolean array.  Consider it as a set of
        //  instructions on how to pick items from the element list.
        //
        //  Starting with the first (right-most) boolean, if it is TRUE,
        //  then place the first item in mElementArray in our combination array.
        //  If it is false, then move on to the next element in the mElementArray (going left).
        //  True means place that item in our combination array, false means to move to the
        //  next.  Continue doing this until our combination array is full.
        //
        //  Note that the chooseArray will be filled going left-to-right (least significant
        //  to most significant) whereas crawling through the elementsArray is right-to-left.
        //  Sorry for the confusion, but that's how it is.

        int chooseArrayPos = 0;
        int elementArrayPos = 0;
        int boolArrayPos = 0;

        do {
            boolean bit = getBit(boolArrayPos, booleanArray);
            if (bit == true) {
                chooseArray[chooseArrayPos++] = elementsArray[elementArrayPos];
            }
            else {
                elementArrayPos++;
            }
            boolArrayPos++;
        } while (chooseArrayPos < bowlSize);

    }


    /**
     * Helper method to figure out the value of a certain bit in a long.
     *
     * NOTE:  This goes RIGHT-TO-LEFT!  The order starts with the least-significant
     * bit, not the most significant.  This may be counter-intuitive, but it fits this
     * application well.
     *
     * @param pos   The position in question, starting at the right!
     *              So pos = 0 means the right-most (least-significant) bit.
     *
     * @param x     The 32-bit number representing our bits.
     *
     * @return  True means that there was a one at that location.
     *          False means there was a zero.
     */
    private static boolean getBit(int pos, long x) {
        // strategy:  shift the number to the right pos times, then AND it with
        // the number 1.  If the result is one, return true, else return false.
        long shifted = x >> pos;
        return (shifted & 1) == 1 ? true : false;
    }


    /**
     * Creates an array of all the integers within the given bounds.
     * For example, if floor = -2, and ceiling = 5, then [-2, -1, 0, 1, 2, 3, 4, 5]
     * will be returned.
     *
     * @param floor     Bottom number
     *
     * @param ceiling   Top number
     *
     * @return  An array containing all the numbers in the describe bounds.
     *          Will return null if floor > ceiling or ceiling - floor > 32.
     *          Yes, it'll return an empty array if floor = ceiling.
     */
    private static int[] makePossibleIntsArray(int floor, int ceiling) {
        if (floor > ceiling) {
            return null;
        }

        int size = ceiling - floor;
        if (size > 32) {
            Log.e(TAG, "Too big a range for makePossibleInts()!");
            return null;
        }

        int[] possibilities = new int[size + 1];
        for (int i = 0; i <= size; i++) {
            possibilities[i] = floor + i;
        }

        return possibilities;
    }

    /**
     * Given the quantity of bits that need to be set, this returns the
     * long with the smallest value that has that many bits set.
     *
     * For example: if numBitsOn = 4, then this will return 0000 0000 0000 1111
     * (or the number 15).
     *
     * Note that this methods starts from the right (least significant bit)
     * and works its way to the left.
     *
     * @param numBitsOn     The total number of bits that should be
     *                      set.  MUST be 1 or greater!
     *
     * @return  a long that represents a boolean array such that it
     *          contains numBitsOn bits that are set in the right-most
     *          (least significant or smallest) configuration.
     */
    private static long getFirstBooleanArray(int numBitsOn) {

        // The number that we're going to return.  Think of this
        // not as a long, but as an array of bits.
        long booleanArray = 1L;

        for (int i = 0; i < numBitsOn - 1; i++) {
            booleanArray <<= 1;
            booleanArray += 1;
        }

        return booleanArray;
    }


    /**
     * Returns the number of combinations possible with the current
     * setup.  Note that bowlSize corresponds to b, and numFlavors is n.<br>
     * <br>
     *      (n + b - 1)!<br>
     *      ------------<br>
     *       b!(n - 1)!<br>
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    private static long numCombinations(int bowlSize, int numFlavors) {

        long n = numFlavors;
        long b = bowlSize;

        double numerator = fact(n + b - 1);

        double denom1 = fact(b);
        double denom2 = fact(n - 1);

        double denominator = denom1 * denom2;

        double retVal = numerator / denominator;

        return (long) retVal;

    }


    private static double fact(long n) {
        double product = (double) n;
        for (long i = 2; i < n; i++) {
            product *= (double) i;
        }
        return product;
    }

    /**
     * Generates the next binary numbers with the same number of set bits.  The name
     * comes from Set Number Of Ones Bits.
     *
     * From https://www.geeksforgeeks.org/next-higher-number-with-same-number-of-set-bits/
     *
     * @param x     The binary number to start with.
     *
     * @return  The next binary number with the same number of 1s (set bits).
     */
    private static long nextSnoob(long x) {

        if (x == 0) {
            return x;   // Can't do anything with this!
        }

        // right-most set bit
        long rightOne = x & -x;

        // reset the pattern and set the next higher bit.
        // left part of x will be here.
        long nextHigherOneBit = x + rightOne;

        // isolate pattern
        long rightOnesPattern = x ^ nextHigherOneBit;

        // right adjust pattern
        rightOnesPattern = rightOnesPattern / rightOne;

        // correction factor
        rightOnesPattern >>= 2;

        // integrate new pattern
        return nextHigherOneBit | rightOnesPattern;
    }


    /**
     * Run this to test this class.
     */
    public static void test() {

        long tenFact = (long) TheNewRandomSum.fact(10);
        if (tenFact != 3628800) {
            Log.e(TAG, "error testing: fact(10) is incorrect!");
            throw new UnknownError("fact(10) is incorrect!");
        }

        int sevenFact = (int) TheNewRandomSum.fact(7);
        if (sevenFact != 5040) {
            Log.e(TAG, "error testing: fact(7) is incorrect!");
            throw new UnknownError("fact(7) is incorrect!");
        }

        long numCombos = numCombinations(3, 5);
        if (numCombos != 35) {
            Log.e(TAG, "error testing: numCombinations(3, 5) is incorrect!");
            throw new UnknownError("numCombinations(3, 5) is incorrect! Returns " + numCombos + " instead of 35");
        }

        long numCombos2 = numCombinations(6, 10);
        if (numCombos2 != 5005) {
            Log.e(TAG, "error testing: numCombinations(6, 10) is incorrect!");
            throw new UnknownError("numCombinations(6, 10) is incorrect! Returns " + numCombos2 + " instead of 3003");
        }


//        List<Integer> testList = getRandomSum(5, 0, -5, 5);
//        Log.d(TAG, "getRandomSum(5, 0, -5, 5) = " + testList.toString());
//        testList = getRandomSum(20, 0, -5, 5);
//        Log.d(TAG, "getRandomSum(20, 0, -5, 5) = " + testList.toString());
        int[] testArray = getRandomSum(5, 0, -5, 5);
        Log.d(TAG, "getRandomSum(5, 0, -5, 5) = " + Arrays.toString(testArray));
        testArray = getRandomSum(20, 0, -5, 5);
        Log.d(TAG, "getRandomSum(20, 0, -5, 5) = " + Arrays.toString(testArray));
    }

}
