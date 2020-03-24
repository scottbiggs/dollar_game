package sleepfuriously.com.dollargame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates combinations with very specific parameters.
 *
 * todo: usage dox
 */
@Deprecated
public class MyCombinationGenerator {

    @SuppressWarnings("unused")
    private static final String TAG = MyCombinationGenerator.class.getSimpleName();

    /** temp var to hold all the numbers to be used for the combinations */
    private static List<Integer> mRangeList = null;

    /** Temporary var to hold the list of combinations (a list of lists!) */
    private static List<List<Integer>> mCombinationsList = null;


    /**
     * This is the main purpose of this class.
     * Estimated complexity: O(howMany ^ (ceiling - floor)). Ugh.<br>
     *<br>
     * NOTE: May need to put calls to this in a separate process.<br>
     * <br>
     * side-effects:<br>
     *     mRangeList<br>
     *
     *
     * @param howMany     The number of ints
     *
     * @param sum     The target sum
     *
     * @param floor     Lower bound
     *
     * @param ceiling     Upper bound
     *
     * @return  A list of lists.  Each sub list will contain n integers
     *          in the range [r..s] that add up to m.  Also, each sub
     *          list will be unique from the others (order doesn't matter).
     *          And numbers may be repeated.
     */
    public static List<List<Integer>> getSums(int howMany, int sum,
                                              int floor, int ceiling) {

        // works by side-effect.  After this call, mCombinationsList will hold all the possible combinations.
        getAllCombinations(howMany, floor, ceiling);

        // now only keep the ones that add to our given sum.
        List<List<Integer>> correctSumList = new ArrayList<>();
        for (List<Integer> list : mCombinationsList) {
            int tmpSum = 0;
            for (int i : list) {
                tmpSum += i;
            }
            if (tmpSum == sum) {
                correctSumList.add(list);
            }
        }

        return correctSumList;
    }


    /**
     * Intermediary calculation for {@link #getSums(int, int, int, int)}.
     * This returns ALL possible combinations with the following parameters:<br>
     *<br>
     * side effects:<br>
     *      mRangeList  - Will hold a copy of all the integers in the range
     *                  [floor..ceiling].
     *      mCombinationsList
     *
     * @param howMany   Number of integers in each list
     *
     * @param floor     Bottom number
     *
     * @param ceiling   Top number
     *
     * @return  A list of lists that comprise all combinations with howMany
     *          integers in range [floor..ceiling].  Numbers may be repeated.
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public static List<List<Integer>> getAllCombinations(int howMany,
                                                         int floor, int ceiling) {
        mCombinationsList = new ArrayList<>();
        int[] got = new int[howMany];

        makeList(floor, ceiling);
        choose(got, 0, howMany, 0, mRangeList.size());

        return new ArrayList<>(mCombinationsList);
    }

    /**
     * Like above, but uses a brute-force method of finding all combinations (with repeats, order not important).<br>
     * <br>
     * For example: if howMany is 2, and items is {1, 2, 3}, then this will return
     * {{1 1} {1 2} {1 3} {2 2} {2 3} {3 3}}.
     *
     *          Will always return
     *          this many items:<br>
     *<br><code>
     *          (n + k - 1)!<br>
     *          ------------<br>
     *           n!(k - 1)!<br>
     *</code><br>
     * where n is howMany and k is the number of items.
     *
     * @param howMany   The size of each list.
     *
     * @param items     List of the items to choose from.
     *
     * @return  A list of all the possible lists that can be made from the items of
     *          length howMany (with repeats, order not important).
     *          Returns NULL if out of memory (numbers too large).
     */
    public static int[][] getAllCombinations2(int howMany, List<Integer> items) {

        // first, calculate the number of lists needed so that the size of our array
        // will be correct
        int n = howMany;
        int k = items.size();

        int numLists = fact(n + k - 1) / (fact(n) * fact(k - 1));
        Log.d(TAG, "numLists = " + numLists);

        int[][] allCombos;
        try {
            allCombos = new int[numLists][howMany];
        }
        catch (OutOfMemoryError e) {
            Log.e(TAG, "Error allocating array in getAllCombinations2!");
            e.printStackTrace();
            return null;
        }

        // Consider an array of n + k - 1 booleans.  This represents a single way of choosing
        // n things out of k items with repetition. Call it pickArray.
        PickArray pickArray = new PickArray(n + k - 1);

        // This pick array will essentially tell us how to create a single combination.
        // It's kind of complicated to explain (at least it was to me), so see this:
        // https://www.mathsisfun.com/combinatorics/combinations-permutations.html,
        // the section 1. Combinations with Repetition.

        // Assign all the possible combinations of pickArray
        for (int i = 0; i < numLists; i++) {

            // use the current pickArray to make an list (actually an array) using the
            // technique described above.
            for (int j = 0, count = 0; j < pickArray.mSize; j++) {
                // stop if we've done howMany already--no need to continue once we've
                // filled our list (array!).
                if (count == howMany) {
                    break;
                }

                if (pickArray.get(j) == true) {
                    allCombos[i][count] = items.get(j);
                    count++;
                }
            }

            pickArray.increment();
        }

        return allCombos;
    }



    /**
     * Finds the factorial of the give number.
     * Funny how this isn't a basic Math function.
     */
    public static int fact(int n) {
        int product = 1;
        for (int i = 2; i <= n; i++) {
            product *= i;
        }
        return product;
    }

