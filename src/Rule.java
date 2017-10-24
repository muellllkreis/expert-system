public class Rule {
	
	private String conditions;
	private String consequence;
	private String rule;
	
	public Rule(String conditions, String consequence) {
		this.setConditions(conditions);
		this.setConsequence(consequence);
	}
	
	public String getConditions() {
		return conditions;
	}
	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	
	public String getConsequence() {
		return consequence;
	}
	public void setConsequence(String consequence) {
		this.consequence = consequence;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	@Override
	public String toString() {
		return this.getConditions() + " -> " + this.getConsequence(); 
	}
	
}
