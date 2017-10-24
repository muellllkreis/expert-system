import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Expert {
	
	private static final Set<Character> OPERATORS = new HashSet<>(Arrays.asList('!', '&',  '|'));
	private static final Set<Character> PARENTHESES = new HashSet<>(Arrays.asList('(', ')'));
	
	private List<Definition> definitions = new ArrayList<>();
	private List<Fact> facts = new ArrayList<>();
	private List<Rule> rules = new ArrayList<>();
	
	public List<Definition> getDefinitions() {
		return definitions;
	}
	public void setDefinitions(List<Definition> definitions) {
		this.definitions = definitions;
	}
	public List<Fact> getFacts() {
		return facts;
	}
	public void setFacts(List<Fact> facts) {
		this.facts = facts;
	}
	public List<Rule> getRules() {
		return rules;
	}
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	
	public void receiveInstruction(String[] instruction) {
		if(instruction == null) {
			return;
		}
		
		switch(instruction[0].toLowerCase()) {
			case "teach":
				if(instruction.length == 4) {
					this.createNewVariable(instruction);
				}
				else if(instruction[2].equals("true") || instruction[2].equals("false")) {
					this.modifyVariable(instruction);
				}
				else {
					this.createNewRule(instruction);
				}
				break;
			case "learn":
				this.learn();
				break;
			case "list":
				this.list();
				break;
			case "query":
				this.query(instruction);
				break;
			case "why":
				this.why(instruction);
				break;
		}
		return;
	}
	
	// called upon "TEACH -R S = "Sam likes ice cream"" - style instructions
	public void createNewVariable(String[] instruction) {
		if(this.getDefinitions().stream().anyMatch(x -> x.getVariable().equals(instruction[2]))) {
			System.out.println("A variable with this name already exists! Please choose a different name.");
			return;
		}
		
		Definition def = new Definition(instruction[2], instruction[3]);
		if(instruction[1].equals("-L")) {
			def.setRoot(false);
		}
		Fact fact = new Fact(def);
		this.getFacts().add(fact);
		this.getDefinitions().add(def);
	}
	
	// called upon "TEACH S = true"-style instructions
	public void modifyVariable(String[] instruction) {
		int index = this.findVariableIndex(instruction[1]);
		boolean value = instruction[2].equals("true") ? true : false;
		if(!this.getFacts().get(index).getVariable().isRoot()) {
			System.out.println("You cannot set the value of a learned variable directly!");
			return;
		}
		this.getFacts().get(index).setValue(value);

		// reset all learned values to false
		for(Fact fact : this.getFacts()) {
			if(!(fact.getVariable().isRoot())) {
				fact.setValue(false);
			}
		}
	}
	
	// called upon "TEACH S&V -> EAT" -style instructions
	public void createNewRule(String[] instruction) {
		if(this.containsUnknown(instruction[1]) || this.containsUnknown(instruction[2])) {
			System.out.println("The rules entered contain unknown variables! No new rule created.");
			return;
		}
		Rule rule = new Rule(instruction[1], instruction[2]);
		this.getRules().add(rule);
	}
	
	// called upon "LEARN" instruction
	public void learn() {
		List<Rule> rules = this.getRules();
		boolean hasLearned = true;
		
		while(hasLearned) {
			List<Fact> knownfacts = this.getFacts().stream().filter(x -> x.isValue() == true).collect(Collectors.toList());
			for(int i = 0; i < rules.size(); i++) {
				int index = this.findVariableIndex(rules.get(i).getConsequence());
				if(RuleParser.parseAndEvaluate(this, rules.get(i).getConditions()) == 1) {
					this.getFacts().get(index).setValue(true);
				}
				else {
					if(!(this.getFacts().get(index).getVariable().isRoot() && this.getFacts().get(index).isValue())) {
						this.getFacts().get(index).setValue(false);		
					}
				}
			}
			List<Fact> learnedfacts = this.getFacts().stream().filter(x -> x.isValue() == true).collect(Collectors.toList());
			hasLearned = this.hasLearnedFacts(knownfacts, learnedfacts);
		}
	}
	
	// called upon LIST instruction
	public void list() {
		List<Definition> rootvars = new ArrayList<>();
		List<Definition> learnedvars = new ArrayList<>();
		for(Definition current : this.getDefinitions()) {
			if(current.isRoot()) {
				rootvars.add(current);
			}
			else {
				learnedvars.add(current);
			}
		}
		System.out.println("Root Variables:");
		for(Definition current : rootvars) {
			System.out.println("\t" + current);
		}
		System.out.println();
		System.out.println("Learned Variables:");
		for(Definition current : learnedvars) {
			System.out.println("\t" + current);
		}
		System.out.println();
		System.out.println("Facts:");
		for(Fact current : this.getFacts()) {
			if(current.isValue()) {
				System.out.println("\t" + current);
			}
		}
		System.out.println();
		System.out.println("Rules:");
		for(Rule current : this.getRules()) {
			System.out.println("\t" + current);
		}
	}
	
	// called upon QUERY instruction
	public void query(String[] instruction) {
		int result = RuleParser.parseAndQuery(this, instruction[1]);
		if(result == 1) {
			System.out.println("true");
		}
		else {
			System.out.println("false");
		}
	}
	
	// called upon WHY instruction
	public void why(String[] instruction) {
		int result = RuleParser.parseAndWhy(this, instruction[1]);
		if(result == 1) {
			System.out.println("THUS I KNOW THAT " + RuleParser.parseConditionReadable(this, RuleParser.separateVariables(instruction[1])));
		}
		else {
			System.out.println("THUS I CANNOT PROVE THAT " + RuleParser.parseConditionReadable(this, RuleParser.separateVariables(instruction[1])));	
		}
	}
	
	public int findVariableIndex(String varname) {
		for(int i = 0; i < this.getFacts().size(); i++) {
			if(this.getFacts().get(i).getVariable().getVariable().equals(varname)) {
				return i;
			}
		}
		return -1;
	}
	
	public int findDefintionIndex(String defname) {
		for(int i = 0; i < this.getDefinitions().size(); i++) {
			if(this.getDefinitions().get(i).getVariable().equals(defname)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean hasLearnedFacts(List<Fact> knownfacts, List<Fact> learnedfacts) {
		return !(knownfacts.equals(learnedfacts));
	}
	
	public boolean containsUnknown(String expression) {
		List<String> parsed = RuleParser.separateVariables(expression);
		for(String s : parsed) {
			if(!OPERATORS.contains(s.charAt(0)) && !PARENTHESES.contains(s.charAt(0))) {
				if(!this.getDefinitions().stream().anyMatch(x -> x.getVariable().equals(s))) {
					return true;
				}
			}
		}
		return false;
	}
	
}
