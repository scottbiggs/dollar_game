package sleepfuriously.com.dollargame.model;


/**
 * Describes a node of the graph.
 * Just holds data--that's all.
 */
public class Node {

    //------------------------
    //  data
    //------------------------

    /** The current amount of dollars in this node */
    private int amount;

    /** The number of times this node has give its money away */
    private int giveCount = 0;

    /** Number of times this node has taken money from its neighbors */
    private int takeCount = 0;

    /**
     * Coordinates for this node (for drawing purposes).
     * Go ahead and set these by hand!
     */
    public int x, y;


    //------------------------
    //  constructors
    //------------------------

    public Node() {
        amount = 0;
    }


    public Node(int startAmount){
        amount = startAmount;
    }


    //------------------------
    //  methods
    //------------------------

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Node: " + amount;
    }


    //------------------------
    //  getters & setters
    //------------------------

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getGiveCount() {
        return giveCount;
    }

    public void setGiveCount(int giveCount) {
        this.giveCount = giveCount;
    }

    public int getTakeCount() {
        return takeCount;
    }

    public void setTakeCount(int takeCount) {
        this.takeCount = takeCount;
    }
}
