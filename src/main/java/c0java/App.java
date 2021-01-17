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

//        String ssss = System.getProperty("user.dir");
//        System.out.println(System.getProperty("user.dir"));
//        InputStream input = new FileInputStream("files/input.c0");
//        String outFile = "files/output.c0";

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

//        var tokens = new ArrayList<Token>();
//        try {
//            while (true) {
//                var token = tokenizer.nextToken();
//                if (token.getTokenType().equals(TokenType.EOF)) {
//                    break;
//                }
//                tokens.add(token);
//            }
//        } catch (Exception e) {
//            // 遇到错误不输出，直接退出
//            System.err.println(e);
//            System.exit(0);
//            return;
//        }
//        for (Token token : tokens) {
//            System.out.println(token.toString());
//        }
        var analyzer = new Analyser(tokenizer);
        analyzer.analyse();
        analyzer.program.exportBinary(outFile);
    }



    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
