package star.pedigree.model;

import java.util.ArrayList;
import java.util.HashMap;

import star.genetics.genetic.impl.GenomeImpl;
import star.genetics.genetic.model.Chromosome;
import star.genetics.genetic.model.DiploidAlleles;
import star.genetics.genetic.model.Gene;
import star.genetics.genetic.model.GeneticMakeup;

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
	
	public void addGenotypes( GeneticMakeup makeup, GenomeImpl genome ) 
	{
		HashMap<String, Object> map = new HashMap<>();
		for( Chromosome c : genome )
		{
			ArrayList<String[]> list = new ArrayList<>();
			for( Gene g : c.getGenes() )
			{
				if( makeup.containsKey(g) )
				{
					DiploidAlleles d = makeup.get(g);
					String s1 = d.get(0) != null ? d.get(0).getName() : "";
					String s2 = d.get(1) != null ? d.get(1).getName() : "";
					list.add( new String[] { s1,s2 });					
				}
			}
			if( list.size() > 0 )
			{
				map.put(c.getName(), list);
			}
		}
		put( "genotype" , map );
//		ArrayList<String[]> list = new ArrayList<>();
//		for( java.util.Map.Entry<Gene, DiploidAlleles> m : makeup.entrySet())
//		{
//			DiploidAlleles d = m.getValue();
//			String s1 = d.get(0) != null ? d.get(0).getName() : "";
//			String s2 = d.get(1) != null ? d.get(1).getName() : "";
//			list.add( new String[] { s1,s2 });
//		}
//		put( "genotype" , list );
	}
}
