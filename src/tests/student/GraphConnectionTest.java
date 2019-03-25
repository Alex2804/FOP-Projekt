package tests.student;

import base.Graph;
import base.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Class to test the correct behavior of {@link Graph#allNodesConnected()}
 * @author Alexander Muth
 */
public class GraphConnectionTest {
	@Test
	public void test_Graph_allNodesConnected_shouldBeTrue() {
		Graph<String> graph = new Graph<>();
		assertTrue(graph.allNodesConnected());
		
		Node<String> node1 = graph.addNode("1");
		assertTrue(graph.allNodesConnected());
		
		Node<String> node2 = graph.addNode("2");
		graph.addEdge(node1, node2);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node3 = graph.addNode("3");
		graph.addEdge(node2, node3);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node4 = graph.addNode("4");
		graph.addEdge(node3, node4);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node5 = graph.addNode("5");
		graph.addEdge(node4, node5);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node6 = graph.addNode("6");
		graph.addEdge(node1, node6);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node7 = graph.addNode("7");
		graph.addEdge(node4, node7);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node8 = graph.addNode("8");
		graph.addEdge(node7, node8);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node9 = graph.addNode("9");
		graph.addEdge(node8, node9);
		assertTrue(graph.allNodesConnected());
		
		Node<String> node10 = graph.addNode("10");
		graph.addEdge(node7, node10);
		assertTrue(graph.allNodesConnected());
		
		graph.addEdge(node10, node5);
		assertTrue(graph.allNodesConnected());
		
		graph.addEdge(node7, node1);
		assertTrue(graph.allNodesConnected());
		
		graph.addEdge(node1, node9);
		assertTrue(graph.allNodesConnected());
		
		graph.addEdge(node3, node6);
		assertTrue(graph.allNodesConnected());
	}
	

	@Test
	public void test_Graph_allNodesConnected_shouldBeFalse() {
		Graph<String> graph = new Graph<>();
		
		Node<String> node1 = graph.addNode("1");
		Node<String> node2 = graph.addNode("2");
		assertFalse(graph.allNodesConnected());
		
		Node<String> node3 = graph.addNode("3");
		graph.addEdge(node1, node2);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node4 = graph.addNode("4");
		graph.addEdge(node3, node4);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node5 = graph.addNode("5");
		graph.addEdge(node4, node5);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node6 = graph.addNode("6");
		graph.addEdge(node1, node6);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node7 = graph.addNode("7");
		graph.addEdge(node4, node7);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node8 = graph.addNode("8");
		graph.addEdge(node7, node8);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node9 = graph.addNode("9");
		graph.addEdge(node8, node9);
		assertFalse(graph.allNodesConnected());
		
		Node<String> node10 = graph.addNode("10");
		graph.addEdge(node7, node10);
		assertFalse(graph.allNodesConnected());
		
		graph.addEdge(node10, node5);
		assertFalse(graph.allNodesConnected());
		
		graph.addEdge(node2, node6);
		assertFalse(graph.allNodesConnected());
	}
}
