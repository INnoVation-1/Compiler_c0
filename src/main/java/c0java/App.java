package c0java;

import java.io.*;
import java.util.Scanner;

import c0java.analyser.Analyser;
import c0java.error.CompileError;
import c0java.error.TokenizeError;
import c0java.tokenizer.StringIter;
import c0java.tokenizer.Tokenizer;

public class App {
    public static void main(String[] args) throws CompileError, TokenizeError, IOException {
        InputStream input = null;
        String outFile = null;
        String infile = null;
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-l")){
                input = new FileInputStream(args[i + 1]);
                infile = args[i + 1];
            }else if(args[i].equals("-o")){
                outFile = args[i + 1];
            }
        }
        System.out.println(infile + "---" + outFile);

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        OutputStream outPutStream;
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(infile)));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(result.toString());

        var analyzer = new Analyser(tokenizer);
        analyzer.analyse();
        analyzer.program.exportBinary(outFile);
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
