package sleepfuriously.com.dollargame.model;

/**
 * This exception is thrown when someone tries to calculate the
 * genus of a graph that is not connected.
 */
public class GraphNotConnectedException extends Exception {
    public GraphNotConnectedException() {
        super("Exception: Graph is not connected.");
    }

    public GraphNotConnectedException(String s) {
        super(s);
    }

}
