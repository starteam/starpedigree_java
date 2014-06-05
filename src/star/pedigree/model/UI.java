package star.pedigree.model;

import java.util.HashMap;

public class UI {
	public UIIndividual[] individuals;
	public Sex[] sexes;
	public UIRelationship[] relationships;
	public Marker[] markers;
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
			if( id.equalsIgnoreCase(m.id))
			{
				return m;
			}
		}
		return null;
	}

}
