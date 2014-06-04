package star.pedigree.model;

import star.genetics.genetic.impl.GeneticMakeupImpl;
import star.genetics.genetic.model.Creature;

public class CompleteIndividual {
	public Individual individual;
	public Creature.Sex sex;
	public GeneticMakeupImpl makeup;
	public CompleteIndividual[] parents;
	public boolean done = false;
	
	
	@Override
	public String toString() {
		return individual.id + " " + sex.toString() + " " + individual.genotype + " " + makeup;
	}
}
