package c0java;

import java.io.*;
import java.util.Scanner;

import c0java.analyser.Analyser;
import c0java.error.TokenizeError;
import c0java.tokenizer.StringIter;
import c0java.tokenizer.Tokenizer;

public class Test {
    public static void main(String[] args) {
        try{

            String ssss = System.getProperty("user.dir");
            System.out.println(System.getProperty("user.dir"));
            InputStream input = new FileInputStream("files/input.c0");
            String outFile = "files/output.c0";

            Scanner scanner;
            scanner = new Scanner(input);
            var iter = new StringIter(scanner);
            var tokenizer = tokenize(iter);
            
            var analyzer = new Analyser(tokenizer);
            analyzer.analyse();
            analyzer.program.export(outFile);
        }catch(Exception | TokenizeError e){
            e.printStackTrace();
        }
    }



    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
