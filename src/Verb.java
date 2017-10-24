
public enum Verb {
	TEACH, LEARN, LIST, QUERY, WHY;


public static String word(Verb v) {
	String str ="";
	switch (v) {
		case TEACH: str = "teach";
			break;
		case LEARN: str = "learn";
			break;
		case LIST: str = "list";
			break;
		case QUERY: str = "query";
			break;
		case WHY: str = "why";
			break;
	}
	return str;
}
}
