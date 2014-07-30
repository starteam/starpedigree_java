package star.pedigree;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.management.RuntimeErrorException;

import star.genetics.genetic.impl.AlleleImpl;
import star.genetics.genetic.impl.ChromosomeImpl;
import star.genetics.genetic.impl.CreatureImpl;
import star.genetics.genetic.impl.DiploidAllelesImpl;
import star.genetics.genetic.impl.GeneImpl;
import star.genetics.genetic.impl.GeneticMakeupImpl;
import star.genetics.genetic.impl.GenomeImpl;
import star.genetics.genetic.impl.MatingEngineImpl_XY;
import star.genetics.genetic.impl.ModelImpl;
import star.genetics.genetic.model.Allele;
import star.genetics.genetic.model.Chromosome;
import star.genetics.genetic.model.Creature;
import star.genetics.genetic.model.Creature.Sex;
import star.genetics.genetic.model.GeneticMakeup;
import star.genetics.genetic.model.Genome;
import star.genetics.genetic.model.Model;
import star.genetics.xls.ParseException;
import star.pedigree.model.CompleteIndividual;
import star.pedigree.model.DiploidAlleles;
import star.pedigree.model.Gene;
import star.pedigree.model.Individual;
import star.pedigree.model.MainModel;
import star.pedigree.model.Marker;
import star.pedigree.model.UIIndividual;
import star.pedigree.model.UIRelationship;
import sun.util.logging.resources.logging;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.corba.se.spi.ior.MakeImmutable;

public class Main {

	public final static String COMPLETE = "complete";
	public final static String PARTIAL_UNIQUE = "partial_unique";
	public final static String DEFAULT = "default";
	public final static String NONE = "none";
	private static final String TEMPLATE = "template";
	private static final String PARTIAL = "partial";

	static Logger logger = Logger.getAnonymousLogger();
	private String config;
	private PedigreeMatingEngine matingEngine;
	private GenomeImpl genome;
	private TreeMap<String, star.genetics.genetic.model.Allele> alleleMap = new TreeMap<>();
	private TreeMap<String, CompleteIndividual> individuals = new TreeMap<>();

	public static DiploidAlleles[] makeup2alleles(GeneticMakeupImpl makeup) {
		ArrayList<DiploidAlleles> alleles = new ArrayList<>();
		for (star.genetics.genetic.model.DiploidAlleles a : makeup.values()) {
			DiploidAlleles al = new DiploidAlleles();
			al.add(a.get(0) != null ? a.get(0).getName() : "");
			al.add(a.get(1) != null ? a.get(1).getName() : "");
			alleles.add(al);
		}
		return alleles.toArray(new DiploidAlleles[0]);
	}

	class PedigreeMatingEngine extends MatingEngineImpl_XY {
		private static final long serialVersionUID = 1L;

