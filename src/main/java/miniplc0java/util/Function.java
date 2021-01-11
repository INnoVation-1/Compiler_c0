package miniplc0java.util;

import java.util.ArrayList;
import java.util.List;

public class Function {
    public int name;
    public int returnSlots = 0;
    public int paramSlots = 0;
    public int locSlots = 0;
    public List<Instruction> body = new ArrayList();

    public void addInstruction(Instruction i){
        body.add(i);
    }
}
