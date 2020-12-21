package miniplc0java;

import miniplc0java.error.TokenizeError;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.*;

public class TokenizerTest {
    private Tokenizer init(){
        File file = new File("C:\\Users\\zhang\\OneDrive\\桌面\\Workspace\\大三上资料整理\\Compiling\\miniplc0-java\\test.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringIter it = new StringIter(sc);
        Tokenizer tokenizer = new Tokenizer(it);
        return tokenizer;
    }

    @Test
    public void TestlexUInt() throws TokenizeError {
        Tokenizer tokenizer = init();
        Token t=null;
        do{
            t = tokenizer.nextToken();
            System.out.println(t+ ", "+t.getEndPos());
        }while(t.getTokenType() != TokenType.EOF);
    }
}
