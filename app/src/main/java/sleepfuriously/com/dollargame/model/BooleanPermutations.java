package sleepfuriously.com.dollargame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a way to find all the permutations of a
 * specific number of booleans.
 *
 * Let's say we have n booleans.  k are true (of course n - k are false).
 * What are all the permutations?
 *
 * For example, let n = 5 and k = 2.  Here are the possibilities:
 *
 * 00011
 * 00101
 * 00110
 * 01001
 * 01010
 * 01100
 * 10001
 * 10010
 * 10100
 * 11000
 *
 * I'm not sure exactly how to classify this, but it's clearly a
 * permutation (not a combination) as order matters.  It's not a straight
 * permutation, because I'm limiting the quantities of each (you could
 * say I'm drawing from a limited set of resources)
 *
 * Oh well, nomenclature aside, this is the class to do it.
 */
@Deprecated
public class BooleanPermutations {

    //-----------------------------
    //  constants
    //-----------------------------

    private static final String TAG = BooleanPermutations.class.getSimpleName();

    //-----------------------------
    //  data
    //-----------------------------

    /**
     * The main list of lists.  Holds all the permutations that fit the
     * given parameters.
     */
    private List<List<Boolean>> mListOfPermutations;

    /** The number of elements each permutation contains */
    private int mLengthOfEachPermutation;

    /** Each permutation will have exactly this many trues */
    private int mNumTruesInEachPermutation;

    //-----------------------------
    //  methods
    //-----------------------------

    /**
     * Constructor - Creates a class that you can use to fulfill all your
     * weird boolean needs.  That is, if your boolean needs are limited to
     * lists of very specific lengths with only a certain number of true
     * elements.
     *
     * NOTE:  this does NOTHING (well, almost).  If you want to do the
     * calculations, which may take a while [hint hint], call
     * {@link #calculate()}.
     *
     * @param length    Total length of your list of booleans.
     *
     * @param numTrue   How many of the list need to be true.
     */
    BooleanPermutations(int length, int numTrue) {
        mLengthOfEachPermutation = length;
        mNumTruesInEachPermutation = numTrue;
    }


    /**
     * This is the meat of the class.  I <em>strongly</em> suggest putting
     * this in a separate thread as it may take a while.
     */
    public void calculate() {
        mListOfPermutations = calculateBooleanPermutations(mLengthOfEachPermutation, mNumTruesInEachPermutation);
    }


    /**
     * Returns the list of permutations that have been calculated.  You
     * HAVE calculated them, haven't you?  If not, call {@link #calculate()}
     * to do the hard work of this class before calling this.
     */
    public List<List<Boolean>> getPermutations() {
        return mListOfPermutations;
    }

    /**
     * The main point of this class.  Generates a list of lists representing
     * all the possibilities that satisfies the parameters.
     *
     * NOTE: this may take a while!
     *
     * @param length    The total length (both trues and falses) of each list.
     *
     * @param numTrue   Quantity of TRUE elements in each list.
     *
     * @return  A list of all the possible lists with the given length and
     *          number of true elements.  Could be an empty list if the params
     *          don't make sense (like there are more numTrue than length!).
     */
    private static @NonNull
    List<List<Boolean>> calculateBooleanPermutations(int length, int numTrue) {

        // base cases

        if ((length == 0) || (numTrue > length)) {
            return new ArrayList<>();   // return an empty list
        }

        if (numTrue == 0) {
            List<List<Boolean>> retList = new ArrayList<>();
            retList.add(makeList(false, length));
            return retList;
        }

        if (numTrue == length) {
            List<List<Boolean>> retList = new ArrayList<>();
            retList.add(makeList(true, length));
            return  retList;
        }

        // recurse
        //  The idea is to construct two lists: one starting with a
        //  true, and the other starting with a false. We'll let the
        //  recursed method handle the details.  Once they are done
        //  we then merge the two lists, giving us all that we need!
        List<List<Boolean>> trueList = calculateBooleanPermutations(length - 1, numTrue - 1);
        List<List<Boolean>> falseList = calculateBooleanPermutations(length - 1, numTrue);

        // build the lists
        //  All the true-lists need to have a TRUE prepended at the front.
        //  All the false-lists need to have a FALSE prepended.
        //  Then we need to put the two lists together.

        for (List<Boolean> list : trueList) {
            list.add(0, true);
        }
        for (List<Boolean> list : falseList) {
            list.add(0, false);
        }

        trueList.addAll(falseList); // don't really need to create another variable!
        return trueList;
    }

//    /**
//     * Array version of {@link #getBooleanPermutations(int, int)}. Instead of a list of
//     * lists, this returns a list of arrays.  Should be a little easier on memory and
//     * a little bit faster.
//     *
//     * @param numTrue   Quantity of TRUE elements in each array.
//     *
//     * @param length    The total length (both trues and falses) of each array.
//     *
//     */
//    private static List<boolean[]> getBooleanPermutationsArray(int numTrue, int length) {
//        // base cases
//        if ((length == 0) || (numTrue > length)) {
//            return new ArrayList<>();   // return an empty list
//        }
//        if (numTrue == 0) {
//            List<boolean[]> retList = new ArrayList<>();
//            retList.add(makeArray(false, length));
//            return retList;
//        }
//        if (numTrue == length) {
//            List<boolean[]> retList = new ArrayList<>();
//            retList.add(makeArray(true, length));
//            return  retList;
//        }
//
//        // recurse
//        List<boolean[]> trueList = getBooleanPermutationsArray(numTrue - 1, length - 1);
//        List<boolean[]> falseList = getBooleanPermutationsArray(numTrue, length - 1);
//
//        // build the lists
//
//    }


    /**
     * Constructs a list of booleans that contain length number
     * of elements.  It's just a bunch of the same thing repeated.
     *
     * @param element   The element to repeat.
     *
     * @param length    How many elements.
     */
    private static List<Boolean> makeList(boolean element, int length) {
       List<Boolean> list = new ArrayList<>();
       for (int i = 0; i < length; i++) {
           list.add(element);
       }
       return list;
    }

//    /**
//     * Constructs an array
//     * Array version of {@link #makeList(boolean, int)}.
//     *
//     * @param element   Item to insert
//     *
//     * @param length    Length of the array
//     */
//    private static boolean[] makeArray(boolean element, int length) {
//        boolean[] array = new boolean[length];
//        Arrays.fill(array, element);
//        return array;
//    }


    public void test() {
        calculate();

        calculate();
        List<List<Boolean>> testBoolList = getPermutations();
        Log.d(TAG, "testBoolList size: " + testBoolList.size());
//        for (List<Boolean> list : testBoolList) {
//            Log.d(TAG, "testBoolList = " + list.toString());
//        }
    }

}
