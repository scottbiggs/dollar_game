package sleepfuriously.com.dollargame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Practice class.  This is for generating permutations of lists
 * of Integers.
 */
public class MyPermutations {

    /**
     * Returns the next item in lexicographical order of a given
     * set and the current list.  Useful for calculating permutations.
     *
     * Note that this is the Repeat version--it allows list elements to
     * repeat.
     *
     * If the given list is the LAST possible, it is returned.
     * It's the resposibility of the caller to check for this.
     *
     * If the current list is empty, an exception is thrown.
     *
     * @param items     The set of all the possible items.  They should be in
     *                  lexicographical order (in this case, always increasing ints).
     *
     * @param current   The current list.  We want to find the next one (if possible).
     */
    public static List<Integer> nextLexicographicalOrder(List<Integer> items,
                                                         List<Integer> current) {

        // What if the list is empty?
        if (current.size() == 0) {
            throw new ArrayIndexOutOfBoundsException("nextLexicographicalOrder() was called with an empty current list!");
        }

        // real base case: list size is just 1 (or less!)
        if (current.size() == 1) {
            Integer nextItem = getNextItem(items, current.get(0));
            if (nextItem == null) {
                // already at the end, return it.
                return current;
            }
            current.set(0, nextItem);
            return current;
        }

        // now we count.  This is trickier than it looks.  Start with the last
        // digit and increment it, which should work just fine.
        List<Integer> nextList = new ArrayList<>(current);
        int lastIndex = nextList.size() - 1;

        Integer nextItem = getNextItem(items, nextList.get(lastIndex));
        if (nextItem == null) {

            // time to recurse.  Try the same thing with the end chopped off
            // and then adding the first item in the list to the end.
            List<Integer> shortenedList = new ArrayList<>(nextList);
            shortenedList.remove(shortenedList.size() - 1);

            List<Integer> tmpList = new ArrayList<>(shortenedList);
            shortenedList = nextLexicographicalOrder(items, shortenedList);
            if (tmpList.equals(shortenedList)) {
                // was unable to find a next, so we're at the last possible
                // lexicographic order.  Return what we have.
                return nextList;
            }

            shortenedList.add(items.get(0));  // add the next digit   (allows for repeats)
            return shortenedList;
        }

        // simply change the last digit to the next item.
        nextList.set(lastIndex, nextItem);
        return nextList;
    }

    /**
     * Just like {@link #nextLexicographicalOrder(List, List)}, except that this
     * version does not allow items to be repeated.
     */
    public static List<Integer> nextLexicographicOrderNoRepeats(List<Integer> items,
                                                                List<Integer> current) {
        // todo (not quite the same as we need to spend more effort checking for repeats
        return null;
    }


    /**
     * Given an ordered list and a number that appears within that list,
     * return the next item in that list.
     *
     * If the given item is the last in the list, return null.
     *
     * If the given item does not appear, throw an exception.
     *
     * NOTE:  the items list must not contain repeats!
     */
    private static Integer getNextItem(List<Integer> items, int givenItem) {
        // check for last
        if (items.get(items.size() - 1) == givenItem) {
            return null;
        }

        for (int i = 0; i < items.size() - 1; i++) {
            if (items.get(i) == givenItem) {
                return items.get(i + 1);
            }
        }

        throw new ArrayIndexOutOfBoundsException("getNextItem() could not find " + givenItem +
                    " in the items list!");
    }

    /**
     * Just like {@link #getNextItem(List, int)}, but this does not
     * allow repeats.  For example, finding the next item after 3
     * within the source list of {1, 2, 3, 4, 5} and the list of
     * usedItems of {1, 3, 4} will yield ==> 5 (not 4) since 4 is
     * already used.
     */
    private static Integer getNextItemNoRepeats(List<Integer> items,
                                                List<Integer> usedItems,
                                                int givenItem) {
        Integer nextItem = getNextItem(items, givenItem);
        if (nextItem == null) {
            return null;
        }

        // check to see if nextItem is in usedItems.  If so, try again.
        if (usedItems.contains(nextItem)) {
            return getNextItemNoRepeats(items, usedItems, nextItem);
        }
        return nextItem;
    }


    /**
     * determines if the first first list is less than (comes BEFORE in
     * lexicographical order) the second list.
     *
     * Note: this will return FALSE if the items are the same.
     *
     * Note2: Will throw ArithmeticException if the lists are of different lengths
     * (yeah, I'm lazy  todo: make my own exception for this
     */
    public static boolean isLessThan (List<Integer> first, List<Integer> second) {

        if (first.size() != second.size()) {
            throw new ArithmeticException("tried to compare lists of different size in isLessThan()");
        }

        for (int i = 0; i < first.size(); i++) {
            int firstItem = first.get(i);
            int secondItem = second.get(i);
            if (firstItem < secondItem) {
                return true;
            }
            else if (firstItem > secondItem) {
                return false;
            }
        }
        return false;
    }



}
