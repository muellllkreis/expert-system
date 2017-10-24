import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class RuleParser {
	
	private static final Set<Character> OPERATORS = new HashSet<>(Arrays.asList('!', '&',  '|'));
	private static final Set<Character> PARENTHESES = new HashSet<>(Arrays.asList('(', ')'));
	
	public static List<String> originalex = new ArrayList<>();
	public static Set<String> reasoning = new LinkedHashSet<>();

	public static int parseAndQuery(Expert e, String expression) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		String truthexpression = "";
		
		List<String> parsed = new ArrayList<>();

		parsed = RuleParser.separateVariables(expression);
		originalex = RuleParser.separateVariables(expression);

		List<String> evaluated = RuleParser.chainBackwards(e, parsed);
		
		for(String s : evaluated) {
			truthexpression += s;
		}
		
		//System.out.println(truthexpression);
		int result;
		try {
			if(truthexpression.equals("true") || truthexpression.equals("!false")) {
				result = 1; 
			}
			else if(truthexpression.equals("false") || truthexpression.equals("!true")) {
				result = 0;
			}
			else {
				if(engine.eval(truthexpression) instanceof Boolean) {
					result = (Boolean) engine.eval(truthexpression) ? 1 : 0;
				}
				else {
					result = (Integer) engine.eval(truthexpression);
				}	
			}
			reasoning.clear();
			originalex.clear();
			return result;
		} catch (ScriptException se) {
			se.printStackTrace();
			return -1;
		}	
		
	}
	
	public static int parseAndWhy(Expert e, String expression) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		String truthexpression = "";
		List<String> parsed = new ArrayList<>();
		
		parsed = RuleParser.separateVariables(expression);
		originalex = RuleParser.separateVariables(expression);


		List<String> evaluated = RuleParser.chainBackwardsAndExplain(e, parsed, true);
		
		for(String s : evaluated) {
			truthexpression += s;
		}
		
		//System.out.println(truthexpression);
		int result;
		try {
			if(truthexpression.equals("true") || truthexpression.equals("!false")) {
				result = 1; 
			}
			else if(truthexpression.equals("false") || truthexpression.equals("!true")) {
				result = 0;
			}
			else {
				if(engine.eval(truthexpression) instanceof Boolean) {
					result = (Boolean) engine.eval(truthexpression) ? 1 : 0;
				}
				else {
					result = (Integer) engine.eval(truthexpression);
				}	
			}
			if(result == 1) {
				System.out.println("true");
			}
			else {
				System.out.println("false");
			}
			for(String s : reasoning) {
				System.out.println(s);
			}
			reasoning.clear();
			originalex.clear();
			return result;
		} catch (ScriptException se) {
			se.printStackTrace();
			return -1;
		}
		
	}
	
	public static List<String> chainBackwards(Expert e, List<String> parsed) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		List<Rule> knownrules = e.getRules();
		List<Fact> truefacts = e.getFacts().stream().filter(x -> x.isValue() == true).collect(Collectors.toList());
		
		for(String variable : parsed) {
			if(!OPERATORS.contains(variable.charAt(0)) && !PARENTHESES.contains(variable.charAt(0))) {
				//knownrules.stream().anyMatch(x -> x.getConsequence().equals(variable) && RuleParser.parseAndEvaluate(e, x.getConditions()) == 1);
				//is variable a known fact?
				String var = variable;
				if(truefacts.stream().anyMatch(x -> x.getVariable().getVariable().equals(var))) {
					parsed.set(parsed.indexOf(variable), "true");
					continue;
				}
				//variable is not a known fact
				else {
					//get all rules that have variable as consequence
					List<Rule> varcon = knownrules.stream().filter(x -> x.getConsequence().equals(var)).collect(Collectors.toList());
					//there is no rule that has variable as a consequence = it is false
					if(varcon.size() == 0) {
						parsed.set(parsed.indexOf(variable), "false");
						continue;
					}
					//is any of the rules true (i.e. condition is known to be true)?
					if(varcon.stream().anyMatch(x -> RuleParser.parseAndEvaluate(e, x.getConditions()) == 1)) {
						parsed.set(parsed.indexOf(variable), "true");
						continue;
					}
					//it is not obvious that any of the rules are true
					else {
						List<String> conditions = varcon.stream().map(x -> x.getConditions()).collect(Collectors.toList());
						List<String> result = new ArrayList<String>();
						String expression = "";
						for(String s : conditions) {
							// recursively check if conditions of found rules are true
							while(true) {
								result = RuleParser.chainBackwards(e, RuleParser.separateVariables(s));
								if(result.size() == 1 && result.get(0).equals("true")) {
									parsed.set(parsed.indexOf(variable), "true");
									break;
								}
								else if (result.size() == 1 && result.get(0).equals("false")){
									parsed.set(parsed.indexOf(variable), "false");
									break;
								}
								else {
									for(String sresult : result) {
										expression += sresult;
									}
									try {
										List<String> temp = RuleParser.separateVariables(expression);
										if(temp.size() == 2) {
											boolean reverse = Boolean.valueOf(temp.get(1));
											reverse = reverse ? false : true;
											parsed.set(parsed.indexOf(variable), Boolean.toString(reverse));
											break;
										}
										if(engine.eval(expression) instanceof Boolean) {
											if((Boolean) engine.eval(expression)) {
												parsed.set(parsed.indexOf(variable), "true");
												break;
											}
											else {
												parsed.set(parsed.indexOf(variable), "false");
												break;	
											}
										}
										if(engine.eval(expression) instanceof Integer) {
											if((Integer) engine.eval(expression) == 1) {
												parsed.set(parsed.indexOf(variable), "true");
												break;
											}
											else {
												parsed.set(parsed.indexOf(variable), "false");
												break;
											}
										}
									} catch (ScriptException e1) {
										e1.printStackTrace();
									}
									break;
								}
							}
						}
					}
				}
			}
		}
		return parsed;
	}
	
	public static List<String> chainBackwardsAndExplain(Expert e, List<String> parsed, Boolean why) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		List<Rule> knownrules = e.getRules();
		List<Fact> truefacts = e.getFacts().stream().filter(x -> x.isValue() == true).collect(Collectors.toList());
		for(String variable : parsed) {
			if(!OPERATORS.contains(variable.charAt(0)) && !PARENTHESES.contains(variable.charAt(0))) {
				//is variable a known fact?
				String var = variable;
				if(truefacts.stream().anyMatch(x -> x.getVariable().getVariable().equals(var))) {
					parsed.set(parsed.indexOf(variable), "true");
					if(why) {
						reasoning.add("I KNOW THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
					}
					continue;
				}
				//variable is not a known fact
				else {
					//get all rules that have variable as consequence
					List<Rule> varcon = knownrules.stream().filter(x -> x.getConsequence().equals(var)).collect(Collectors.toList());
					//there is no rule that has variable as a consequence = it is false
					if(varcon.size() == 0) {
						parsed.set(parsed.indexOf(variable), "false");
						if(why) {
							reasoning.add("I KNOW IT IS NOT TRUE THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
						}
						continue;
					}
					//is any of the rules true (i.e. condition is known to be true)?
					if(varcon.stream().anyMatch(x -> RuleParser.parseAndEvaluate(e, x.getConditions()) == 1)) {
						parsed.set(parsed.indexOf(variable), "true");
						if(why) {
							String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(varcon.stream().findAny().get().getConditions()));
							String output2 = e.getDefinitions().get(e.findDefintionIndex(variable)).getValue();
							for(String s : RuleParser.separateVariables(varcon.stream().findAny().get().getConditions())) {
								if(!OPERATORS.contains(s.charAt(0)) && !PARENTHESES.contains(s.charAt(0))) {
									reasoning.add("I KNOW THAT " + e.getDefinitions().get(e.findDefintionIndex(s)).getValue());
								}
							}
							reasoning.add("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
						}
						continue;
					}
					//it is not obvious that any of the rules are true
					else {
						for(Rule r : varcon) {
							String conditions = r.getConditions();
							List<String> result = new ArrayList<String>();
							String expression = "";
							
							result = RuleParser.chainBackwardsAndExplain(e, RuleParser.separateVariables(conditions), true);
							if(result.size() == 1 && result.get(0).equals("true")) {
								parsed.set(parsed.indexOf(variable), "true");
								String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
								String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
								reasoning.add("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
								break;
							}
							else if (result.size() == 1 && result.get(0).equals("false")){
								parsed.set(parsed.indexOf(variable), "false");
								String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
								String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
								reasoning.add("BECAUSE IT IS NOT TRUE THAT " + output + " I CANNOT PROVE " + output2);
								break;
							}
							else {
								for(String sresult : result) {
									expression += sresult;
								}
								try {
									List<String> temp = RuleParser.separateVariables(expression);
									if(temp.size() == 2) {
										boolean reverse = Boolean.valueOf(temp.get(1));
										reverse = reverse ? false : true;
										parsed.set(parsed.indexOf(variable), Boolean.toString(reverse));
										break;
									}
									if(engine.eval(expression) instanceof Boolean) {
										if((Boolean) engine.eval(expression)) {
											parsed.set(parsed.indexOf(variable), "true");
											String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
											String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
											reasoning.add("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
											break;
										}
										else {
											parsed.set(parsed.indexOf(variable), "false");
											String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
											String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
											reasoning.add("BECAUSE IT IS NOT TRUE THAT " + output + " I CANNOT PROVE " + output2);
											break;	
										}
									}
									if(engine.eval(expression) instanceof Integer) {
										if((Integer) engine.eval(expression) == 1) {
											parsed.set(parsed.indexOf(variable), "true");
											String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
											String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
											reasoning.add("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
											break;
										}
										else {
											parsed.set(parsed.indexOf(variable), "false");
											String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(r.getConditions()));
											String output2 = e.getDefinitions().get(e.findDefintionIndex(r.getConsequence())).getValue();
											reasoning.add("BECAUSE IT IS NOT TRUE THAT " + output + " I CANNOT PROVE " + output2);
											break;
										}
									}
								} catch (ScriptException e1) {
									e1.printStackTrace();
								}
								break;
							}
						}
					}
				}
			}
		}
		return parsed;
	}
	
	public static int parseAndEvaluate(Expert e, String condition) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		
		List<String> parsed = new ArrayList<>();
		List<String> translated = new ArrayList<>();
		
		parsed = RuleParser.separateVariables(condition);
		translated = RuleParser.translateToTruth(e, parsed);
		// (re-)builds string of truthvalues and operators, parentheses
		String expression = "";
		//translated.forEach(System.out::print);
		for(int i = 0; i < translated.size(); i++) {
			expression += translated.get(i);
		}
		//System.out.println();

		// calls JavaScript engine to evaluate expression
		int result;

		try {
			if(expression.equals("true") || expression.equals("!false")) {
				result = 1; 
			}
			else if(expression.equals("false") || expression.equals("!true")) {
				result = 0;
			}
			else {
				if(engine.eval(expression) instanceof Boolean) {
					result = (Boolean) engine.eval(expression) ? 1 : 0;
				}
				else {
					result = (Integer) engine.eval(expression);	
				}
			}
			return result;
		} catch (ScriptException se) {
			se.printStackTrace();
			return -1;
		}	
	}
	
	// makes parentheses, operators and variable names seperate elements of an arraylist (parsed)
	public static List<String> separateVariables(String condition) {
		String curr = "";
		List<String> parsed = new ArrayList<>();
		for(int i = 0; i < condition.length(); i++) {
			if(OPERATORS.contains(condition.charAt(i)) || PARENTHESES.contains(condition.charAt(i))) {
				if(curr != "") {
					parsed.add(curr);
					curr = "";
				}
				parsed.add(Character.toString(condition.charAt(i)));
			}
			else {
				curr += condition.charAt(i);
			}
		}
		if(curr != "") {
			parsed.add(curr);
			curr = "";
		}
		return parsed;
	}
	
	public static String parseConditionReadable(Expert e, List<String> condition) {
		String readable = "";
		for(String s: condition) {
			if(PARENTHESES.contains(s.charAt(0))) {
				readable += s;
				continue;
			}
			else if(OPERATORS.contains(s.charAt(0))) {
				switch(s.charAt(0)) {
				case '&':
					readable += " AND ";
					break;
				case '|':
					readable += " OR ";
					break;
				case '!':
					readable += " NOT ";
					break;
				}
				continue;
			}
			else {
				readable += e.getDefinitions().get(e.findDefintionIndex(s)).getValue();
			}
		}
		return readable;
	}
	
	public static void printConclusion(Expert e, List<String> expression, boolean conclusion) {
		if(conclusion) {
			System.out.println("THUS I KNOW THAT " + RuleParser.parseConditionReadable(e, expression));
		}
		else {
			System.out.println("THUS I CANNOT PROVE THAT " + RuleParser.parseConditionReadable(e, expression));
		}
	}
	
	// replaces varnames with their value (= true or false)
	public static List<String> translateToTruth(Expert e, List<String> parsed) {
		List<String> translated = new ArrayList<>();
		for(String elem : parsed) {
			if((!OPERATORS.contains(elem.charAt(0))) && (!PARENTHESES.contains(elem.charAt(0)))) {
				int index = e.findVariableIndex(elem);
				elem = Boolean.toString(e.getFacts().get(index).isValue());
				translated.add(elem);
			}
			else {
				translated.add(elem);
			}
		}
		return translated;
	}
}

//NOT WORKING DRAFT OF WHY COMMAND IMPLEMENTATION
//
//
//public static List<String> chainBackwardsAndExplain(Expert e, List<String> parsed, Boolean why) {
//ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//List<Rule> knownrules = e.getRules();
//List<Fact> truefacts = e.getFacts().stream().filter(x -> x.isValue() == true).collect(Collectors.toList());
//for(String variable : parsed) {
//	if(!OPERATORS.contains(variable.charAt(0)) && !PARENTHESES.contains(variable.charAt(0))) {
//		//knownrules.stream().anyMatch(x -> x.getConsequence().equals(variable) && RuleParser.parseAndEvaluate(e, x.getConditions()) == 1);
//		//is variable a known fact?
//		String var = variable;
//		if(truefacts.stream().anyMatch(x -> x.getVariable().getVariable().equals(var))) {
//			parsed.set(parsed.indexOf(variable), "true");
//			if(why) {
//				//System.out.println("I KNOW THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
//				reasoning.add("I KNOW THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
//			}
//			continue;
//		}
//		//variable is not a known fact
//		else {
//			//get all rules that have variable as consequence
//			List<Rule> varcon = knownrules.stream().filter(x -> x.getConsequence().equals(var)).collect(Collectors.toList());
//			//there is no rule that has variable as a consequence = it is false
//			if(varcon.size() == 0) {
//				parsed.set(parsed.indexOf(variable), "false");
//				if(why) {
//					//System.out.println("I KNOW IT IS NOT TRUE THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
//					reasoning.add("I KNOW IT IS NOT TRUE THAT " + e.getDefinitions().get(e.findDefintionIndex(variable)).getValue());
//				}
//				continue;
//			}
//			//is any of the rules true (i.e. condition is known to be true)?
//			if(varcon.stream().anyMatch(x -> RuleParser.parseAndEvaluate(e, x.getConditions()) == 1)) {
//				parsed.set(parsed.indexOf(variable), "true");
//				if(why) {
//					String output = RuleParser.parseConditionReadable(e, RuleParser.separateVariables(varcon.stream().findAny().get().getConditions()));
//					String output2 = e.getDefinitions().get(e.findDefintionIndex(variable)).getValue();
//					//System.out.println("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
//					for(String s : RuleParser.separateVariables(varcon.stream().findAny().get().getConditions())) {
//						if(!OPERATORS.contains(s.charAt(0)) && !PARENTHESES.contains(s.charAt(0))) {
//							reasoning.add("I KNOW THAT " + e.getDefinitions().get(e.findDefintionIndex(s)).getValue());
//						}
//					}
//					reasoning.add("BECAUSE I KNOW " + output + " I KNOW THAT " + output2);
//				}
//				continue;
//			}
//			//it is not obvious that any of the rules are true
//			else {
//				List<String> conditions = varcon.stream().map(x -> x.getConditions()).collect(Collectors.toList());
//				List<String> result = new ArrayList<String>();
//				String expression = "";
//				for(String s : conditions) {
//					// recursively check if conditions of found rules are true
//					while(true) {
//						result = RuleParser.chainBackwardsAndExplain(e, RuleParser.separateVariables(s), true);
//						if(result.size() == 1 && result.get(0).equals("true")) {
//							parsed.set(parsed.indexOf(variable), "true");
//							break;
//						}
//						else if (result.size() == 1 && result.get(0).equals("false")){
//							parsed.set(parsed.indexOf(variable), "false");
//							break;
//						}
//						else {
//							for(String sresult : result) {
//								expression += sresult;
//							}
//							try {
//								List<String> temp = RuleParser.separateVariables(expression);
//								if(temp.size() == 2) {
//									boolean reverse = Boolean.valueOf(temp.get(1));
//									reverse = reverse ? false : true;
//									parsed.set(parsed.indexOf(variable), Boolean.toString(reverse));
//									break;
//								}
//								if(engine.eval(expression) instanceof Boolean) {
//									if((Boolean) engine.eval(expression)) {
//										parsed.set(parsed.indexOf(variable), "true");
//										break;
//									}
//									else {
//										parsed.set(parsed.indexOf(variable), "false");
//										break;	
//									}
//								}
//								if(engine.eval(expression) instanceof Integer) {
//									if((Integer) engine.eval(expression) == 1) {
//										parsed.set(parsed.indexOf(variable), "true");
//										break;
//									}
//									else {
//										parsed.set(parsed.indexOf(variable), "false");
//										break;
//									}
//								}
//							} catch (ScriptException e1) {
//								e1.printStackTrace();
//							}
//							break;
//						}
//					}
//				}
//			}
//		}
//	}
//}
//return parsed;
//}