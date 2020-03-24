package sleepfuriously.com.dollargame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic class to help find all combinations with repeats on a list of
 * items. Note that in combinations, ORDER DOES NOT MATTER!
 *
 * Given a list of n items, we want to choose k of them (repeats allowed).
 * The formula for figuring out how many total is pretty straight-forward:
 *
 *      (n + k - 1)!
 *      ------------
 *       k!(n - 1)!
 *
 *  Example:  We want to find out how many ways I can get 3 scoops of ice
 *  cream from the 6 flavors to choose from (my bowl only holds 3 scoops).
 *  Thus n = 6, k = 3...
 *
 *      (6 + 3 - 1)!         8!         40320
 *      ------------   =   -------   =  -----  =  56
 *       3!(6 - 1)!        3! x 5!       720
 *
 *  eg: choc choc choc, choc choc van, choc cho stra, choc choc ban, etc.
 *
 *  This class helps in calculating this number, and to generate ALL those
 *  combinations!
 *
 *  USAGE:
 *  -----
 *      No real calculations are done during instantiating (for speed).
 *      Thus you still MUST call {@link #init(int, int[])} set the k and
 *      the list (actually an array, which holds n elements).
 *
 *      Then call todo
 *
 *  Implementation note:
 *  Because of the enormous numbers potentially involved, many tricks are
 *  applied: all the methods and data are static; recursion is avoided in
 *  favor of loops (even if it makes things very messy), and whatever else
 *  I can think of.
 */
@Deprecated
public class CombinationMaker {

    //------------------------
    //  constants
    //------------------------

    private static final String TAG = CombinationMaker.class.getSimpleName();

    //------------------------
    //  data
    //------------------------

    /**
     * Just like it says, this is how many we are choosing from our list of elements.
     * It represents the letter 'k' in our example above.
     */
    private static int mNumToChoose;

    /**
     * This is the list of items to choose FROM.  It's length represents the letter
     * 'n' in the math above.
     */
    private static int[] mElementArray;

    /**
     * This list is used to help choose items from {@link #mElementArray}.
     * It is essentially an instruction list on how to choose those
     * elements.<br>
     *<br>
     * It will contain (n + k - 1) elements, with exactly k (or {@link #mNumToChoose})
     * being true; all the others will be false.
     */
    private static boolean[] mBooleanArray;

    //------------------------
    // methods
    //------------------------


    //=============================
    //  initializations and settings
    //=============================

    /**
     * Sets the parameters for this class.
     *
     * @param chooseQuantity    The number of elements to choose at a time.
     *
     * @param elementsToChooseFrom  Array of the elements to choose FROM.
     *
     * @throws  OutOfMemoryError    If chooseQuantity + elementsToChooseFrom > 32.
     */

    public static void init(int chooseQuantity, int[] elementsToChooseFrom) {

        if (chooseQuantity + elementsToChooseFrom.length > 32) {
            throw new OutOfMemoryError("Too many items to calculate in CombinationMaker.init( " +
                                       chooseQuantity + ", " + elementsToChooseFrom.length + " )");
        }

        mNumToChoose = chooseQuantity;
        mElementArray = elementsToChooseFrom;

        mBooleanArray = new boolean[mNumToChoose + mElementArray.length - 1];
        initBooleanArray();
    }

    //=============================
    //  useful public methods
    //=============================

    /**
     * Returns the number of combinations possible with the current
     * setup.<br>
     * <br>
     *      (n + k - 1)!<br>
     *      ------------<br>
     *       k!(n - 1)!<br>
     *<br>
     * preconditions:<br>
     *  mNumToChoose - set<br>
     *  mElementList - set<br>
     */
    public static long numCombinations() {
        int n = mElementArray.length;
        int k = mNumToChoose;
        return fact(n + k - 1) / (fact(k) * fact(n - 1));
    }


    /**
     * This is IT, the method you were looking for.
     *
     * Note: I recommend putting this in a seperate thread.  It could take a
     * a while.
     *
     * preconditions:
     *  mNumToChoose
     *  mElementArray
     *
     * side-effects
     *  mBooleanArray   Used for temp storage
     *
     * @return  An list of arrays.  Each sub-array is a combination
     *          of the specified elements and has the specified
     *          length (see {@link #init(int, int[])}).
     *          If no combinations can be made, the list will be empty.
     */
    public static List<int[]> getAllCombinations() {

        // create the binaries needed to generate

        List<int[]> finalList = new ArrayList<>();

        // todo
        // todo
        // todo
        // todo
        // todo
        // todo
        // todo
        // todo
        // todo
        // todo

        return null;
    }


    //=============================
    //  mBooleanArray methods
    //=============================

    /**
     * Does just what it says, converts a given long into a list of
     * booleans.  Could be useful.
     *
     * @param x     The binary to convert.  Assumes that it's 32 bits.
     *
     * @param length    The length of the list. MUST be 32 or less.
     *
     * @param reverse   When TRUE, will revert the list so that the
     *                  least significant bit (right-most) will be the
     *                  FIRST Boolean in the list.  When false will go
     *                  the other direction.
     *
     * @return  A list of length Booleans.  This list will be taken from
     *          the least-significant part of x.
     *          If the requested length is too long, then null is returned.
     */
    private static List<Boolean> convertLongToBooleanList(long x, int length, boolean reverse) {

        if (length > 32) {
            return null;
        }

        List<Boolean> list = new ArrayList<>(length);   // help the memory allocator a bit

        for (int i = 0; i < length; i++) {
            Boolean bit = ((x >> i) & 1) == 1 ? true : false;

            if (reverse) {
                list.add(bit);
            }
            else {
                // left-to right, but using right-most set of bits.
                // For example, if x = 0000 0000 0010 1011 and length = 7,
                // then our list will look like: 0101011 (or ftftftt).
                list.add(0, bit);
            }
        }

        return list;
    }

    /**
     * Just like {@link #convertLongToBooleanList(long, int, boolean)}, except that this
     * returns an array of the given size.
     */
    private static boolean[] convertLongToBooleanArray(long x, int length, boolean reverse) {
        if (length > 32) {
            return null;
        }

        boolean[] array = new boolean[length];

        for (int i = 0; i < length; i++) {
            boolean bit = ((x >> i) & 1) == 1 ? true : false;

            if (reverse) {
                array[i] = bit;
            }
            else {
                array[length - i - 1] = bit;
            }
        }
        return array;
    }


    /**
     * Operating on the current boolean array of instructions, this creates a
     * list of elements from the element list.  This is the final part of
     * making a combination of elements.
     *
     * preconditions:
     *  mNumToChoose    - properly set
     *  mElementArray   - holds the items to be chosen from
     *  mBooleanArray   - needs to be properly setup for this combination
     */
    private static int[] convertBooleanArrayToCombination() {
        // Strategy:
        //  Start with our boolean array.  Consider it as a set of
        //  instructions on how to pick items from the element list.
        //
        //  Starting with the first boolean, if it is TRUE, then place the first
        //  item in mElementArray in our combination array.  If it is false,
        //  then move on to the next element in the mElementArray.  True means
        //  place that item in our combination array, false means to move to the
        //  next.  Do this until our combination array is full.
        //  Again, see https://www.mathsisfun.com/combinatorics/combinations-permutations.html
        //  for a better explanation.

        int[] chooseArray = new int[mNumToChoose];
        int chooseArrayPos = 0;
        int elementArrayPos = 0;
        int boolArrayPos = 0;

        do {
            if (mBooleanArray[boolArrayPos] == true) {
                chooseArray[chooseArrayPos++] = mElementArray[elementArrayPos];
            }
            else {
                elementArrayPos++;
            }
            boolArrayPos++;
        } while (chooseArrayPos < mNumToChoose);

        return chooseArray;
    }

    /**
     * Takes an allocated {@link #mBooleanArray} and resets it to the
     * first possible value.<br>
     * <br>
     * preconditions:<br>
     *  {@link #mBooleanArray} must be allocated.<br>
     *  {@link #mNumToChoose} must be set to the correct number<br>
     *  {@link #mElementArray} should be filled<br>
     *  <br>
     * side-effects:<br>
     *  {@link #mBooleanArray}   reset to initial values
     */
    private static void initBooleanArray() {
        // the initial values will be all true, then all false.
        int i = 0;
        for (; i < mNumToChoose; i++) {     // loop through first group (all true)
            mBooleanArray[i] = true;
        }

        for (; i < mBooleanArray.length; i++) {  // finish with all false
            mBooleanArray[i] = false;
        }
    }

    /**
     * Sets {@link #mBooleanArray} to the items in the given list.
     *
     * @param setList   List of elements that should be the same size
     *                  as the boolean array. (n + k - 1)
     */
    private static void setBooleanArray(boolean[] setList) {
        for (int i = 0; i < setList.length; i++) {
            mBooleanArray[i] = setList[i];
        }
    }


    /**
     * Checks the list {@link #mBooleanArray} and see if it's at its
     * max value or not.<br>
     * <br>
     * pre-conditions:<br>
     *  mBooleanList is initialized.
     *
     * @return  True iff mBooleanList is at its max value.
     */
    private static boolean isBooleanArrayMax() {

        // pardon the opaque code, it's optimized.
        // If the last k elements are true, then we're
        // maxed out. Simple.
        for (int i = mBooleanArray.length - 1;
             i >= mBooleanArray.length - mNumToChoose;
             i--) {
            if (mBooleanArray[i] == false) {
                return false;
            }
        }
        return true;
    }


    /**
     * Works by side-effect on {@link #mBooleanArray}, increments
     * it to the next possible sequence.  If it is already at its max, then
     * nothing is done.
     */
    @Deprecated // todo:  !!! doesn't work !!!
    private static void incrementBooleanArray() {
        if (isBooleanArrayMax()) {
            return; // nothing to do
        }

        int lastMovableTruePos = getPosRightMostMovableTrue(mBooleanArray);
        if (lastMovableTruePos == -1) {
            return; // again, nothing to do
        }

        int firstFalsePos = firstFalseAfter (mBooleanArray, lastMovableTruePos + 1);
        if (firstFalsePos == -1) {
            return; // ibid
        }

        swapBooleanArray(mBooleanArray, lastMovableTruePos, firstFalsePos);
    }


    /**
     * Finds the location of the first false to be found starting at the given index.
     *
     * @param boolArray     Array to search.
     *
     * @param afterPos      Position to start looking for the first false.
     *
     * @return  The position of the first false found.
     *          -1 if no false found.
     */
    private static int firstFalseAfter(boolean[] boolArray, int afterPos) {
        for (int i = afterPos; i < boolArray.length; i++) {
            if (boolArray[i] == false) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Returns the index to the last TRUE element of the given array
     * of booleans.  Will return -1 if no TRUE elements are found.
     */
    private static int rightMostTrue(boolean[] boolArray) {
        // just go backwards and find the first TRUE index.
        for (int i = boolArray.length - 1; i >= 0; i--) {
            if (boolArray[i] == true) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Similar to {@link #rightMostTrue(boolean[])}, but this requires there
     * to be a false to the right of it.  That's what makes it movable.
     *
     * @return  The index of the right-most movable TRUE element.  Will return -1
     *          if none are found.
     */
    private static int getPosRightMostMovableTrue(boolean[] boolArray) {
        boolean falsefound = false;    // have not found a false yet
        for (int i = boolArray.length - 1; i >= 0; i--) {
            if (boolArray[i] == false) {
                falsefound = true;
            }
            else if (falsefound) {
                return i;   // yup, found a TRUE after finding a false.
            }
        }
        return -1;  // not found
    }

    /**
     * Swaps the contents of the array at the two indices.  So if the array were
     * {true, false, false}, and a = 0, b = 2, then the array would be changed
     * to be {false, false, true}.
     *
     * @param boolArray     Array to change
     * @param a     First index
     * @param b     Second index
     */
    private static void swapBooleanArray(boolean[] boolArray, int a, int b) {
        boolean tmp = boolArray[a];
        boolArray[a] = boolArray[b];
        boolArray[b] = tmp;
    }


    //=============================
    //  misc
    //=============================


    /**
     * Finds the factorial of the give number.
     * Funny how this isn't a basic Math function.
     */
    private static int fact(int n) {
        int product = 1;
        for (int i = 2; i <= n; i++) {
            product *= i;
        }
        return product;
    }

    /**
     * Prints the current variables to logcat
     */
    public static void printToLog() {
        String str =  "   k = " + mNumToChoose +
                "   n = " + mElementArray.length +
                "   total combinations = " + numCombinations() + "\n" +
                "   mElementsList: " + Arrays.toString(mElementArray) + "\n" +
                "   mBooleanArray = " + Arrays.toString(mBooleanArray) + "\n" +
                "   isBooleanListMax() = " + isBooleanArrayMax() + "\n" +
                "   rightMostTrue(mBooleanArray) = " + rightMostTrue(mBooleanArray) + "\n" +
                "   rightMostMovableTrue(mBooleanArray) = " + getPosRightMostMovableTrue(mBooleanArray);
        Log.d(TAG, str);
    }


    public static void test() {
        init(4, new int[] {1, 2, 3, 4, 5});
        printToLog();

//        setBooleanArray(new boolean[] {false, false, false, true, true, true, true});
//        printToLog();
//        setBooleanArray(new boolean[] {true, false, false, false, false, false, false});
//        printToLog();
//        setBooleanArray(new boolean[] {false, false, false, false, false, true, true});
//        printToLog();

//        for (int i = 0; isBooleanArrayMax() == false; i++) {
//            incrementBooleanArray();
//            Log.d(TAG, "mBooleanArray is: " + Arrays.toString(mBooleanArray));
//
//            int[] combinationArray = convertBooleanArrayToCombination();
//            Log.d(TAG, "combinationArray = " + Arrays.toString(combinationArray));
//        }

        long t1 = 0b001100;
        long t2 = 0b11001100;
        long t3 = 0b00001111;
//        Log.d(TAG, "t1 = " + Long.toBinaryString(nextSnoob(t1)));
//        Log.d(TAG, "t2 = " + Long.toBinaryString(nextSnoob(t2)));
//        Log.d(TAG, "t3 = " + Long.toBinaryString(nextSnoob(t3)));

//        long test = t3;
//        for (int i = 0; i < 300; i++) {
//            test = nextSnoob(test);
//            Log.d(TAG, "snoob: " + Long.toBinaryString(test));
//        }

//        BooleanPermutations bpTest = new BooleanPermutations(25, 10);
//        bpTest.test();

/*
        List<Boolean> testList_6_true = convertLongToBooleanList(t3, 6, true);
        Log.d(TAG, "testList_6_true: " + Long.toBinaryString(t3));
        Log.d(TAG, " convertLongToBinaryList -> " + testList_6_true.toString());

        List<Boolean> testList_6_false = convertLongToBooleanList(t3, 6, false);
        Log.d(TAG, "testList_6_false: " + Long.toBinaryString(t3));
        Log.d(TAG, " convertLongToBinaryList -> " + testList_6_false.toString());

        boolean[] testArray_6_true = convertLongToBooleanArray(t3, 6, true);
        Log.d(TAG, "testArray_6_true: " + Long.toBinaryString(t3));
        Log.d(TAG, " convertLongToBooleanArray -> " + Arrays.toString(testArray_6_true));

        boolean[] testArray_6_false = convertLongToBooleanArray(t3, 6, false);
        Log.d(TAG, "testArray_6_false: " + Long.toBinaryString(t3));
        Log.d(TAG, " convertLongToBooleanArray -> " + Arrays.toString(testArray_6_false));
*/


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


}
