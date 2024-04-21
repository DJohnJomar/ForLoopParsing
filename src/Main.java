public class Main {
    public static void main(String[] args) throws SyntaxErrorException {
        
        ForLoopParser flParser = new ForLoopParser();
        flParser.parseForLoop("for(int x = 1; x<10;x++) x=x+1;");
        flParser.printTokens();
    }
}
