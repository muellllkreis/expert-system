
public class Definition {
	private String variable = "";
	private String value = "";
	private String equals = "=";
	private boolean root;
	
	public Definition(String variable, String value) {
		this.setVariable(variable);
		this.setValue(value);
		this.setRoot(true);
	}
	
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getEquals() {
		return equals;
	}
	public void setEquals(String equals) {
		this.equals = equals;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	
	@Override
	public String toString() {
		return this.getVariable() + " " + this.getEquals() + " " + "\"" + this.getValue() + "\"";
	}
	
}
