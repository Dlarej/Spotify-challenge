import java.util.ArrayList;


public class Voter {
	private static final String CAT = "C";
	private static final String DOG = "D";
	// id of Voter
	private int id;
	// type of Voter (CAT or DOG)
	private String type;
	// Pet voter wants to keep
	private String keep;
	// Pet voter wants to remove
	private String remove;
	// List of conflicting voters (edges)
	private ArrayList<Integer> conflicts;
	// List of matchings
	private ArrayList<Integer> matchings;
	
	public Voter(int id) {
		this.id = id;
		this.conflicts = new ArrayList<Integer>();
		this.matchings = new ArrayList<Integer>();
	}
	
	public int get_id() {
		return this.id;
	}
	
	public String get_type() {
		return this.type;
	}
	
	public String get_keep() {
		return this.keep;
	}
	
	public String get_remove() {
		return this.remove;
	}
	
	public ArrayList<Integer> get_conflicts() {
		return this.conflicts;
	}
	
	public ArrayList<Integer> get_matchings() {
		return this.matchings;
	}
	
	public void set_keep(String keep) {
		this.keep = keep;
		// Set type
		if (keep.charAt(0) == CAT.charAt(0)) {
			this.type = CAT;
		} else {
			this.type = DOG;
		}
	}
	
	public void set_remove(String remove) {
		this.remove = remove;
	}
	
	public void set_conflicts(ArrayList<Integer> conflicts) {
		this.conflicts = conflicts;
	}
	
	public void add_conflict(int conflict) {
		if (!this.conflicts.contains(conflict)) {
			this.conflicts.add(conflict);
		}
	}
	
	public void add_matching(int matching) {
		if (!this.matchings.contains(matching)) {
			this.matchings.add(matching);
		}
	}
	
	public void remove_conflict(int conflict) {
		if (this.conflicts.contains(conflict)) {
			this.conflicts.remove(new Integer(conflict));
		}
	}
	
	public void remove_matching(int matching) {
		if (this.matchings.contains(matching)) {
			this.matchings.remove(new Integer(matching));
		}
	}
	
	public boolean is_conflicting(Voter v) {
		boolean b = false;
		if (this.remove.equals(v.keep)) {
			b = true;
		}
		return b;
	}
	
	public void printConflicts() {
		System.out.println(this.toString());
		System.out.println("Conflicts: ");
		for (Integer i : conflicts) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
	
	public boolean is_matched() {
		return (!matchings.isEmpty());
	}
	
	public boolean is_matched_with(int v) {
		boolean b = false;
		if (!this.matchings.isEmpty()) {
			for (Integer m : this.matchings) {
				if (m.equals(v)) {
					b = true;
				}
			}
		}
		return b;
	}
	
	@Override
	public String toString() {
		return (this.id + ": " + this.keep + " " + this.remove);
	}

}