    /**
     * Another helper function.  Creates a list of consecutive integers from
     * a given floor and ceiling.
     *
     * side effects:
     *      mRangeList  Will hold a copy of the returned list.
     *
     * @param floor     The bottom number
     *
     * @param ceiling   The top number
     *
     * @return  A list of integers in the range [floor..ceiling].
     */
    @SuppressWarnings("UnusedReturnValue")
    private static List<Integer> makeList(int floor, int ceiling) {

        List<Integer> list = new ArrayList<>();

        for (int i = floor; i < ceiling + 1; i++) {
            list.add(i);
        }

        mRangeList = new ArrayList<>(list);

        return list;
    }


    /**
     * Loosely based on some C code found at http://rosettacode.org/wiki/Combinations_with_repetitions#C.
     * So many side-effects, yuck!.<br>
     *<br>
     * Difficult to calculate order of complexity because of the recursion AND iteration, but it
     * works!  That's what I get for cobbling together pieces of code.  Estimated O(n^n)
     * where n is maxTypes.<br>
     *<br>
     * side-effects<br>
     *  mRangeList<br>
     *  mCombinationsList
     *
     * @param got       temp storage. needs to have at least len elements
     * @param n_chosen  Number of items already chosen (in the 'got' array)
     * @param len
     * @param at
     * @param maxTypes
     *
     * @return  The number of the different listings.
     */
    public static long choose(int[] got, int n_chosen, int len, int at, int maxTypes) {

        int count = 0;
        if (n_chosen == len) {
            if (got == null)
                return 1;

            List<Integer> list = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                int fromListIndex = got[i];
                list.add(mRangeList.get(fromListIndex));
            }
            mCombinationsList.add(list);
            return 1;
        }

        for (int i = at; i < maxTypes; i++) {
            if (got != null)
                got[n_chosen] = i;

            count += choose(got, n_chosen + 1, len, i, maxTypes);
        }

        return count;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//    public class GFG {
//        /**
//         * arr[] ---> Input Array
//         * chosen[] ---> Temporary array to store indices of
//         *               current combination
//         * start & end ---> Staring and Ending indexes in arr[]
//         * r ---> Size of a combination to be printed
//         */
//        void CombinationRepetitionUtil(int chosen[], int arr[],
//                                              int index, int r, int start, int end) {
//            // Since index has become r, current combination is
//            // ready to be printed, print
//            if (index == r) {
//                for (int i = 0; i < r; i++) {
//                    System.out.printf("%d ", arr[chosen[i]]);
//                }
//                System.out.printf("\n");
//                return;
//            }
//
//            // One by one choose all elements (without considering
//            // the fact whether element is already chosen or not)
//            // and recur
//            for (int i = start; i <= end; i++) {
//                chosen[index] = i;
//                CombinationRepetitionUtil(chosen, arr, index + 1,
//                        r, i, end);
//            }
//            return;
//        }
//
//        /**
//         * The main function that prints all combinations of size r
//         * in arr[] of size n with repitions. This function mainly
//         * uses CombinationRepetitionUtil().
//         */
//        void CombinationRepetition(int arr[], int n, int r) {
//            // Allocate memory
//            int chosen[] = new int[r + 1];
//
//            // Call the recursice function
//            CombinationRepetitionUtil(chosen, arr, 0, r, 0, n - 1);
//        }
//
//        // Driver program to test above functions
//        public void main(String[] args) {
//            int arr[] = {1, 2, 3, 4};
//            int n = arr.length;
//            int r = 2;
//            CombinationRepetition(arr, n, r);
//        }
//    }


    /**
     * A PickArray is essentially an array of booleans and some useful
     * methods attached to it.
     *
     * A PickArray is essentially a variably sized binary number, where false
     * is 0 and true is 1.  It's a convenience data for figuring out combinations
     * with repetition.
     */
    static class PickArray {

        boolean[] mPickArray;
        int mSize;

        /**
         * The size is essentially the number of bits for this binary
         * number.  It is literally the number of booleans in the pickArray.
         */
        PickArray(int _size) {
            mSize = _size;
            mPickArray = new boolean[mSize];
            zero();
        }

        /**
         * Resets the array to all false (0).
         */
        void zero() {
            for (int i = 0; i < mSize; i++) {
                mPickArray[i] = false;
            }
        }

        /**
         * Returns the value of the boolean at the given index
         * (starts at zero of course).
         */
        boolean get(int index) {
            return mPickArray[index];
        }

        /**
         * Returns TRUE iff the value is max (all true).
         */
        boolean isMax() {
            for (int i = 0; i < mSize; i++) {
                if (mPickArray[i] == false) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Increase the pickArray by 1.  If at max, then do nothing.
         */
        void increment() {
            if (isMax()) {
                return; // do nothing
            }
            for (int i = mSize - 1; i >= 0; i--) {
                if (mPickArray[i] == false) {
                    mPickArray[i] = true;
                    return; // done
                }
                mPickArray[i] = false;
            }
        }

        /**
         * Allows you to directly set the values of pickArray.
         *
         * @param bools     Be nice, and try to match the number of
         *                  params to the size!
         */
        void set(boolean... bools) {
            for (int i = 0; i < bools.length; i++) {
                mPickArray[i] = bools[i];
            }
        }

    } // class PickArray


}
