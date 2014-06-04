package star.pedigree.model;

import star.genetics.genetic.model.Creature;

public class MainModel {
	public UI ui;
	public Rules rules;
	
	public Creature.Sex getSex( String id )
	{
		for(UIIndividual i : ui.individuals)
		{
			if( id.equalsIgnoreCase(i.id()))
			{
				String sex = i.sex();
				for( Sex s : ui.sexes )
				{
					if( sex.equalsIgnoreCase(s.id))
					{
						return Creature.Sex.parse(s.kind);
					}
				}
			}
		}
		return null;
	}
}
