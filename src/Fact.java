
public class Fact {
	private Definition variable;
	private boolean value;
	
	public Definition getVariable() {
		return variable;
	}
	
	public void setVariable(Definition variable) {
		this.variable = variable;
	}
	
	public boolean isValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public Fact(Definition variable) {
		this.variable = variable;
		this.value = false;
	}
	
	public Fact(Definition variable, Boolean value) {
		this.variable = variable;
		this.value = value;
	}
	
	public Fact() {
		this.variable = null;
		this.value = false;
	}
	
	@Override
	public String toString() {
		return this.getVariable().getVariable();
	}
}
