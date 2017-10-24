import java.util.Scanner;

public class ShellMain {
	
	public static Scanner reader = new Scanner(System.in); 
	
	public static void main(String[] args) {
		
		//Initialize Expert
		Expert e = new Expert();
		
		//Start Input Shell
		while(true) {
			System.out.println("Enter a String: ");
			String input = reader.nextLine();
			
			if(input.equals("quit")) {
				reader.close();
				break;
			}
			else if(input.equals("clr")) {
				for(int i = 0; i <= 12; i++) {
					System.out.println("\n");
				}
				continue;
			}
			
			Parser p = new Parser();
			String[] instruction = p.categorize(input);
			e.receiveInstruction(instruction);
		}
	}
}
