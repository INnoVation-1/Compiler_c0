package miniplc0java;

import miniplc0java.error.CompileError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.analyser.Analyser;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class AnalyserTest {
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
    public void TestAnalyser() throws CompileError {
        Tokenizer tokenizer = init();
        Analyser analyzer = new Analyser(tokenizer);
        List<Instruction> res = analyzer.analyse();
        for (Instruction ee:res) {
            System.out.println(ee.toString());
        }
    }
}
