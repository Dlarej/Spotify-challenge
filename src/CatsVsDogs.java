import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CatsVsDogs {
	// Constants
	private static final String CAT = "C";
	private static final String DOG = "D";
	private static final String VOTER = "V";
	private static final int INF = Integer.MAX_VALUE;
	
	// Long Type Input
	private static int NUM_TEST_CASES;
	private static int num_c;
	private static int num_d;
	private static int num_v;
	
	// String analyzers
	private static StringTokenizer st;
	private static BufferedReader stdin;
	private static PrintWriter out;
	
	// Contains all the voters
	//private static HashMap<String,Voter> voters = new HashMap<String,Voter>();
	private static Voter[] voters;
	// Contains voters who want to keep a cat and have a conflict 
	//private static HashMap<String,Voter> cat_conflicts = new HashMap<String,Voter>();
	private static ArrayList<Voter> cat_conflicts;
	// Contains voters who want to keep a dog and have a conflict
	//private static HashMap<String,Voter> dog_conflicts = new HashMap<String,Voter>();
	// Contains all voters with conflicts. This narrows down the scope of investigation.
	//private static HashMap<String,Voter> conflicts = new HashMap<String,Voter>();
	private static Voter[] conflicts;

	// Critical utilities
	//private static HashMap<String,Integer> distances = new HashMap<String,Integer>();
	private static int[] distances; 
	private static HashSet<Integer> freeVertices = new HashSet<Integer>();
	private static HashSet<LinkedList<Integer>> maximalPaths = new HashSet<LinkedList<Integer>>();
	
	// Output
	private static ArrayList<Long> output = new ArrayList<Long>();

	/*
	 * This function performs a Breadth First Search (BFS). The search returns either true
	 * or false. True, if a free vertex of dog conflicts has been found starting from a free
	 * vertex in cat conflicts. False, if no such vertex can be found. In this case, the 
	 * while loop in the main method will terminate, storing the maximum users satisfied
	 * in the output list.
	 */
	public static boolean BFS() {
		ConcurrentLinkedQueue<Integer> bfsQueue = new ConcurrentLinkedQueue<Integer>();
		// Set to true if there is a free vertex in cat that has a path to free vertex in dog
		boolean b = false;
		distances = new int[num_v];
		for (int i=0; i<distances.length; i++) {
			distances[i] = INF;
		}
		// Reset set of free vertices (in dogs). Later to be used in DFS();
		freeVertices.clear();
		// Potential roots of the BFS. Store in queue if not matched.
		for (Voter v : cat_conflicts) {
			// If vertex is not matched already 
			if (!v.is_matched()) {
				distances[v.get_id()] = 0;
				bfsQueue.add(v.get_id());
			}
		}
		// Begin BFS
		while (!bfsQueue.isEmpty()) {
			Voter v = conflicts[bfsQueue.poll()];
			// If free and a dog lover, then store in a set
			if (!v.is_matched() && v.get_type().equals(DOG)) {
				freeVertices.add(v.get_id());
				b = true;
			}
			// Update distances from starting nodes for all 
			// vertices that follow an alternating "unmatched-matched-
			// unmatched..." path.
			if (distances[v.get_id()] < INF) {
				ArrayList<Integer> conflicting_voters = v.get_conflicts();
				for (Integer voter : conflicting_voters) {
					if (conflicts[v.get_id()].get_type() == DOG) {
						if (!conflicts[v.get_id()].is_matched_with(voter)) {
							continue;
						}
					}
					if (distances[voter] == INF) {
						distances[voter] = distances[v.get_id()] + 1;
						bfsQueue.add(voter);
					}
				}
			}
		}	
		return b;
	}
	/*
	 * Traverses the layered subgraph to guide the search for
	 * target vertices (free dogs). 
	 */
	public static void DFS() {
		// Create "Deep Copy" of conflicts.
		Voter[] tempConflicts = deepCopy(conflicts);
		// Keeps track of parents to re-build path once traversed.
		//HashMap<String,String> parent = new HashMap<String,String>();
		int[] parent = new int[num_v]; 
		// Stack involved in the iterative dfs.
		Stack<Integer> dfsStack;
		// Path taken to get to target vertices in freeVertices (free dog)
		LinkedList<Integer> path;
		// Reset maximal paths
		maximalPaths.clear();
		for (Voter v : cat_conflicts) {
			// If vertex is not matched already
			if (v != null && !v.is_matched()) {
				dfsStack = new Stack<Integer>();
				path = new LinkedList<Integer>();
				dfsStack.push(v.get_id());
				while (!dfsStack.isEmpty()) {
					int voter = dfsStack.pop();
					// If voter is not matched and is in dog, 
					// Remove visited vertices to get to there
					if (freeVertices.contains(voter)) {
						int curr = voter;
						// Add to path
						path.addFirst(curr);
						// Remove any relation to curr
						for (Integer i : tempConflicts[curr].get_conflicts()) {
							tempConflicts[i].remove_conflict(curr);
						}
						// Remove curr itself
						tempConflicts[curr] = null;
						while (parent[curr] != 0) {
							curr = parent[curr];
							// Add to path
							path.addFirst(curr);
							// Remove any relation to curr
							for (Integer s : tempConflicts[curr].get_conflicts()) {
								tempConflicts[s].remove_conflict(curr);
							}
							// Remove curr itself
							tempConflicts[curr] = null;
						}
						// Add to maximal paths
						maximalPaths.add(path);
						// Empty the stack
						dfsStack.clear();
					} else {
						ArrayList<Integer> edgeList = tempConflicts[voter].get_conflicts();
						// For all outgoing edges
						for (Integer edge : edgeList) {
							// Make sure it is a free dog
							if (tempConflicts[voter].get_type() == DOG) {
								if (!tempConflicts[voter].is_matched_with(edge)) {
									continue;
								}
							}
							// Make sure on consecutive layer.
							if (distances[edge] == (distances[voter] + 1)) {
								dfsStack.push(edge);
								parent[edge] = voter;
							}
						}
					}
				}
			}
		}
	}
	/*
	 * Utility function to create deep copy of HashMap.
	 * Why isn't there a native Java method that takes care of that???
	 */
	public static Voter[] deepCopy(Voter[] original) {
		Voter[] copy = new Voter[num_v];
		for (int i=0; i<original.length; i++) {
			// Create new voter object
			Voter old_voter = original[i];
			if (old_voter != null) {
				Voter new_voter = new Voter(old_voter.get_id());
				new_voter.set_keep(old_voter.get_keep());
				new_voter.set_remove(old_voter.get_remove());
				// Manually add conflicts
				for (Integer c : old_voter.get_conflicts()) {
					new_voter.add_conflict(c);
				}
				for (Integer m : old_voter.get_matchings()) {
					new_voter.add_matching(m);
				}
				copy[i] = new_voter;
			}
		}
		return copy;
	}
	/*
	 * The symmetric difference between maximal paths and the existing
	 * paths of the investigation.
	 */
	public static void symmetricDifference(LinkedList<Integer> p) {
		
		Integer prev_voter = p.poll();
		// Placeholder voter
		Voter v;
		while (p.peekFirst() != null) {
			int curr_voter = p.poll();
			// If prev matched with curr, remove the matching
			// Otherwise, set the matching
			if (conflicts[prev_voter].is_matched_with(curr_voter)) {
				v = conflicts[prev_voter];
				v.remove_matching(curr_voter);
				conflicts[v.get_id()] = v;
				
				v = conflicts[curr_voter];
				v.remove_matching(prev_voter);
				conflicts[v.get_id()] = v;
			} else {
				v = conflicts[prev_voter];
				v.add_matching(curr_voter);
				conflicts[v.get_id()] = v;
				
				v = conflicts[curr_voter];
				v.add_matching(prev_voter);
				conflicts[v.get_id()] = v;
			}
			prev_voter = curr_voter;
		}
	}

	/*
	 * Driver method of the program. This problem heavily depends on an algorithm
	 * I had to learn: Hopcroft-Karp Algorithm. This algorithm is used to solve
	 * the minimum vertex cover problem on a bipartite graph. Typically, the minimum
	 * vertex cover problem is NP-Hard. However, Koenig's Theorem states that it can
	 * be done in polynomial time if a bipartite graph is used. In this context, any
	 * conflicting voters will share an edge. A conflicting voter is someone who is 
	 * either "threatened" by another voter's potential to be satisfied (e.g., "C1 D1" and
	 * "D1 C1", "C1 D2" and "D 3" are both examples of conflicting pairs). The Hopcroft-Karp
	 * Algorithm performs a BFS to layer the vertices and to make sure there are 
	 * augmenting paths to target node(s) (free vertice(s) in dogs) from starting node(s) 
	 * (free vertice(s) in cats). These augmenting paths will by nature of the algorithm
	 * disjoint. We then apply symmetric differences of the existing paths to the set of
	 * maximal paths found in the DFS, which increase the num of unsatisfied users by 1.
	 * We continue doing BFS and DFS until there are no more augmenting paths. We then
	 * store the (total num of voter - num users unsatisfied) into a list out of output
	 * which finally printed once all simulations are done running.
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		stdin = new BufferedReader(new InputStreamReader(System.in));
		out = new PrintWriter(new BufferedOutputStream(System.out));
		//sc = new Scanner(System.in);
		
		NUM_TEST_CASES = Integer.parseInt(stdin.readLine());
		long begin = System.currentTimeMillis();
		for (int i=0; i<NUM_TEST_CASES; i++) {
			Long count = (long) 0;
			// Parse through c, d, v
			String c_d_v = stdin.readLine();
			st = new StringTokenizer(c_d_v);
			num_c = Integer.parseInt(st.nextToken());
			num_d = Integer.parseInt(st.nextToken());
			num_v = Integer.parseInt(st.nextToken())+1;
			
			// Re-initialize critical data structures
			voters = new Voter[num_v];
			cat_conflicts = new ArrayList<Voter>();
			conflicts = new Voter[num_v];
			
			// Populate voters with useful information
			for (int j=1; j<num_v; j++) {
				Voter v = new Voter(j);
				st = new StringTokenizer(stdin.readLine());
				// Animal that voter wants to keep
				v.set_keep(st.nextToken());
				// Animal the voter wants to remove
				v.set_remove(st.nextToken());
				// Place voter in HashMap of voters
				voters[j] = v;
			}
			// Find conflicting voters
			for (int v1=1; v1<voters.length; v1++) { //v1
				for (int v2=1; v2<voters.length; v2++) { //v2
					if (voters[v1].is_conflicting(voters[v2])) {
						Voter v = voters[v1];
						v.add_conflict(v2);
						v = voters[v2];
						v.add_conflict(v1);
						// add conflicts to cat_conflicts and dog_conflicts
						if (voters[v1].get_type().equals(CAT)) {
							// add v1 to cat_conflicts and v2 to dog_conflicts
							if (!cat_conflicts.contains(voters[v1])) {
								cat_conflicts.add(voters[v1]);
							}
							conflicts[v1] = voters[v1];
							conflicts[v2] = voters[v2];
						} else {
							if (!cat_conflicts.contains(voters[v2])) {
								cat_conflicts.add(voters[v2]);
							}
							conflicts[v1] = voters[v1];
							conflicts[v2] = voters[v2];
						}
					}
				}
			}
			// Begin investigation of vertices
			while (BFS() == true) {
				DFS();
				ArrayList<LinkedList<Integer>> max_paths = new ArrayList<LinkedList<Integer>>(maximalPaths);
				// Apply symmetric difference of matched edges and maximal paths
				for (LinkedList<Integer> p : max_paths) {
					symmetricDifference(p);
					count++;
				}
			}
			
			// Add to output list the max num of satisfied users possible i.e.,
			// total number of voter minus the number of symmetric differences
			// applied to the paths.
			output.add(num_v-count-1);
		}
		// Output
		for (Long l : output) {
			out.println(l);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + (end-begin));
		stdin.close();
		out.flush();
		out.close();
	}
}