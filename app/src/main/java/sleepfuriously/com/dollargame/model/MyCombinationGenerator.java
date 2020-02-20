package sleepfuriously.com.dollargame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates combinations with very specific parameters.
 *
 *
 */
public class MyCombinationGenerator {

    private static final String TAG = MyCombinationGenerator.class.getSimpleName();

    /** temp var to hold all the numbers to be used for the combinations */
    private static List<Integer> mRangeList = null;

    /** Temporary var to hold the list of combinations */
    private static List<List<Integer>> mCombinationsList = null;




    /**
     * This is the main purpose of this class.
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
     * This returns ALL possible combinations with the following parameters:
     *
     * side effects:
     *      mRangeList  Will hold a copy of all the integers in the range
     *                  [floor..ceiling].
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
    public static List<List<Integer>> getAllCombinations(int howMany,
                                                         int floor, int ceiling) {
        mCombinationsList = new ArrayList<>();
        int[] got = new int[howMany];

        makeList(floor, ceiling);
        choose(got, 0, howMany, 0, mRangeList.size());

        return new ArrayList<>(mCombinationsList);
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
    private static List<Integer> makeList(int floor, int ceiling) {

        List<Integer> list = new ArrayList<>();

        for (int i = floor; i < ceiling + 1; i++) {
            list.add(i);
        }

        mRangeList = new ArrayList<>(list);

        return list;
    }


//    static String[] donuts = {"iced", "jam", "plain", "choc", "kolache", "powdered", "cake", "kruller", "buttermilk"};

    /**
     * Test, found at http://rosettacode.org/wiki/Combinations_with_repetitions#C.
     * I'm trying to port it to java from C.
     *
     * @param got   needs to have at least len elements
     * @param n_chosen
     * @param len
     * @param at
     * @param maxTypes
     *
     * @return  Seems to return the number of the different listings.
     */
    public static long choose(int[] got, int n_chosen, int len, int at, int maxTypes) {

        int count = 0;
        if (n_chosen == len) {
            if (got == null)
                return 1;

            List<Integer> list = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                int fromListIndex = got[i];
//                Log.d(TAG, donuts[fromListIndex]);
                list.add(mRangeList.get(fromListIndex));
            }
//            Log.d(TAG, "---");
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



    public static List<List<Integer>> testChooseStart(int len, int maxTypes) {
        mCombinationsList = new ArrayList<>();
        int[] got = new int[len];

        makeList(-2, 2);    // works by side effect
        long retVal = choose(got, 0, len, 0, maxTypes);


//        Log.d(TAG, "list is " + mCombinationsList.toString());
        Log.d(TAG, "testChooseStart() returns " + retVal);
        return new ArrayList<>(mCombinationsList);
    }




}
