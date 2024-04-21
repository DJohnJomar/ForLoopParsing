/*
 * Parses for
 * <for loop> =:: for(<assignment> ; <condition> ; <increment> | <decrement>) "{" {<arithmetic expression>} "}"
 *               |for(<assignment> ; <condition> ; <increment> | <decrement>) <arithmetic expression>
 * <assignment> =:: <data type> <identifier> = <digit>
 * <condition> =:: <identifier> | digit  < | > | == | >= | <= <identifier> | <digit>
 * <increment> =:: <digit>|<identifier> ++
 * <decrement> =:: <digit> | identifier> --
 * <digit> =:: 0|...|9
 * <identifier> =:: <letter> {<letter>}
 * <letter> =:: "a"|...|"Z"
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ForLoopParser {

    private HashMap<String, String> map = new HashMap<String, String>();
    private ArrayList<String> result = new ArrayList<String>();
    private int index;
    private ArithmeticParser arithmeticParser;

    public ForLoopParser() {
        setupHashMap();
        arithmeticParser = new ArithmeticParser(result);
    }

    /*
     * Parses for
     * <for loop> =:: for(<assignment> ; <condition> ; <increment> | <decrement>) "{" {<arithmetic expression>} "}"
     *               |for(<assignment> ; <condition> ; <increment> | <decrement>) <arithmetic expression>
     */
    public void parseForLoop(String input) throws SyntaxErrorException {
        String temp = "";
        index = 0;
        //Checks for the "for" keyword first
        parseForKeyword(input);

        //Checks tthe open and closed parenthesis
        if (index < input.length() && input.charAt(index) == '(') {
            temp = "(";
            checkForToken(temp);
            index++;

            //Checks for (<assignment> ; <condition> ; <increment> | <decrement>)
            parseAssignment(input);
            parseSemiColon(input);
            parseCondition(input);
            parseSemiColon(input);
            parseIncrementOrDecrement(input);
            if (index < input.length() && input.charAt(index) == ')') {
                temp = ")";
                checkForToken(temp);
                index++;
            } else {
                throw new SyntaxErrorException("Expected ')' at index " + index);
            }
        } else {
            throw new SyntaxErrorException("Expected '(' at index " + index);
        }

        skipForWhiteSpaces(input);

        /*
         * Checks if we have curly braces or not
         * Having curly braces allows for multiple java arithmetic expressions
         * While if there are are no braces, it means only single line of expression is allowed
         */
        if (index < input.length() && input.charAt(index) == '{') {
            temp = "{";
            checkForToken(temp);
            index++;
            skipForWhiteSpaces(input);

            temp = "";
            while (index < input.length() && input.charAt(index) != '}') {
                /*
                 * The function to parse arithmetic expressions, utilizes the Arithmetic Parser Class
                 * Extracts all arithmetic expressions and then parse them one by one.
                 */
                while (index < input.length() && input.charAt(index) != ';') {
                    temp += input.charAt(index);
                    index++;
                }
                if (input.charAt(index) == ';') {
                    temp += input.charAt(index);
                    arithmeticParser.parseArithmetic(temp);
                    temp = "";
                    index++;
                }

            }
            skipForWhiteSpaces(input);
            if (index < input.length() && input.charAt(index) == '}') {
                temp = "}";
                checkForToken(temp);
                index++;
            } else {
                throw new SyntaxErrorException("Expected '}' at index " + index);
            }
        } else {

            temp = "";
                while (index < input.length() && input.charAt(index) != ';') {
                    temp += input.charAt(index);
                    index++;
                }
                if (input.charAt(index) == ';') {
                    temp += input.charAt(index);
                    arithmeticParser.parseArithmetic(temp);
                    temp = "";
                    index++;
                }
        }

    }

    //Checks for the "for" keyword
    public void parseForKeyword(String input) throws SyntaxErrorException {
        skipForWhiteSpaces(input);
        if (index < input.length() && input.substring(index, index + 3).equals("for")) {
            // checkForToken(temp);
            result.add("for : Keyword");
            index += 3;
        } else {
            throw new SyntaxErrorException("Expected 'for' keyword at index " + index);
        }
        skipForWhiteSpaces(input);
    }

    //Checks for <assignment> =:: <data type> <identifier> = <digit>
    public void parseAssignment(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        parseDataType(input); //Parses data type
        parseIdentifier(input); //Parses Identifier
        skipForWhiteSpaces(input);

        // if data type -> identifier -> = order
        if (index < input.length() && input.charAt(index) == '=') {
            temp += input.charAt(index);
            checkForToken(temp);
            index++;
            parseNumber(input);//Parses number
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for: <data type> =:: "int" |... |"double"
     */
    public void parseDataType(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        if (index < input.length() && Character.isLetter(input.charAt(index))) {
            while (index < input.length() && Character.isLetterOrDigit(input.charAt(index)) && input.charAt(index) != '=') {
                temp += input.charAt(index);
                index++;
            }
            checkForToken(temp);
        } else {
            throw new SyntaxErrorException("Expected data type keyword at index " + index);
        }
        skipForWhiteSpaces(input);
    }

    /*
     * Parses for: <identifier> =:: <letter> {<letter>}
     */
    public void parseIdentifier(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        // Gathers all letters to temp as long as current character is a
        // letter/digit/"_"
        if (index < input.length() && Character.isLetter(input.charAt(index))) {
            while (index < input.length() && (Character.isLetterOrDigit(input.charAt(index)) || input.charAt(index) == '_')) {
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
     * Parses for: <number =:: <digit> {<digit>}[.<digit>]
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
     * Parses for the condition
     * <condition> =:: <identifier> | digit  < | > | == | >= | <= <identifier> | <digit>
     */
    public void parseCondition(String input) throws SyntaxErrorException {
        String temp = "";

        /*
         * Sample a > b checking for a
         */
        skipForWhiteSpaces(input);
        if (Character.isLetter(input.charAt(index))) {
            parseIdentifier(input);
        } else if (Character.isDigit(input.charAt(index))) {
            parseNumber(input);
        }

        // checking for >
        skipForWhiteSpaces(input);
        while (index < input.length() && isConditional(input.charAt(index))) {
            temp += input.charAt(index);
            index++;
        }
        checkForToken(temp);

        // checking for b
        if (Character.isLetter(input.charAt(index))) {
            parseIdentifier(input);
        } else if (Character.isDigit(input.charAt(index))) {
            parseNumber(input);
        }

        skipForWhiteSpaces(input);
    }

    //Basically checks increment/decrement operator
    public void parseIncrementOrDecrement(String input) throws SyntaxErrorException {
        String temp = "";
        skipForWhiteSpaces(input);
        if (Character.isLetter(input.charAt(index))) {
            parseIdentifier(input);
        }
        if (input.substring(index, index + 2).equals("++")) {
            temp = "++";
            checkForToken(temp);
            index += 2;
        } else if (input.substring(index, index + 2).equals("--")) {
            temp = "--";
            checkForToken(temp);
            index += 2;
        }

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
        map.put("{", "Open Curly Braces");
        map.put("}", "Close Curly Braces");
        map.put(">", "Greater Than Sign");
        map.put("<", "Less Than Sign");
        map.put(">=", "Greater Than or Equal Sign");
        map.put("<=", "Less Than or Equal Sign");
        map.put("==", "\"Is Equal To\" sign");
        map.put(";", "Semicolon");
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

    public boolean isOperator(char character) {
        boolean isOperator = false;
        if (character == '=' || character == '+' || character == '-' || character == '*' || character == '/' || character == '%') {
            isOperator = true;
        }
        return isOperator;
    }

    public boolean isConditional(char character) {
        boolean isConditional = false;

        if (character == '>' || character == '<' || character == '=') {
            isConditional = true;
        }
        return isConditional;
    }

    public void printTokens() {
        System.out.println("\n----- Lexeme : Token Pairs -----\n");
        for (String str : result) {
            System.out.println(str);
        }
    }

    public void clearResult() {
        result.clear();
    }

}

class SyntaxErrorException extends Exception {
    public SyntaxErrorException(String message) {
        super(message);
    }
}
