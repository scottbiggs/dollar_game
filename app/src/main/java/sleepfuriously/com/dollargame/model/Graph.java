package sleepfuriously.com.dollargame.model;


import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * todo: use Templates to make the data more generic
 *
 * Library for directed and undirected graphs.
 * Note that this uses lists for its data, so it may
 * not be as fast as it could be.
 *
 *	USAGE:
 *		- When instantiating, provide a Node type to fill
 *		  in the generic T. This will be whatever data you want
 *		  to be held in a node.  I HIGHLY recommend that you override
 *		  the toString() method for this class.  It's used by the
 *		  Graph class' .toString() method.
 *
 *		- Also define whether it's directed (default is undirected).
 *
 *		- Add the nodes, supplying a unique id for that node.
 *
 *		- Add edges.  Use a weight if desired.
 *
 *		- Use the graph as you like.
 */
@SuppressWarnings("ALL")
public class Graph<T>
        implements Iterable<T> {

    //-----------------------
    //	constants
    //-----------------------

    private static final String TAG = "Graph";

    //-----------------------
    //	data
    //-----------------------

    /**
     * Holds the nodes.
     *
     * In this case, a node is a key-value pair of an id and any data
     * associated with it.
     */
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, T> mNodes = new HashMap<>();

    /**
     * Used when traversing the graph so that we don't get caught in
     * loops.
     */
    private List<Integer> mVisited = new ArrayList<>();

    /**
     * Holds all the edges.
     *
     * Note that for a undirected graph, there will be just one
     * edge--startNode and endNode are the same things for them.
     */
    private List<Edge> mEdges = new ArrayList<>();

    /** Tells whether this is a directed graph or undirected (default) */
    private boolean mDirected = false;

    //-----------------------
    //	constructors
    //-----------------------

    /**
     * Basic constructor. Graph will be undirected (default)!
     */
    public Graph() {
    }

    /**
     * Use this constructor to set the Graph as directed.
     * Undirected is the default.
     */
    public Graph(boolean directed) {
        mDirected = directed;
    }


    //-----------------------
    //	methods
    //-----------------------

    /**
     * Returns an id that is guaranteed to be unique from any node already
     * in the Graph.
     *
     * O(n)
     */
    public int getUniqueNodeId() {
        int id = 0;
        while (mNodes.get(id) != null) {
            id++;
        }
        return id;
    }

    /**
     * Add a new node to this graph.
     *
     *	@param	id	A unique id for this node.
     *
     *	@param	nodeData	Some data to store with this node.
     *
     *	@return	The current number of nodes AFTER this one has been added.
     *
     *	@throws	GraphNodeDuplicateIdException	if the id is already used
     *											for a node.
     */
    public int addNode(int id, T nodeData)
            throws GraphNodeDuplicateIdException {
        // check to see if id already exists
        if (mNodes.containsKey(id)) {
            throw new GraphNodeDuplicateIdException();
        }

        mNodes.put(id, nodeData);
        return mNodes.size();
    }

    /**
     * Adds an edge to this class.  Does not allow duplicate edges!
     *
     *	@param	startNodeId	The first the starting node (not relevant
     *						for non-directed graphs).
     *
     *	@param	endNodeId	The ending edge.
     *
     *	@param	weight	The weight of this edge.
     *
     *	@return	The total number of edges AFTER this has been added.
     *          -1 if this is a duplicate edge.
     */
    public int addEdge(int startNodeId, int endNodeId, int weight) {
        if (getEdgeIndex(startNodeId, endNodeId) != -1) {
            Log.e(TAG, "Tried to add duplicate edge!");
            return -1;
        }

        Edge edge = new Edge();
        edge.startNodeId = startNodeId;
        edge.endNodeId = endNodeId;
        edge.weight = weight;

        mEdges.add(edge);
        return mEdges.size();
    }

    /**
     * Finds the edge index with the given start and end nodes.
     * If not found, returns -1;<br>
     * <br>
     * Also useful just to see if an edge exists.<br>
     * <br>
     * Relies on {@link #mDirected} to determine if direction
     * matters.<br>
     * <br>
     * O(n)
     */
    public int getEdgeIndex(int startNodeId, int endNodeId) {
        for (int i = 0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            if ((startNodeId == edge.startNodeId) &&
                (endNodeId == edge.endNodeId)) {
                return i;
            }
            if (!mDirected) {
                if ((startNodeId == edge.endNodeId) &&
                        (endNodeId == edge.startNodeId)) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     *	Just like AddEdge, but without any weight.
     */
    public int addEdge(int startNodeId, int endNodeId) {
        return addEdge(startNodeId, endNodeId, 0);
    }

    /**
     * private util method to simplify a few things
     */
    private int addEdge(Edge edge) {
        mEdges.add(edge);
        return mEdges.size();
    }


    /**
     * Creates an exact duplicate of this graph.
     *
     * Returns NULL if something was wrong with the
     * graph that prevents making a clone (probably
     * a duplicate node ID).
     */
    public Graph clone() {
        Graph<T> newGraph = new Graph<>(mDirected);

        // Copying the nodes is a little tricky as it's base
        // is a HashMap.
        // Get a Set of the keys (aka IDs) and then copy
        // them one-by-one into the new graph
        Set<Integer> ids = mNodes.keySet();
        Iterator<Integer> iterator = ids.iterator();

        try {
            while (iterator.hasNext()) {
                int id = iterator.next();
                T node = mNodes.get(id);
                newGraph.addNode(id, node);
            }
        }
        catch (GraphNodeDuplicateIdException e) {
            // duplicate node id found
            e.printStackTrace();
            return null;
        }

        // The edges are much easier
        for (int i =0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            newGraph.addEdge(edge);
        }

        return newGraph;
    }


    /**
     * Returns a list of all the node IDs adjacent to the given node.
     * If none, this returns an empty list.
     *
     *	O(n)
     *
     * @param	nodeId		The ID of the node in question.
     *
     * @param	directed	True only for directed graphs.
     */
    public List<Integer> getAllAdjacentTo(int nodeId, boolean directed) {

        List<Integer> adjacentList = new ArrayList<>();
        List<Edge> edgeList = getEdges(nodeId);

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            if (directed) {
                if (edge.startNodeId == nodeId) {
                    // This is an edge that stars with our node.
                    // Add the end node to our list.
                    adjacentList.add(edge.endNodeId);
                }
            }
            else {
                // undirected--just include the other node.
                if (edge.startNodeId == nodeId) {
                    adjacentList.add(edge.endNodeId);
                }
                else {
                    adjacentList.add(edge.startNodeId);
                }
            }
        }

        return adjacentList;
    }

    /**
     * Like getAllAdjacentTo(nodeId, directed), but this uses the
     * directedness of the current Graph.
     */
    public List<Integer> getAllAdjacentTo(int nodeId) {
        return getAllAdjacentTo(nodeId, mDirected);
    }


    /**
     * Returns a list of all the node ids for this graph.
     */
    public List<Integer> getAllNodeIds() {
        return new ArrayList<Integer>(mNodes.keySet());
    }

    /**
     * Returns a list of all the Node data.  Note that
     * ids are NOT part of this list!
     *
     * Note that this is a copy of the data.
     */
    public List<T> getAllNodeData() {
        return new ArrayList<T>(mNodes.values());
    }

    /**
     * Returns the data associated with the node id.
     */
    public T getNodeData(int nodeId) {
        return mNodes.get(nodeId);
    }

    /**
     * Returns the id of the first node to match the given
     * data.
     *
     *	@return		The key or null if not found.
     */
    public Integer getNodeId(T data) {
        // Note: the id is also the key
        Set<Integer> keys = mNodes.keySet();
        Iterator<Integer> iterator = keys.iterator();

        while (iterator.hasNext()) {
            int key = iterator.next();
            // Now get the associated data with this key and
            // test it against the input param
            T tmpData = mNodes.get(key);
            if (tmpData.equals(data)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Curious if two nodes are adjacent?  Use this to find out!
     * For undirected graphs, the order doesn't matter.
     */
    public boolean isAdjacent(int startNodeId, int endNodeId) {
        boolean found = false;

        // simply go through our adjacency list and see if there's a match.
        for (Edge edge : mEdges) {
            if ((edge.startNodeId == startNodeId) &&
                    (edge.endNodeId == endNodeId)) {
                found = true;
                break;
            }
            else if (mDirected == false) {
                // special case for undirected graphs
                if ((edge.startNodeId == endNodeId) &&
                        (edge.endNodeId == startNodeId)) {
                    found = true;
                    break;
                }
            }

        }
        return found;
    }


    /**
     * Figures out if this Graph is connected or not.
     *
     * For undirected graphs, this simply means that all the nodes
     * are accessible from any other node (by one or more steps).
     * This is pretty straight-forward and what you'd expect.
     *
     * For directed graphs, I'm using the official term of
     * "weakly connected."  That is, it would be a connected
     * graph were it undirected.
     *
     * Note that a graph with no nodes is NOT connected.
     * And a graph with a just 1 node is connected ONLY if
     * it connects to itself.
     *
     * todo: write a Strongly Connected graph routine, that
     * tells if in a directed graph any node can get to any node.
     */
    public boolean isConnected() {

        // Easy case first.
        if ((mNodes.size() == 0) || (mEdges.size() == 0)) {
            return false;
        }

        // create a list of visited vertices
        List<Integer> visited = new ArrayList<>();

        // start with any old key/ID (since HashMaps are not really ordered).
        int anId = mNodes.keySet().iterator().next();	// finds the "first" key
        isConnectedHelper(anId, visited);

        // if the size of the visited list is the same as our number of
        // nodes, then we'll know that all were visited. This can only
        // happen if the graph is connected!
        if (visited.size() == mNodes.size()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Does a recursive depth-first search of the Graph's edges
     * (assumes that it is undirected!).
     *
     *	@param	nodeId		An unvisited node in the Graph. This method
     *						will find all the edges that it connects to
     *						and so one.
     *
     *	@param	visited		A list of visited nodes. These will be
     *						added to as the nodes are visited.  Yes,
     *						this data structure WILL BE MODIFIED.
     */
    private void isConnectedHelper(int nodeId, List<Integer> visited) {
//		System.out.println("entering isConnectedHelper(" + nodeId + ", " + visited + ")");

        // Start by adding this node to the visited list.
        visited.add(nodeId);

        // For considering connectivity, we always use an undirected graph
        List<Integer> adjacentNodeIds = getAllAdjacentTo(nodeId, false);

        for (Integer adjacentNodeId : adjacentNodeIds) {
            if (visited.contains(adjacentNodeId) == false) {
                // not found in the visited list, do it!
                isConnectedHelper(adjacentNodeId, visited);
            }
        }
    }

    /**
     * Find all the edges that use the given node.
     * If none are found, the returned list will be empty.
     *
     *	O(n)
     *
     * @param	nodeId		The ID of the node in question.
     */
    protected List<Edge> getEdges(int nodeId) {

        List<Edge> edgeList = new ArrayList<>();

        for (int i = 0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            if ((edge.startNodeId == nodeId) || (edge.endNodeId == nodeId)) {
                edgeList.add(edge);
            }
        }

        return edgeList;
    }

    /**
     * Returns the number of nodes in this graph. Hope you didn't make
     * any duplicates!
     */
    public int numNodes() {
        return mNodes.size();
    }

    /**
     * Returns the number of edges in this graph.  For undirected graphs,
     * this may return the count for both A->B and B-> IF YOU WERE DUMB
     * ENOUGH TO ENTER THOSE EDGES!
     */
    public int numEdges() {
        return mEdges.size();
    }


    /**
     * Removes the given node.  This assumes that any edges associated
     * with this node have been PREVIOUSLY removed!  This will cause
     * a total cluster fuck if you don't do this before-hand! You've
     * been warned!
     *
     * But wait, there's more! If there are MORE THAN ONE node with
     * the same id (and there shouldn't!), this will remove only the
     * first that was found.  Really--you should be more careful with
     * your graphs!
     *
     *	@param	id	The id of the node to be removed.
     *
     *	@returns	TRUE if the node was removed.
     *				FALSE if the node can't be found.
     */
    public boolean removeNode(int id) {
        if (mNodes.remove(id) == null) {
            return false;
        }
        return true;
    }

    /**
     *	Removes the specified edge.  For undirected graphs, will
     * try both directions, possibly removing both.
     */
    public boolean removeEdge(int startNodeId, int endNodeId) {
        boolean removed = false;

        for (int i = 0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            if ((edge.startNodeId == startNodeId) &&
                    (edge.endNodeId == endNodeId)) {
                mEdges.remove(i);
                removed = true;
                break;
            }
        }

        // TODO: this shouldn't be necessary!
        if (mDirected) {
            return removed;
        }

        // Undirected, check for other direction
        for (int i = 0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            if ((edge.startNodeId == endNodeId) &&
                    (edge.endNodeId == startNodeId)) {
                mEdges.remove(i);
                removed = true;
                break;
            }
        }
        return removed;
    }

    /** Returns the edge at the given index */
    public Edge getEdge(int index) {
        return mEdges.get(index);
    }


    /**
     *	Prints the contents of this graph to a string.
     *
     *	preconditions:
     *		Uses the .toString() method of the T class.
     */
    @Override
    public String toString() {
        String nodestr = " Nodes[" + mNodes.size() + "]:";

        // Display the nodes. Like as before, we need to get
        // a Set of the keys/IDs and use an Iterator to process
        // them.
        Set<Integer> ids = mNodes.keySet();
        Iterator<Integer> iterator = ids.iterator();

        while (iterator.hasNext()) {
            int id = iterator.next();
            nodestr = nodestr + " (" + id + ": " + mNodes.get(id) + ")";
        }

        // for (int i = 0; i < mNodes.size(); i++) {
        // 	nodestr = nodestr + " " + mNodes.get(i);
        // }

        String edgestr = " Edges[" + mEdges.size() + "]:";
        for (int i = 0; i < mEdges.size(); i++) {
            Edge edge = mEdges.get(i);
            edgestr = edgestr + " (" + edge.startNodeId + ", " + edge.endNodeId
                    + ": " + edge.weight + ")";
        }

        return nodestr + "\n" + edgestr;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  classes & class-bearing variables
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Defines an edge of the graph. Very simple class meant to
     * only be used within the Graph class.
     */
    public class Edge {
        public int startNodeId;
        public int endNodeId;
        public int weight = 0;
    }


    @NonNull
    @Override
    public Iterator iterator() {

        return (Iterator) new Iterator() {

            private Set<Integer> keySet = mNodes.keySet();
            private Iterator<Integer> keySetIterator = keySet.iterator();

            @Override
            public boolean hasNext() {
                return keySetIterator.hasNext();
            }

            @Override
            public Object next() {
                int key = keySetIterator.next();
                return mNodes.get(key);
            }
        };

    }

}
