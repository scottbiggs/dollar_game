package sleepfuriously.com.dollargame.view;

import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.Set;

import sleepfuriously.com.dollargame.model.Graph;

/**
 * Extends the Graph class to handle NodeButtons.
 *
 * Unlike the Graph class, this does not use templates.
 * The data will always be a NodeButton.
 */
public class NodeButtonGraph extends Graph
        implements Iterable {



    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @NonNull
    @Override
    public Iterator iterator() {

        Iterator iterator = new Iterator() {

            private Set<Integer> keySet = mNodes.keySet();
            private Iterator<Integer> keySetIterator = keySet.iterator();

            @Override
            public boolean hasNext() {
                return keySetIterator.hasNext();
            }

            @Override
            public Object next() {
                int key = (Integer) keySetIterator.next();
                return mNodes.get(key);
            }
        };

        return iterator;
    }


}
