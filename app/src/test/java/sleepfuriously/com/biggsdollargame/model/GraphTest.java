package sleepfuriously.com.biggsdollargame.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * Tests the {@link Graph} class.
 *
 * todo: add tests for directed graphs as well
 */
public class GraphTest {

    @Test
    public void getUniqueNodeId() {

        // create a bunch of ids
        Graph<Integer> graph = new Graph<>();

        int[] ids = addSomeNodes(graph, 25);

        // make sure all the ids are different (uses side-effect of Set--that all members need to be unique)
        boolean unique = true;
        Set<Integer> set = new HashSet<>();
        for (int id : ids) {
            if (set.add(id) == false) {
                unique = false;
                break;
            }
        }

        Assert.assertTrue("Found ids that were not unique!", unique);
    }


    @Test
    public void addNode() {
        Graph<Integer> graph = new Graph<>();

        // since addNode(data) calls addNode(id, data), I'm just testing the former
        try {
            graph.addNode(1);
            graph.addNode(2);
            graph.addNode(3);
            graph.addNode(4);
            graph.addNode(5);
            graph.addNode(6);
            graph.addNode(7);
        }
        catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(graph.numNodes(), 7);
    }

    @Test
    public void addEdge() {

        // setup a graph with a few nodes
        Graph<Integer> graph = new Graph<>();
        int[] ids = new int[7];

        try {
            for (int i = 0; i < ids.length; i++) {
                ids[i] = graph.addNode(i + 30);
            }
        }
        catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
        }

        // add some edges
        Assert.assertEquals(1, graph.addEdge(ids[0], ids[1]));
        Assert.assertEquals(2, graph.addEdge(ids[0], ids[2]));
        Assert.assertEquals(3, graph.addEdge(ids[0], ids[3]));
        Assert.assertEquals(4, graph.addEdge(ids[0], ids[4]));
        Assert.assertEquals(5, graph.addEdge(ids[0], ids[5]));
        Assert.assertEquals(6, graph.addEdge(ids[0], ids[6]));
        Assert.assertEquals(7, graph.addEdge(ids[5], ids[6]));
        Assert.assertEquals(8, graph.addEdge(ids[4], ids[6]));
        Assert.assertEquals(9, graph.addEdge(ids[3], ids[6]));
        Assert.assertEquals(10, graph.addEdge( ids[2], ids[6]));
        Assert.assertEquals(11, graph.addEdge( ids[1], ids[6]));
        Assert.assertEquals(12, graph.addEdge( ids[1], ids[3]));
        Assert.assertEquals(13, graph.addEdge( ids[1], ids[2]));

