package c0java.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Program {
    public String magic = "72 30 3b 3e";
    public String version = "00 00 00 01";
    public List<Global> globals = new ArrayList();
    public List<Function> functions = new ArrayList();


    public Function getFunction(int pos) {
        for(Function f : functions){
            if(f.name == pos){
                return f;
            }
        }
        return null;
    }

    public void writeBytes(String s, PrintStream d) throws IOException {
        String temp = s.replaceAll(" ", "");
        for(int i = 0; i + 2 <= temp.length(); i+=2){
            d.write(Integer.parseInt(temp.substring(i, i + 2), 16));
        }
    }

    public void write8ByteByCal(int s, PrintStream d) throws IOException {
        String temp = Integer.toHexString(s);

        for(; temp.length() < 16;){
            temp = "0" + temp;
        }
        for(int i = 0; i + 2 <= temp.length(); i+=2){
            d.write(Integer.parseInt(temp.substring(i, i + 2), 16));
        }
    }

    public void write4ByteByCal(int s, PrintStream d) throws IOException {
        String temp = Integer.toHexString(s);

        for(; temp.length() < 8;){
            temp = "0" + temp;
        }
        for(int i = 0; i + 2 <= temp.length(); i+=2){
            d.write(Integer.parseInt(temp.substring(i, i + 2), 16));
        }
    }

    public void write2ByteByCal(int s, PrintStream d) throws IOException {
        String temp = Integer.toHexString(s);

        for(; temp.length() < 4;){
            temp = "0" + temp;
        }
        for(int i = 0; i + 2 <= temp.length(); i+=2){
            d.write(Integer.parseInt(temp.substring(i, i + 2), 16));
        }
    }

    public void writeChar(char c, PrintStream d) throws IOException {
        d.write((int) c);
    }

    public void exportBinary(String s) throws IOException{
        //FileOutputStream fos = new FileOutputStream(s);
        //DataOutputStream dos = new DataOutputStream(fos);

        PrintStream dos = new PrintStream(new FileOutputStream(s));

        int[] num;
        StringBuilder res = new StringBuilder();

        writeBytes(magic, dos);
        writeBytes(version, dos);

        write4ByteByCal(globals.size(), dos);

        //Globals
        for(Global global : globals){
            if(!global.isConst){
                dos.write(0);
            }else{
                dos.write(1);
            }
            write4ByteByCal(global.count, dos);
            if(global.value.length() == 0){
                write8ByteByCal(0, dos);
            }else{
                for(int i = 0; i < global.value.length(); i++){
                    writeChar(global.value.charAt(i), dos);
                }
            }
        }
        //fs.write("Fns\n");
        //Fns
        //fs.write("\n");
        write4ByteByCal(functions.size(), dos);
        for(Function function : functions){
            write4ByteByCal(function.name, dos);
            write4ByteByCal(function.returnSlots, dos);
            write4ByteByCal(function.paramSlots, dos);
            write4ByteByCal(function.locSlots, dos);
            write4ByteByCal(function.body.size(), dos);

            for(Instruction i : function.body){
                if(i.type == InstructionType.NoParam){
                    writeBytes(i.exportOpcode(), dos);
                }else if(i.type == InstructionType.u32Param){
                    writeBytes(i.exportOpcode(), dos);
                    write4ByteByCal(Integer.parseInt(i.param), dos);
                }else if(i.type == InstructionType.u64Param){
                    writeBytes(i.exportOpcode(), dos);
                    if(i.param.length() != 16){
                        write8ByteByCal(Integer.parseInt(i.param), dos);
                    }else{
                        for(int j = 0; j + 2 <= i.param.length(); j+=2){
                            dos.write(Integer.parseInt(i.param.substring(j, j + 2), 16));
                        }
                    }
                }
            }
        }


        dos.close();
        //fos.close();

    }

    public void export(String s) throws IOException {
        File file = new File("files/output1.c0");
        FileWriter fs = new FileWriter(file);
        fs.write(magic + "\n");
        fs.write(version + "\n");

        fs.write(globals.size() + "\n");

        //Globals
        for(Global global : globals){
            fs.write(global.isConst + "\n");
            fs.write(global.count + "\n");
            fs.write(global.value + "\n");
            fs.write("-----------\n");
        }
        fs.write("Fns\n");
        //Fns
        for(Function function : functions){
            fs.write("name " + Integer.toString(function.name) + " ");
            fs.write("return " + Integer.toString(function.returnSlots) + " ");
            fs.write("param " + Integer.toString(function.paramSlots) + " ");
            fs.write("loc " + Integer.toString(function.locSlots) + " ");
            fs.write("body " + Integer.toString(function.body.size()) + " ");
            fs.write("\n");
            for(Instruction i : function.body){
                if(i.type == InstructionType.NoParam){
                    fs.write("\t" + i.exportOpcode() + "\n");
                }else{
                    fs.write("\t" + i.exportOpcode() + " " + i.param + "\n");
                }
            }
        }

        fs.close();
    }

    public static void main(String[] args) throws IOException {
        //new Program().export();
    }
}
