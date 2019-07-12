package sleepfuriously.com.dollargame.model;

/**
 *	This Exception is thrown whenever the user tries to
 *	add a node with the same ID as an already existing
 *	node.
 */
public class GraphNodeDuplicateIdException extends Exception {
    public GraphNodeDuplicateIdException() {
        super("Exception: NodeId already used.");
    }
    public GraphNodeDuplicateIdException(String s) {
        super(s);
    }
}
