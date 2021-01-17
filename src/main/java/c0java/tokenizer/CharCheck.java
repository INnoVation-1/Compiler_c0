package c0java.tokenizer;

public class CharCheck {
    private static char[] signChar = {'+', '-', '*', '/',
            '=', '!', '<', '>',
            '(', ')', '{', '}',
            ',', ':', ';'};
    private static char[] escapeChar = {'\\', '"', '\'', 'n', 'r', 't'};

    public static boolean isSignChar(char c) {
        for (char e : signChar) {
            if (e == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEscapeChar(char c){
        for(char e : escapeChar){
            if(e == c){
                return true;
            }
        }
        return false;
    }
}