        // try edges that are repeats
        Assert.assertEquals(-1, graph.addEdge( ids[1], ids[2]));
        Assert.assertEquals(-1, graph.addEdge( ids[2], ids[1]));
        Assert.assertEquals(-1, graph.addEdge( ids[5], ids[6]));
        Assert.assertEquals(-1, graph.addEdge( ids[6], ids[5]));

    }

    @Test
    public void getEdgeIndex() {
        Graph<Integer> graph = new Graph<>();

        int[] ids = addSomeNodes(graph, 20);

        // add some edges
        graph.addEdge(0, 4);
        graph.addEdge(4, 8);
        graph.addEdge(8, 12);
        graph.addEdge(12, 0);

        Assert.assertNotEquals(-1, graph.getEdgeIndex(4, 0));
        Assert.assertNotEquals(-1, graph.getEdgeIndex(4, 8));
        Assert.assertNotEquals(-1, graph.getEdgeIndex(12, 0));

        Assert.assertEquals(-1, graph.getEdgeIndex(0, 3));
        Assert.assertEquals(-1, graph.getEdgeIndex(0, 8));
        Assert.assertEquals(-1, graph.getEdgeIndex(20, 3));
    }


    @Test
    public void clone1() {
        Graph<Integer> graph1 = new Graph<>();

        addSomeNodes(graph1, 10);
        addSomeEdges(graph1);

        Graph<Integer> graph2 = graph1.clone();
        Assert.assertNotNull(graph2);

        // test the nodes
        List ids1 = graph1.getAllNodeIds();
        List ids2 = graph2.getAllNodeIds();

        Assert.assertTrue(ids1.size() == ids2.size());
        for (int i = 0; i < ids1.size(); i++) {
            int val1 = graph1.getNodeData((int) ids1.get(i));
            int val2 = graph2.getNodeData((int) ids2.get(i));
            Assert.assertTrue(val1 == val2);
        }
    }

    @Test
    public void getAllAdjacentToDirected() {
        // todo
    }

    @Test
    public void getAllAdjacentToNonDirected() {
        Graph<Integer> graph = new Graph<>();
        int[] nodeIds = addSomeNodes(graph, 11);

        // add some edges
        graph.addEdge(nodeIds[0], nodeIds[1]);  // 1 connected to 0, 2, 3, 6, 7
        graph.addEdge(nodeIds[2], nodeIds[1]);
        graph.addEdge(nodeIds[3], nodeIds[1]);
        graph.addEdge(nodeIds[1], nodeIds[6]);
        graph.addEdge(nodeIds[1], nodeIds[7]);

        // add extra edges
        graph.addEdge(nodeIds[0], nodeIds[4]);  // 0 extends to 4 and 2 (2 creates a loop with 1)
        graph.addEdge(nodeIds[0], nodeIds[2]);
        graph.addEdge(nodeIds[9], nodeIds[7]);  // 7 extends to 9

        graph.addEdge(nodeIds[5], nodeIds[8]);  // 5 & 8 connect, but are separate from the rest

        // test adjacents to 1
        Assert.assertTrue(graph.isAdjacent(nodeIds[1], nodeIds[0]));
        Assert.assertTrue(graph.isAdjacent(nodeIds[7], nodeIds[1]));
        Assert.assertTrue(graph.isAdjacent(nodeIds[1], nodeIds[6]));
        Assert.assertTrue(graph.isAdjacent(nodeIds[1], nodeIds[3]));
        Assert.assertTrue(graph.isAdjacent(nodeIds[1], nodeIds[2]));

        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[1]));   // can't be adjacent to yourself!
        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[4]));
        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[5]));
        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[8]));
        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[9]));
        Assert.assertFalse(graph.isAdjacent(nodeIds[1], nodeIds[10]));

        List<Integer> adjToOne = graph.getAllAdjacentTo(nodeIds[1]);
        Assert.assertEquals(5, adjToOne.size());    // make sure the size is correct

        Assert.assertTrue(adjToOne.contains(nodeIds[0]));   // check for correct items
        Assert.assertTrue(adjToOne.contains(nodeIds[7]));
        Assert.assertTrue(adjToOne.contains(nodeIds[6]));
        Assert.assertTrue(adjToOne.contains(nodeIds[3]));
        Assert.assertTrue(adjToOne.contains(nodeIds[2]));

        Assert.assertFalse(adjToOne.contains(nodeIds[1]));
        Assert.assertFalse(adjToOne.contains(nodeIds[4]));
        Assert.assertFalse(adjToOne.contains(nodeIds[5]));
        Assert.assertFalse(adjToOne.contains(nodeIds[8]));
        Assert.assertFalse(adjToOne.contains(nodeIds[9]));
        Assert.assertFalse(adjToOne.contains(nodeIds[10]));

        // test adjacents to 0
        List<Integer> adjToZero = graph.getAllAdjacentTo(nodeIds[0]);
        Assert.assertEquals(3, adjToZero.size());

        // test adjacents to 5
        List<Integer> adjToFive = graph.getAllAdjacentTo(nodeIds[5]);
        Assert.assertEquals(1, adjToFive.size());

        // test adjacents to 10 (which should be none)
        List<Integer> adjToTen = graph.getAllAdjacentTo(nodeIds[10]);
        Assert.assertEquals(0, adjToTen.size());


    }

    @Test
    public void getAllNodeIds() {
        Graph<Integer> graph = new Graph<>();

        int id1, id2, id3, id4;

        try {
            id1 = graph.addNode(9);
            id2 = graph.addNode(10);
            id3 = graph.addNode(11);
            id4 = graph.addNode(12);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            Assert.fail("Unable to add nodes in getAllNodeIds()");
            return;
        }

        List<Integer> allIds = graph.getAllNodeIds();

        Assert.assertEquals(4, allIds.size());
        Assert.assertTrue(allIds.contains(id1));
    }

    @Test
    public void getAllNodeData() {
        Graph<Integer> graph = new Graph<>();

        try {
            graph.addNode(1, 9);
            graph.addNode(2, 10);
            graph.addNode(3, 11);
            graph.addNode(4, 12);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            Assert.fail("Unable to add nodes in getAllNodeData()");
            return;
        }

        List<Integer> data = graph.getAllNodeData();
        Assert.assertEquals(4, data.size());

        Assert.assertEquals(9, (int)graph.getNodeData(1));
        Assert.assertEquals(10, (int)graph.getNodeData(2));
        Assert.assertEquals(11, (int)graph.getNodeData(3));
        Assert.assertEquals(12, (int)graph.getNodeData(4));

        Assert.assertNull(graph.getNodeData(5));    // should be nothing with this id
    }

    @Test
    public void getNodeData() {
        Graph<Integer> graph = new Graph<>();

        try {
            graph.addNode(1, 9);
            graph.addNode(2, 10);
            graph.addNode(3, 11);
            graph.addNode(4, 12);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            Assert.fail("Unable to add nodes in getNodeId()");
            return;
        }

        int id = graph.getNodeData(1);
        Assert.assertEquals(9, id);

        id = graph.getNodeData(2);
        Assert.assertEquals(10, id);

        id = graph.getNodeData(3);
        Assert.assertEquals(11, id);

        id = graph.getNodeData(4);
        Assert.assertEquals(12, id);
    }

    @Test
    public void getNodeId() {
        Graph<Integer> graph = new Graph<>();

        try {
            graph.addNode(1, 9);
            graph.addNode(2, 10);
            graph.addNode(3, 11);
            graph.addNode(4, 12);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            Assert.fail("Unable to add nodes in getNodeId()");
            return;
        }

        int id = graph.getNodeId(9);
        Assert.assertEquals(1, id);

        id = graph.getNodeId(10);
        Assert.assertEquals(2, id);

        id = graph.getNodeId(11);
        Assert.assertEquals(3, id);

        id = graph.getNodeId(12);
        Assert.assertEquals(4, id);
    }


    @Test
    public void isAdjacent() {
        Graph<Integer> graph = new Graph<>();
        int[] ids = addSomeNodes(graph, 9);

        // make some edges
        graph.addEdge(ids[0], ids[1]);  // 1 is adjacent: 0, 2, 4, 5
        graph.addEdge(ids[2], ids[1]);
        graph.addEdge(ids[4], ids[1]);
        graph.addEdge(ids[5], ids[1]);

        graph.addEdge(ids[5], ids[4]);  // make some loops
        graph.addEdge(ids[0], ids[2]);

        Assert.assertTrue(graph.isAdjacent(1, 0));  // check for positives
        Assert.assertTrue(graph.isAdjacent(1, 2));
        Assert.assertTrue(graph.isAdjacent(1, 4));
        Assert.assertTrue(graph.isAdjacent(5, 1));
        Assert.assertTrue(graph.isAdjacent(5, 4));
        Assert.assertTrue(graph.isAdjacent(2, 0));

        Assert.assertFalse(graph.isAdjacent(1, 1)); // negatives
        Assert.assertFalse(graph.isAdjacent(1, 3));
        Assert.assertFalse(graph.isAdjacent(1, 6));
        Assert.assertFalse(graph.isAdjacent(1, 8));
        Assert.assertFalse(graph.isAdjacent(7, 1));

        Assert.assertFalse(graph.isAdjacent(5, 0));
        Assert.assertFalse(graph.isAdjacent(0, 5));
        Assert.assertFalse(graph.isAdjacent(0, 3));
        Assert.assertFalse(graph.isAdjacent(0, 4));
    }

    @Test
    public void getGenus() {

        Graph<Integer> graph = new Graph<>();
        int[] ids = addSomeNodes(graph, 4);

        // Connect the graph--all to node 0
        graph.addEdge(ids[0], ids[1]);
        graph.addEdge(ids[0], ids[2]);
        graph.addEdge(ids[0], ids[3]);

        // This try is for all the remaining tests, which should NOT cause an exception!
        try {
            Assert.assertEquals(0, graph.getGenus());

            // add loops
            graph.addEdge(ids[1], ids[2]);
            Assert.assertEquals(1, graph.getGenus());

            graph.addEdge(ids[2], ids[3]);
            Assert.assertEquals(2, graph.getGenus());

            graph.addEdge(ids[3], ids[1]);
            Assert.assertEquals(3, graph.getGenus());

            graph.removeEdge(ids[1], ids[0]);
            Assert.assertEquals(2, graph.getGenus());

        } catch (GraphNotConnectedException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception in getGenus()");
        }
    }


    @Test
    public void isConnected() {
        Graph<Integer> graph = new Graph<>();
        int[] ids = addSomeNodes(graph, 4);

        Assert.assertFalse(graph.isConnected());

        graph.addEdge(ids[0], ids[1]);  // connect in a line
        graph.addEdge(ids[2], ids[1]);
        graph.addEdge(ids[3], ids[2]);
        Assert.assertTrue(graph.isConnected());

        // add a loop
        graph.addEdge(ids[3], ids[0]);
        Assert.assertTrue(graph.isConnected());

        // Remove an edge
        graph.removeEdge(ids[1], ids[2]);
        Assert.assertTrue(graph.isConnected());

        // Remove another edge
        graph.removeEdge(ids[1], ids[0]);
        Assert.assertFalse(graph.isConnected());
    }

    @Test
    public void getEdges() {

        Graph<Integer> graph = new Graph<>();
        int[] ids = addSomeNodes(graph, 4);

        // add some edges (a simple loop)
        graph.addEdge(ids[0], ids[1]);
        graph.addEdge(ids[1], ids[2]);
        graph.addEdge(ids[2], ids[3]);
        graph.addEdge(ids[0], ids[3]);

        List<Graph<Integer>.Edge> edges = graph.getEdges(ids[0]);
        Assert.assertEquals(2, edges.size());

        Graph.Edge edge1 = edges.get(0);
        Assert.assertTrue(edge1.startNodeId == ids[0]);
        Assert.assertTrue(edge1.endNodeId == ids[1]);

        graph.removeNode(ids[3]);
        edges = graph.getEdges(ids[0]);
        Assert.assertEquals(1, edges.size());
    }

    @Test
    public void numNodes() {
        Graph<Integer> graph = new Graph<>();
        int[] nodes = addSomeNodes(graph, 14);
        Assert.assertEquals(14, graph.numNodes());

        try {
            graph.addNode(323);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals(15, graph.numNodes());

        graph.removeNode(nodes[2]);
        graph.removeNode(nodes[0]);

        Assert.assertEquals(13, graph.numNodes());
    }

    @Test
    public void numEdges() {
        Graph<Integer> graph = new Graph<>();
        int[] nodes = addSomeNodes(graph, 12);

        // Connect all the nodes sequentially, leaving 11
        for (int i = 1; i < nodes.length;  i++) {
            graph.addEdge(nodes[i - 1], nodes[i]);
        }
        Assert.assertEquals(11, graph.numEdges());

        graph.addEdge(nodes[2], nodes[4]);
        graph.addEdge(nodes[4], nodes[6]);
        Assert.assertEquals(13, graph.numEdges());

        graph.removeEdge(nodes[0], nodes[1]);
        Assert.assertEquals(12, graph.numEdges());
    }

    @Test
    public void removeNode() {
        Graph<Integer> graph = new Graph<>();
        int[] nodeIds = addSomeNodes(graph, 5);

        Assert.assertEquals(5, graph.numNodes());

        graph.removeNode(nodeIds[4]);   // remove last node
        Assert.assertEquals(4, graph.numNodes());

        graph.removeNode(nodeIds[1]);   // remove 2nd node
        Assert.assertEquals(3, graph.numNodes());

        graph.removeNode(nodeIds[0]);   // remove first node
        Assert.assertEquals(2, graph.numNodes());
    }

    @Test
    public void removeAllNodes() {
        Graph<Integer> graph = new Graph<>();
        addSomeNodes(graph, 5);

        graph.removeAllNodes();
        Assert.assertEquals(0, graph.numNodes());
    }

    @Test
    public void removeEdgesWithNode() {
        Graph<Integer> graph = new Graph<>();
        int[] nodeIds = addSomeNodes(graph, 5);

        // make all the nodes connect to 3
        graph.addEdge(nodeIds[3], nodeIds[0]);
        graph.addEdge(nodeIds[3], nodeIds[1]);
        graph.addEdge(nodeIds[3], nodeIds[2]);
        graph.addEdge(nodeIds[3], nodeIds[4]);

        graph.removeEdgesWithNode(nodeIds[3]);
        Assert.assertEquals(0, graph.numEdges());

        // make a circle of edges (five edges)
        graph.addEdge(nodeIds[0], nodeIds[1]);
        graph.addEdge(nodeIds[1], nodeIds[2]);
        graph.addEdge(nodeIds[2], nodeIds[3]);
        graph.addEdge(nodeIds[3], nodeIds[4]);
        graph.addEdge(nodeIds[4], nodeIds[0]);

        graph.removeEdgesWithNode(nodeIds[1]);  // Remove all associated with node 1, leaving 3
        Assert.assertEquals(3, graph.numEdges());
    }

    @Test
    public void removeAllEdges() {
        Graph<Integer> graph = new Graph<>();
        addSomeNodes(graph, 5);
        addSomeEdges(graph);
        Assert.assertNotEquals(0, graph.numEdges());

        graph.removeAllEdges();
        Assert.assertEquals(0, graph.numEdges());
    }

    @Test
    public void removeEdge() {
        Graph<Integer> graph = new Graph<>();
        int[] nodeIds = addSomeNodes(graph, 5);

        // circle of edges (5)
        graph.addEdge(nodeIds[0], nodeIds[1]);
        graph.addEdge(nodeIds[1], nodeIds[2]);
        graph.addEdge(nodeIds[2], nodeIds[3]);
        graph.addEdge(nodeIds[3], nodeIds[4]);
        graph.addEdge(nodeIds[4], nodeIds[0]);

        graph.removeEdge(nodeIds[2], nodeIds[1]);
        Assert.assertEquals(4, graph.numEdges());

        graph.removeEdge(nodeIds[0], nodeIds[1]);
        Assert.assertEquals(3, graph.numEdges());
    }

    @Test
    public void getAllEdges() {
        Graph<Integer> graph = new Graph<>();
        int[] nodeIds = addSomeNodes(graph, 5);

        // circle of edges (5)
        graph.addEdge(nodeIds[0], nodeIds[1]);
        graph.addEdge(nodeIds[1], nodeIds[2]);
        graph.addEdge(nodeIds[2], nodeIds[3]);
        graph.addEdge(nodeIds[3], nodeIds[4]);
        graph.addEdge(nodeIds[4], nodeIds[0]);

        List edges = graph.getAllEdges();
        Assert.assertEquals(5, edges.size());

        graph.addEdge(nodeIds[0], nodeIds[4]);
        Assert.assertEquals(5, edges.size());

        graph.addEdge(nodeIds[1], nodeIds[4]);
        Assert.assertEquals(6, edges.size());

        graph.removeEdge(nodeIds[2], nodeIds[1]);
        graph.removeEdge(nodeIds[2], nodeIds[3]);
        Assert.assertEquals(4, edges.size());
    }


    //---------------------------------
    //  helpers
    //---------------------------------

    /**
     * adds a node to the supplied graph
     *
     * @return id of the added node
     */
    private int testAddNode(Graph<Integer> graph, int val) {
        int id = graph.getUniqueNodeId();
        try {
            graph.addNode(id, val);
        } catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Ads some nodes to the given graph.
     *
     * @param graph     The graph in question. Will be modified to hold
     *                  the new nodes, all will have dummy Integers as their value.
     *
     * @param howMany   Number of nodes to add.
     *
     * @return  An array of ints that hold the node ids.
     */
    private int[] addSomeNodes(Graph<Integer> graph, int howMany) {

        int[] ids = new int[howMany];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = testAddNode(graph, 7 + i);
        }

        return ids;
    }

    /**
     * Tries to remove the number of nodes requested.  If the graph is
     * not big enough, then all the nodes are removed one-by-one.
     * The nodes will be removed in random order.
     *
     * @param graph     The graph in question.
     *
     * @param howMany   The number of nodes to attempt to remove.
     */
    private void removeSomeNodes(Graph<Integer> graph, int howMany) {

        Random rand = new Random();

        for (int i = 0; i < howMany; i++) {
            List ids = graph.getAllNodeIds();
            int randIndex = rand.nextInt(ids.size());

            int id = (int) ids.get(randIndex);
            graph.removeNode(id);
        }
    }

    /**
     * Adds some edges to the graph.  If it's big enough, the graph
     * will have at least one loop.
     *
     * @param graph     The graph in question.
     */
    private void addSomeEdges(Graph graph) {

        // todo: test with weights

        List ids = graph.getAllNodeIds();

        for (int i = 1; i < ids.size(); i++) {
            graph.addEdge(0, i);
            graph.addEdge(i - 1, i);    // doing both insures loops. dupes are ignored
        }
    }

}