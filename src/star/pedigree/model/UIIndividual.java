package star.pedigree.model;

import java.util.ArrayList;
import java.util.HashMap;

public class UIIndividual extends HashMap<String, Object> {
	public String sex() {
		return String.valueOf(get("sex"));
	}
	
	public String id() {
		return String.valueOf(get("id"));
	}
	
	public void clearMarkers()
	{
		put( "markers", new ArrayList<>());		
	}
	
	@SuppressWarnings("unchecked")
	public void addMarker(String marker )
	{
		if(! containsKey("markers") )
		{
			put( "markers", new ArrayList<>());
		}
		ArrayList<Object> markers = ((ArrayList<Object>)get("markers"));
		if( markers.indexOf(marker) == -1)
		{
			markers.add(marker);
		}
	}
}
