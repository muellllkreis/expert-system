
public class Parser {
	
	public String[] categorize(String input) {
		int i = 0;
		while(i < input.length() && input.charAt(i) != ' ') {
			i++;
		}
		String command = input.substring(0, i);
		String verb = command;
		if(command.toLowerCase().equals(Verb.TEACH.toString().toLowerCase())) {
			if(input.charAt(i+1) == '-') {
				//teach new variable
				command = input.substring(i+1);
				String arg = command.substring(0, 2);
//				if(arg.equals("-L")) {
//					System.out.println("YOU CANNOT SET A LEARNED VARIABLE");
//					return null;
//				}
				//sets i after spacing behind -R/-L
				i = 3;
				while(command.charAt(i) != ' ') {
					i++;
				}
				String var = command.substring(3, i);
				//sets start to first word of string after =
				int start = i + 4;
				i = i + 4;
				while(command.charAt(i) !='\"') {
					i++;
				}
				String value = command.substring(start, i);
				String[] result = {verb, arg, var, value};
				//System.out.println("ARG: " + arg +"\nVAR: " + var + "\nVALUE: " + value);
				return result;
			}
			else if(input.contains("=")) {
				//teach about root variable
				command = input.substring(i+1);
				i = 0;
				while(command.charAt(i) != ' ') {
					i++;
				}
				String var = command.substring(0, i);
				String val = (command.charAt(i+3) == 't') ? "true" : "false";
				//System.out.println(var + " = " + val);
				String[] result = {verb, var, val};
				return result;
			}
			else {
				//teach new rule
				command = input.substring(i+1);
				i = 0;
				while(command.charAt(i) != ' ') {
					i++;
				}
				String cond = command.substring(0, i);
				int start = i + 4;
				i = i + 4;
				while(i <= command.length()) {
					i++;
				}
				String cons = command.substring(start, i-1);
				//System.out.println(cond + " -> " + cons);
				String[] result = {verb, cond, cons};
				return result;
			}
		}
		else if(command.toLowerCase().equals(Verb.LEARN.toString().toLowerCase())) {
				String[] result = {verb};
				return result;
				//learn
		}
		else if(command.toLowerCase().equals(Verb.LIST.toString().toLowerCase())) {
				String[] result = {verb};
				return result;
				//list
		}
		else if(command.toLowerCase().equals(Verb.QUERY.toString().toLowerCase())) {
				//query expression
				command = input.substring(i+1);
				i = 0;
				while(i <= command.length()) {
					i++;
				}
				String exp = command;
				//System.out.println(exp);
				String[] result = {verb, exp};
				return result;
		}
		else if(command.toLowerCase().equals(Verb.WHY.toString().toLowerCase())) {
				//query expression
				command = input.substring(i+1);
				i = 0;
				while(i <= command.length()) {
					i++;
				}
				String exp = command;
				//System.out.println(exp);
				String[] result = {verb, exp};
				return result;
		}
		else {
			System.out.println("UNKNOWN COMMAND, TRY AGAIN.");
			return null;
		}
	}
	
	public Parser() {
	}
}
