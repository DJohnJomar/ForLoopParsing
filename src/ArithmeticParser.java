/*
 * The program follows the following BNF:
 * <arithmetic expression> =:: <data type> <identifier> = <expression>;
 *                             |<data type> <identifier> += <expression>; // The compounds operators
 *                             | <identifier> += <expression>; // The compounds operators
 *                             | <identifier> = <expression>;
 * <expression> =:: <term> {+ | - | % <term>}
 * <term> =:: <factor> {* | / factor}
 * <factor> =:: (<expression>) | <increment> | <decrement> | <number> | <identifier>
 * <increment> =:: <number> ++;
 * <increment> =:: <identifier> ++;
 * <decrement> =:: <number> --; 
 * <decrement> =:: <identifier> --;
 * <number =:: <digit> {<digit>}[.<digit>]
 * <identifier> =:: <letter> {<letter>}
 * <data type> =:: "int" |... |double
 * <digit> =:: "0"| ... | "9"
 * <letter> =:: "a" | ... | "Z"
 */
    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.regex.Pattern;

    
public class ArithmeticParser {
    private HashMap<String, String> map = new HashMap<String, String>();
    private ArrayList<String> result = new ArrayList<String>();
    private int index;
    private boolean hasDataType;

    public ArithmeticParser(ArrayList result){
        setupHashMap();
        this.result = result;
    }
    
     /*
     * Parses for:
     * <arithmetic expression> =:: <data type> <identifier> = <expression>;
     * |<data type> <identifier> += <expression>; // The compounds operators
     * | <identifier> += <expression>; // The compounds operators
     * | <identifier> = <expression>;
     */
    public int parseArithmetic(String input) throws SyntaxErrorException {
        String temp = "";
        index = 0;
        parseDataType(input);
        parseIdentifier(input);
        skipForWhiteSpaces(input);

        // if data type -> identifier -> = order
        if (index < input.length() && input.charAt(index) == '=') {
            temp += input.charAt(index);
            checkForToken(temp);
            index++;
            parseExpression(input);
            parseSemiColon(input);
        }
        /*
         * if data type -> identifier -> += order
         * isOperator() checks if current character is an operator, most compound
         * characters start with an operator
         * followd by the equal (=) sign
         */
        else if (index < input.length() && isOperator(input.charAt(index))) {
            temp += input.charAt(index);
            index++;

            temp += input.charAt(index);
            checkForToken(temp);
            index++;

            parseExpression(input);
            parseSemiColon(input);
        }
        // If nothing matches, then it is an error
        else {
            throw new SyntaxErrorException("Expected '=' at index " + index);
        }
        return index;
    }

