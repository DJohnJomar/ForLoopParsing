import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws SyntaxErrorException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ForLoopParser flParser = new ForLoopParser();

        System.out.println("Due to the limitations for user inputs in java console, you can only input the for loop statement in a single line.");
        System.out.println("Example:\n\tfor(int x = 1; x<10;x++){ x=x++; y = y+5;}\n\tfor(int x = 1; x<10;x++) x=x++;\n");

        String input;
        while(true){
            try{
                System.out.print("\nEnter your Java For-Loop Expression: ");
                input = reader.readLine();
                flParser.parseForLoop(input);
                flParser.printTokens();
                flParser.clearResult();
            }catch(Exception e){
                System.out.println("Invalid Input!\n"+e);
            }
        }
    }
}
