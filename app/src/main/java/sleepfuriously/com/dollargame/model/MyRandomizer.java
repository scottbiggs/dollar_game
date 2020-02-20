package sleepfuriously.com.dollargame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Implements the special random numbers needed for this game.
 *
 * I need to be able to have an arbitrary length list of numbers
 * that add up to another given number.  In other words: I need
 * to generate N random integers whose sum is M.
 *
 * This uses the singleton pattern.
 */
public class MyRandomizer {

    //---------------------
    //  constants
    //---------------------

    private static final String TAG = MyRandomizer.class.getSimpleName();

    //---------------------
    //  data
    //---------------------

    private volatile static MyRandomizer mInstance = null;

    Random mRand;

    //---------------------
    //  methods
    //---------------------

    private MyRandomizer() {
        mRand = new Random();
    }


    public static synchronized MyRandomizer getInstance() {
        if (mInstance == null) {
            mInstance = new MyRandomizer();
        }
        return mInstance;
    }


    /**
     * Returns a set of all the possible combinations (with repeats) of the
     * numbers in the given range [lower..upper].
     */
    public Set<Set<Integer>> allCombos(int lower, int upper) {

        //  method:
        // Let's say that the lower is -2 and the upper is 1.  That's a total of
        // four items to choose from.  So we use a four-digit binary number to
        // represent all the possible combinations of this set.  And just count.

        int count = upper - lower + 1;
        count = (int) Math.pow(2, count);

        // this is the return set of numbers
        Set<Set<Integer>> retSet = new HashSet<>();



        for (int i = 0; i < count; i++) {
            // this creates a temporary string.  It may not be the correct length,
            // so prepending 0s may be necessary.
            StringBuilder tmpStr = new StringBuilder(Integer.toBinaryString(i));

            while (tmpStr.length() < count) {
                tmpStr.insert(0, "0");
            }

            // now we have a binary string of the correct length. Translate the binary representation
            // to a Set of digits.
            Set<Integer> numSet = new HashSet<>();
            for (int j = 0; j < tmpStr.length(); j++) {
                if (tmpStr.charAt(j) == '1') {
                    Integer num = lower + j;
                    numSet.add(num);
                }
            }

            retSet.add(numSet);
        }

        return retSet;
    }


    /**
     * Creates a list of integers that add up to a given sum.
     * This is the meat of this class.
     *
     * @param howMany   The number of integers in the list
     *
     * @param sum       The sum of all the numbers
     *
     * @param lowerBounds   The lowest possible number (inclusive)
     *
     * @param upperBounds   The highest possible number (inclusive)
     *
     * @return  A list that fulfills the requirements.  In the
     *          case of an impossible demand, null is returned.
     */
    public List<Integer> buildList(int howMany, int sum, int lowerBounds, int upperBounds) {

        // too few items
        if (howMany <= 0) {
            return null;
        }

        List<Integer> finalList = new ArrayList<>();

        // easy case: just one item
        if (howMany == 1) {
            finalList.add(sum);
            return finalList;
        }

        //  Strategy:
        // 1) find N - 1 random numbers within the boundaries.
        // 2) sort them from smallest to largest
        // 3) Use the spaces between them as the numbers
        // 4) calculate the last number
        //
        //  found here:  https://stackoverflow.com/a/29675742/624814

        List<Integer> randomNums = new ArrayList<>();
        for (int i = 0; i < howMany - 1; i++) {
            // randomNums will be zero based (all numbers >= 0)
            randomNums.add(myRand(0, upperBounds - lowerBounds));
        }

        Collections.sort(randomNums);

        finalList.add(randomNums.get(0) - lowerBounds);
        for (int i = 1; i < howMany - 1; i++) {
            int previous = finalList.get(i - 1);
            finalList.add(randomNums.get(i) - previous);
        }

        finalList.add(upperBounds - finalList.get(howMany - 2));

        return finalList;
    }


    /**
     * Returns a random integer within the given bounds [lower, upper] (inclusive).
     */
    private int myRand(int lower, int upper) {
        int r = mRand.nextInt(upper - lower + 1) + lower;
//        Log.d(TAG, "myRand( " + lower + ", " + upper + " ) = " + r);
        return r;
    }


    /**
     * recursive version.
     *
     * Creates a Set of all Sets of given length that can be made of the
     * items in possibilities.
     *
     * In other words, find all combinations of possibilities with the given length
     * (with repetitions).
     */
    public Set<Integer> combinations(Set<Integer> possibilities, int length) {
        // base case
        if ((possibilities == null) || (possibilities.isEmpty())) {
            return possibilities;
        }

        Set<Integer> newSet = new HashSet<>();
        for (Integer i : possibilities) {

        }

        // todo
        return null;
    }

}