    /*
     * Parses for:
     * <expression> =:: <term> {+ | - | % <term>}
     */
    public void parseExpression(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        parseTerm(input);

        // Parses other terms
        while (index < input.length() && (input.charAt(index) == '+' || input.charAt(index) == '-' || input.charAt(index) == '%')) {
            temp += input.charAt(index);
            System.out.println("Value in temp must be Addition: "+temp);
            checkForToken(temp);
            index++;
            temp = "";
            parseTerm(input);
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for:
     * <term> =:: <factor> {+ | / factor}
     */
    public void parseTerm(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        parseFactor(input);

        // Parses for other factors
        while (index < input.length() && (input.charAt(index) == '*' || input.charAt(index) == '/')) {
            temp += input.charAt(index);
            checkForToken(temp);
            index++;
            temp = "";
            parseFactor(input);
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for:
     * <factor> =:: (<expression>) | <increment> | <decrement> | <number> |
     * <identifier>
     * 
     */
    public void parseFactor(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);

        // checks for (<expression>)
        if (index < input.length() && input.charAt(index) == '(') {
            // Parse expression within parentheses
            temp += input.charAt(index);
            checkForToken(temp);
            index++;
            parseExpression(input);
            skipForWhiteSpaces(input);
            if (index < input.length() && input.charAt(index) == ')') {
                // Check for closing parenthesis
                temp = ")";
                checkForToken(temp);
                index++;
                skipForWhiteSpaces(input);
            } else {
                throw new SyntaxErrorException("Expected ')' at index " + index);
            }
        }

        // checks for <number> | <increment> | <decrement> | <identifier>
        else if (Character.isDigit(input.charAt(index))) {
            parseNumber(input);

            //check for increment/decrement
            if (index + 1 < input.length() && input.charAt(index) == '+' && input.charAt(index + 1) == '+') {
                temp += "" + input.charAt(index) + input.charAt(index + 1);
                index += 2;
                checkForToken(temp);
            } else if (index + 1 < input.length() && input.charAt(index) == '-' && input.charAt(index + 1) == '-') {
                temp += "" + input.charAt(index) + input.charAt(index + 1);
                index += 2;
                checkForToken(temp);
            }
        }else {
            // Parses identifier
            parseIdentifier(input);
            //check for increment/decrement
            if (index + 1 < input.length() && input.charAt(index) == '+' && input.charAt(index + 1) == '+') {
                temp += "" + input.charAt(index) + input.charAt(index + 1);
                index += 2;
                checkForToken(temp);
            } else if (index + 1 < input.length() && input.charAt(index) == '-' && input.charAt(index + 1) == '-') {
                temp += "" + input.charAt(index) + input.charAt(index + 1);
                index += 2;
                checkForToken(temp);
            }
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for:
     * <identifier> =:: <letter> {<letter>}
     */
    public void parseIdentifier(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);

        // Gathers all letters to temp as long as current character is a
        // letter/digit/"_"
        if (index < input.length() && Character.isLetter(input.charAt(index))) {
            while (index < input.length()
                    && (Character.isLetterOrDigit(input.charAt(index)) || input.charAt(index) == '_')) {
                temp += input.charAt(index);
                index++;
            }
            result.add(temp + " : Identifier");// Similar function to checkForToken();
        } else {
            throw new SyntaxErrorException("Expected identifier at index " + index);
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for:
     * <number =:: <digit> {<digit>}[.<digit>]
     */
    public void parseNumber(String input) {
        String temp = "";
        skipForWhiteSpaces(input);

        while (index < input.length() && Character.isDigit(input.charAt(index)) || input.charAt(index) == '.') {
            temp += input.charAt(index);
            index++;
        }
        result.add(temp + " : " + identifyNumericType(temp));// Similar function to checkForToken()
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for:
     * <data type> =:: "int" |... |"double"
     */
    public void parseDataType(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);

        if (index < input.length() && Character.isLetter(input.charAt(index))) {
            while (index < input.length() && Character.isLetterOrDigit(input.charAt(index)) && input.charAt(index) != '=') {
                temp += input.charAt(index);
                index++;
            }
            if (!temp.isEmpty()) {
                hasDataType = checkForToken(temp);// method returns a boolean value if there is a match
                if (!hasDataType) {// If there is no data type, reset index to start
                    index = 0;
                }
            }
        } else {
            throw new SyntaxErrorException("Expected data type keyword at index " + index);
        }
        skipForWhiteSpaces(input);
    }

    // Checks for the semicolon
    public void parseSemiColon(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);

        if (index < input.length() && input.charAt(index) == ';') {
            temp += input.charAt(index);
            index++;
            checkForToken(temp);
        } else {
            throw new SyntaxErrorException("Expected semicolon at index " + index);
        }
        skipForWhiteSpaces(input);
    }

    public void skipForWhiteSpaces(String input) {
        while (index < input.length() && input.charAt(index) == ' ') {
            index++;
        }
    }

    public String identifyNumericType(String str) {
        // Regular expressions to match different numeric types
        String byteRegex = "-?\\d+[bB]";
        String shortRegex = "-?\\d+[sS]";
        String intRegex = "-?\\d+";
        String longRegex = "-?\\d+[lL]";
        String floatRegex = "-?\\d+\\.\\d+[fF]?";
        String doubleRegex = "-?\\d+\\.\\d+([dD]|\\.)?";

        // Checking if the input str matches one of the patterns
        if (Pattern.matches(byteRegex, str)) {
            return "Byte Literal";
        } else if (Pattern.matches(shortRegex, str)) {
            return "Short Literal";
        } else if (Pattern.matches(intRegex, str)) {
            return "Integer Literal";
        } else if (Pattern.matches(longRegex, str)) {
            return "Long Literal";
        } else if (Pattern.matches(floatRegex, str)) {
            return "Float Literal";
        } else if (Pattern.matches(doubleRegex, str)) {
            return "Double Literal";
        } else {
            return "Not a numeric type";
        }
    }

    // Checks the input string if it matches one of the keys in the hashmap of
    // lexemes:tokens pairs
    public boolean checkForToken(String string) {
        boolean tokenMatch = false;
        for (String key : map.keySet()) {
            if (string.equals(key)) {
                result.add(string + " : " + map.get(key));
                tokenMatch = true;
                break;
            }
        }
        return tokenMatch;
    }

    public boolean isOperator(char character) {
        boolean isOperator = false;
        if (character == '=' || character == '+' || character == '-' || character == '*' || character == '/'
                || character == '%') {
            isOperator = true;
        }
        return isOperator;
    }

    // Simply filling up the hashmap with values beforehand
    public void setupHashMap() {

        map.put("byte", "Keyword");
        map.put("short", "Keyword");
        map.put("int", "Keyword");
        map.put("long", "Keyword");
        map.put("float", "Keyword");
        map.put("double", "Keyword");
        map.put("=", "Equal Sign");
        map.put("+", "Plus Sign");
        map.put("-", "Minus Sign");
        map.put("*", "Multiplication Sign");
        map.put("/", "Division Sign");
        map.put("%", "Modulo Sign");
        map.put("++", "Increment sign");
        map.put("--", "Decrement Sign");
        map.put("+=", "Compound Addition");
        map.put("-=", "Compound Subtractions");
        map.put("*=", "Compound Multiplication");
        map.put("/=", "Compound Division");
        map.put("%=", "Compound Modulo");
        map.put("(", "Open Parenthesis");
        map.put(")", "Close Parenthesis");
        map.put(";", "Semicolon");
    }

    public void printTokens(){
        System.out.println("\n----- Lexeme : Token Pairs -----\n");
                for (String str : result) {
                    System.out.println(str);
                }
    }
}


    
class SyntaxErrorException extends Exception {
    public SyntaxErrorException(String message) {
        super(message);
    }
}