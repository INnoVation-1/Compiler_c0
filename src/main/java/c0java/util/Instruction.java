package c0java.util;

public class Instruction {
    public InstructionType type;
    public InstructionKind opcode;
    public String param;

    public Instruction(InstructionType type, InstructionKind opcode) {
        this.type = type;
        this.opcode = opcode;
    }

    public Instruction(InstructionType type, InstructionKind opcode, String param) {
        this.type = type;
        this.opcode = opcode;
        this.param = param;
    }

    public String exportOpcode() {
        switch (opcode) {
            case nop:
                return "00";
            case push:
                return "01";
            case pop:
                return "02";
            case popn:
                return "03";
            case dup:
                return "04";
            case loca:
                return "0a";
            case arga:
                return "0b";
            case globa:
                return "0c";
            case load8:
                return "10";
            case load16:
                return "11";
            case load32:
                return "12";
            case load64:
                return "13";
            case store8:
                return "14";
            case store16:
                return "15";
            case store32:
                return "16";
            case store64:
                return "17";
            case alloc:
                return "18";
            case free:
                return "19";
            case stackalloc:
                return "1a";
            case addi:
                return "20";
            case subi:
                return "21";
            case muli:
                return "22";
            case divi:
                return "23";
            case addf:
                return "24";
            case subf:
                return "25";
            case mulf:
                return "26";
            case divf:
                return "27";
            case divu:
                return "28";
            case shl:
                return "29";
            case shr:
                return "2a";
            case and:
                return "2b";
            case or:
                return "2c";
            case xor:
                return "2d";
            case not:
                return "2e";
            case cmpi:
                return "30";
            case cmpu:
                return "31";
            case cmpf:
                return "32";
            case negi:
                return "34";
            case negf:
                return "35";
            case itof:
                return "36";
            case ftoi:
                return "37";
            case shrl:
                return "38";
            case setlt:
                return "39";
            case setgt:
                return "3a";
            case br:
                return "41";
            case brfalse:
                return "42";
            case brtrue:
                return "43";
            case call:
                return "48";
            case ret:
                return "49";
            case callname:
                return "4a";
            case scani:
                return "50";
            case scanc:
                return "51";
            case scanf:
                return "52";
            case printi:
                return "54";
            case printc:
                return "55";
            case printf:
                return "56";
            case prints:
                return "57";
            case println:
                return "58";
            case panic:
                return "fe";
            default:
                return null;
        }
    }
}
