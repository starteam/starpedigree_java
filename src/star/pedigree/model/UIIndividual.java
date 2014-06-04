package star.pedigree.model;

import java.util.HashMap;

public class UIIndividual extends HashMap<String, Object> {
	public String sex() {
		return String.valueOf(get("sex"));
	}
	
	public String id() {
		return String.valueOf(get("id"));
	}
}