		public void makeup(CompleteIndividual c) {
			int counter = 0;
			logger.info("" + c);
			while (!c.done && counter++ < 99) {
				try {
					GeneticMakeupImpl makeup = propose(c);
					Creature.Sex sex = star.genetics.genetic.impl.Sex.getSex(
							makeup, genome, "");
					if (NONE.equalsIgnoreCase(c.individual.genotype)) {
						if (c.sex.equals(sex)) {
							c.makeup = makeup;
							c.individual.genotype = COMPLETE;
							c.individual.alleles = makeup2alleles(makeup);
							c.done = true;
						}
					} else if (PARTIAL.equalsIgnoreCase(c.individual.genotype)) {
						if (c.sex.equals(sex)) {
							if (matches(makeup, c.makeup)) {
								c.makeup = makeup;
								c.individual.genotype = COMPLETE;
								c.individual.alleles = makeup2alleles(makeup);
								c.done = true;
							}
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!c.done) {
				throw new RuntimeException();
			}
			logger.info(c + " " + c.done);
		}

		public GeneticMakeupImpl propose(CompleteIndividual c) {
			GeneticMakeup makeup1 = c.parents[0].makeup;
			GeneticMakeup makeup2 = c.parents[1].makeup;
			Creature.Sex sex1 = c.parents[0].sex;
			Creature.Sex sex2 = c.parents[1].sex;
			GeneticMakeup makeup = super.mate(genome, makeup1, sex1, makeup2,
					sex2);
			return (GeneticMakeupImpl) makeup;
		}

	}

	public Main(String[] args) {
		config = args[0];
	}

	public void run() {
		MainModel model = load();
		buildEnvironment(model);
		buildGenotypes(model);
		updateModel(model, individuals);
		dump();
		printModel(model);
	}

	private void printModel(MainModel model) {
		Gson g = new Gson();
		System.out.println(g.toJson(model));

	}

	private void updateModel(MainModel model,
			TreeMap<String, CompleteIndividual> individuals) {
		for (UIIndividual individual : model.ui.individuals) {
			individual.clearMarkers();
		}
		for (CompleteIndividual i : individuals.values()) {
			if (COMPLETE.equalsIgnoreCase(i.individual.genotype)) {
				UIIndividual individual = model.ui
						.findUIIndividual(i.individual.id);
				GeneticMakeupImpl makeup = i.makeup;
				for (star.genetics.genetic.model.DiploidAlleles d : makeup
						.values()) {
					updateUIIndividualWithMarker(model, individual, d.get(0));
					updateUIIndividualWithMarker(model, individual, d.get(1));
				}
				individual.addGenotypes( makeup, genome );
			} else {
				logger.info("Skipping update for " + i.individual.id);
			}
		}
	}

	private void updateUIIndividualWithMarker(MainModel model,
			UIIndividual individual, Allele allele) {
		if (allele != null) {
			Marker m = model.ui.findMarker(allele.getName());
			if (m != null) {
				logger.info("Updating marker: " + individual.id() + " marker:"
						+ m.id);
				individual.addMarker(m.id);
			}
		}

	}

	private MainModel load() {
		try {
			Gson gson = new Gson();
			FileReader in = new FileReader(config);
			JsonReader reader = new JsonReader(in);
			MainModel m = gson.fromJson(reader, MainModel.class);
			return m;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void dump() {
		for (CompleteIndividual i : individuals.values()) {
			System.err.println(i.individual.id);
			for (Entry<star.genetics.genetic.model.Gene, star.genetics.genetic.model.DiploidAlleles> e : i.makeup
					.entrySet()) {
				System.err.println("\t" + e.getKey().getName() + " "
						+ e.getValue());
			}

		}

	}

	private void buildEnvironment(MainModel model) {
		this.genome = buildGenome(model);
		this.matingEngine = new PedigreeMatingEngine();
		buildIndividuals(model);
		buildRelationships(model);
	}

	private GenomeImpl buildGenome(MainModel model) {
		GenomeImpl genome = new GenomeImpl();
		genome.setSexType("XY");
		genome.setName("Pedigree");
		for (Gene g : model.rules.genes) {
			String geneName = g.id;
			String chromosomeName = g.chromosome;
			float position = g.location;
			String[] alleles = g.alleles;
			Chromosome chromosome = genome.getChromosomeByName(chromosomeName);
			if (chromosome == null) {
				chromosome = new ChromosomeImpl(chromosomeName, genome);
			}
			star.genetics.genetic.model.Gene gene = chromosome
					.getGeneByName(geneName);
			if (gene == null) {
				gene = new GeneImpl(geneName, position / 100f, chromosome);
			} else {
				throw new RuntimeException("Gene name already exists "
						+ geneName);
			}
			for (String allele : alleles) {
				alleleMap.put(allele, new AlleleImpl(allele, gene));
			}
		}
		fixGenome(genome);
		return genome;
	}

	private void fixGenome(GenomeImpl genome) {
		if (true) {
			Chromosome x = genome.getChromosomeByName("X");
			if (x == null) {
				x = new ChromosomeImpl("X", genome);
			}
			if (x.getGenes().size() == 0) {
				GeneImpl xx = new GeneImpl("G_X", 0, x);
				AlleleImpl xxx = new AlleleImpl("A_X", xx);
			}
		}
		if (true) {
			Chromosome y = genome.getChromosomeByName("Y");
			if (y == null) {
				y = new ChromosomeImpl("Y", genome);
			}
			if (y.getGenes().size() == 0) {
				GeneImpl yy = new GeneImpl("G_Y", 0, y);
				AlleleImpl yyy = new AlleleImpl("A_Y", yy);
			}
		}
	}

	private void buildIndividuals(MainModel model) {
		logger.info("buildIndividuals");
		// rules declared individuals
		for (Individual individual : model.rules.individuals) {
			String id = individual.id;
			String genotype = individual.genotype;
			DiploidAlleles[] alleles = individual.alleles;

			CompleteIndividual ret = new CompleteIndividual();
			ret.individual = individual;
			ret.sex = model.getSex(id);
			if (alleles != null) {
				GeneticMakeupImpl makeup = new GeneticMakeupImpl();
				for (DiploidAlleles a : alleles) {
					Allele a0 = alleleMap.get(a.get(0));
					Allele a1 = alleleMap.get(a.get(1));
					DiploidAllelesImpl d = new DiploidAllelesImpl(a0, a1);
					makeup.put(a0.getGene(), d);
				}
				ret.makeup = makeup;
			}
			individuals.put(id, ret);
		}
		// fill with individuals not specified in rules
		for (UIIndividual individual : model.ui.individuals) {
			if (!individuals.containsKey(individual.id())) {
				String id = individual.id();
				Individual generated = new Individual();
				generated.id = id;
				generated.genotype = "none";
				CompleteIndividual ret = new CompleteIndividual();
				ret.individual = generated;
				ret.sex = model.getSex(id);
				individuals.put(id, ret);
			}
		}
	}

	private void buildRelationships(MainModel model) {
		logger.info("buildRelationships");

		for (UIRelationship relationship : model.ui.relationships) {
			ArrayList<CompleteIndividual> parents = new ArrayList<>();
			for (String parent_id : relationship.parents) {
				parents.add(individuals.get(parent_id));
			}
			for (String child_id : relationship.children) {
				CompleteIndividual child = individuals.get(child_id);
				child.parents = parents.toArray(new CompleteIndividual[0]);
			}
		}
	}

	private void buildGenotypes(MainModel model) {
		logger.info("buildGenotypes");
		for (CompleteIndividual c : individuals.values()) {
			if (COMPLETE.equalsIgnoreCase(c.individual.genotype)) {
				GeneticMakeupImpl makeup = c.makeup;
				logger.info("fixMakeup for " + c.individual.id);
				fixMakeup(makeup, c.sex);
			}
			if (PARTIAL_UNIQUE.equalsIgnoreCase(c.individual.genotype)) {
				logger.info("partial_unique for " + c.individual.id);
				generateDiploidAlleles(model,c.makeup, c.individual.options);
				logger.info("fixMakeup for " + c.individual.id);
				fixMakeup(c.makeup, c.sex);
				c.individual.genotype = COMPLETE;
			}
		}

		for (CompleteIndividual c : individuals.values()) {
			processIndividual(c, model);
		}
	}

	private boolean nameAvailable(GeneticMakeupImpl makeup, String name )
	{
		boolean available = true;
		for (star.genetics.genetic.model.Gene g : genome.getGenes()) {
			if( g.getAlleleByName(name) != null )
			{
				available = false;
				break;
			}
		}
		return available;
	}
	private void generateDiploidAlleles(MainModel model, GeneticMakeupImpl makeup, String options) {
		for (star.genetics.genetic.model.Gene g : genome.getGenes()) {
			if (String.valueOf(options).equals(g.getName())) {
				logger.info("generateDiploidAlleles " + g.getId());
				char c = 'A';
				AlleleImpl first = null;
				AlleleImpl second = null;
				while (c < 'Z') {
					String name = Character.toString(c);
					if (nameAvailable(makeup, name)) {
						first = new AlleleImpl(name, g);
						alleleMap.put(first.getName(), first);
						addUIMarker(model,name);
						break;
					}
					c++;
				}
				while (c < 'Z') {
					String name = Character.toString(c);
					if (nameAvailable(makeup, name)) {
						second = new AlleleImpl(name, g);
						alleleMap.put(second.getName(), second);
						addUIMarker(model,name);
						break;
					}
					c++;
				}
				if (first != null && second != null) {
					makeup.put(g, new DiploidAllelesImpl(first, second));
				} else {
					throw new RuntimeException(
							"Can not generate allele with this method");
				}
			}

		}

	}

	private void addUIMarker(MainModel model, String str )
	{
		Marker[] markers = model.ui.markers;
		Marker[] new_markers = new Marker[markers.length+1];
		System.arraycopy(markers, 0, new_markers, 0, markers.length);
		Marker new_marker = new Marker();
		new_marker.id = str;
		new_marker.name = str;
		new_marker.kind = "label";
		new_markers[markers.length] = new_marker;
		model.ui.markers = new_markers;
	}
	private void fixMakeup(GeneticMakeupImpl makeup, Sex sex) {
		if (Sex.FEMALE.equals(sex)) {
			// needs XX
			boolean OK = false;
			for (star.genetics.genetic.model.Gene g : makeup.keySet()) {
				if ("X".equalsIgnoreCase(g.getChromosome().getName())) {
					if (makeup.get(g).getAlleleCount() == 2) {
						OK = true;
						break;
					}
				}
			}
			if (!OK) {
				star.genetics.genetic.model.Gene g = genome
						.getChromosomeByName("X").getGeneByName("G_X");
				Allele a = g.getGeneTypes().get(0);
				makeup.put(g, new DiploidAllelesImpl(a, a));
			}
		} else {
			boolean xOK = false;
			boolean yOK = false;
			for (star.genetics.genetic.model.Gene g : makeup.keySet()) {
				if ("X".equalsIgnoreCase(g.getChromosome().getName())) {
					if (makeup.get(g).getAlleleCount() == 1) {
						xOK = true;
						break;
					}
				}
				if ("Y".equalsIgnoreCase(g.getChromosome().getName())) {
					if (makeup.get(g).getAlleleCount() == 1) {
						yOK = true;
						break;
					}
				}
			}
			if (!xOK) {
				star.genetics.genetic.model.Gene g = genome
						.getChromosomeByName("X").getGeneByName("G_X");
				Allele a = g.getGeneTypes().get(0);
				makeup.put(g, new DiploidAllelesImpl(a, null));
			}
			if (!yOK) {
				star.genetics.genetic.model.Gene g = genome
						.getChromosomeByName("Y").getGeneByName("G_Y");
				Allele a = g.getGeneTypes().get(0);
				makeup.put(g, new DiploidAllelesImpl(a, null));
			}

		}

	}

	private void processIndividual(CompleteIndividual c, MainModel model) {
		logger.info("processIndividual: " + c.done + " " + c);
		if (!c.done) {
			if (COMPLETE.equalsIgnoreCase(c.individual.genotype)) {
				c.done = true;
			} else if (PARTIAL.equalsIgnoreCase(c.individual.genotype)) {
				GeneticMakeupImpl templateMakeup = c.makeup;
				generateGenotype(c, model, true);
			} else if (DEFAULT.equalsIgnoreCase(c.individual.genotype)) {
				CompleteIndividual d = individuals.get(DEFAULT);
				if (d != null) {
					GeneticMakeupImpl m = (GeneticMakeupImpl) d.makeup.clone();
					Sex sex = model.getSex(c.individual.id);
					fixMakeup(m, sex);
					c.makeup = m;
					c.individual.genotype = COMPLETE;
					c.individual.alleles = makeup2alleles(c.makeup);

				} else {
					throw new RuntimeException("Default genotype not available");
				}
			} else if (TEMPLATE.equalsIgnoreCase(c.individual.genotype)) {
				// nothing
			} else {
				generateGenotype(c, model, false);
			}
		}
	}

	private void generateGenotype(CompleteIndividual c, MainModel model,
			boolean hasTemplate) {
		logger.finer("generateGenotype: " + c);
		generateParentGenotypes(c, model);
		generateChildGenotype(c, hasTemplate);
	}

	private void generateParentGenotypes(CompleteIndividual c, MainModel model) {
		logger.info("generateParentGenotypes: " + c);

		if (c.parents != null && c.parents.length == 2) {
			for (CompleteIndividual parent : c.parents) {
				processIndividual(parent, model);
			}
			assert (c.parents[0].done && c.parents[1].done);
		} else {
			throw new RuntimeException("Need parents:" + c);
		}
	}

	private static boolean matches(GeneticMakeupImpl target,
			GeneticMakeupImpl source) {
		boolean ret = true;
		for (Entry<star.genetics.genetic.model.Gene, star.genetics.genetic.model.DiploidAlleles> e : source
				.entrySet()) {
			star.genetics.genetic.model.DiploidAlleles td = target.get(e
					.getKey());
			star.genetics.genetic.model.DiploidAlleles sd = e.getValue();
			ret &= sd.equals(td);
		}
		logger.info("Compare (" + ret + ")\n\t" + source + "\n\t" + target);
		return ret;
	}

	private void generateChildGenotype(CompleteIndividual c, boolean hasTemplate) {
		logger.info("generateChildGenotype: " + c);
		matingEngine.makeup(c);
		c.done = true;
	}

	public static void main(String[] args) {
		Main m = new Main(args);
		m.run();
	}
}
