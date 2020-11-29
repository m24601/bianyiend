package miniplc0java.Analyser;

import java.util.ArrayList;

public class Function {
    public String name;
    public int returnSlot=0;
    public int paramSlot=0;
    public int locSlot=0;
    public int instructionNum=0;
    public VariableType variableType;
    public ArrayList<ArrayList<Byte>>instructions=new ArrayList<>();
    public ArrayList<String>instructionsString=new ArrayList<>();
    public ArrayList<Variable> params=new ArrayList<>();
    public ArrayList<Variable> locals=new ArrayList<>();
    public Function(String name){
        this.name=name;
    }
    public String getInstructionsString(){
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<instructionsString.size();i++){
            stringBuilder.append(instructionsString.get(i)+"\n");
        }
        return stringBuilder.toString();
    }
    public ArrayList<Byte> getInstructions() {
        ArrayList<Byte>bytes=new ArrayList<>();
        for(ArrayList<Byte>instructions:this.instructions)bytes.addAll(instructions);
        return bytes;
    }
}
