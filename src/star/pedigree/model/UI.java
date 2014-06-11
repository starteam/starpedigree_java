package star.pedigree.model;

import java.util.HashMap;

public class UI {
	public UIIndividual[] individuals;
	public Sex[] sexes;
	public UIRelationship[] relationships;
	public Marker[] markers;
	public HashMap<String,Object> options;
	public UIIndividual findUIIndividual( String id )
	{
		for( UIIndividual i : individuals )
		{
			if( id.equalsIgnoreCase(i.id()))
			{
				return i;
			}
		}
		return null;
	}
	
	public Marker findMarker( String id ) {
		for( Marker m : markers )
		{
			if( id.equals(m.id))
			{
				return m;
			}
		}
		return null;
	}

}
