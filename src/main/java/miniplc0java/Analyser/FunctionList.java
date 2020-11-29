package miniplc0java.Analyser;

import java.util.ArrayList;

public class FunctionList {
    ArrayList<Function>functions=new ArrayList<>();
    Function topFunction=null;
    public void add(Function function){
        for(int i=1;i<functions.size();i++){
            if(functions.get(i).name.equals(function.name))
                throw new Error("function ["+function.name+"] redefined "+Analyser.getPos());
        }
        functions.add(function);
        topFunction=function;
    }
    public void addVariable(Variable variable){
        if(variable.isGlobal){
            variable.offset=functions.get(0).params.size();
            functions.get(0).params.add(variable);
        }else if(variable.isParam){
            variable.offset=topFunction.params.size();
            topFunction.params.add(variable);
            topFunction.paramSlot++;
        }else if(variable.isLocal){
            variable.offset=topFunction.locals.size();
            topFunction.locals.add(variable);
            topFunction.locSlot++;
        }else
            throw new Error("cannot identify type of variable "+Analyser.getPos());
    }
    public void setVariableType(VariableType variableType){
        topFunction.variableType=variableType;
    }
    public void setReturnSlot(){
        topFunction.returnSlot=1;
    }
    public void addInstruction(String instructionString,byte...instruction){
        addInstruction(topFunction,instructionString,instruction);
    }
    public void addInitialInstruction(String instructionString,byte...instruction){
        addInstruction(functions.get(0),instructionString,instruction);
    }
    public void addInstruction(Function function,String instructionString,byte...instruction){
        function.instructionsString.add(instructionString);
        function.instructions.add(new ArrayList<>());
        int num=function.instructions.size()-1;
        for(byte b:instruction)function.instructions.get(num).add(b);
        function.instructionNum++;
    }
    public void replaceInstruction(int pos,String instructionString,byte...instruction){
        topFunction.instructionsString.remove(pos);
        topFunction.instructionsString.add(pos,instructionString);
        topFunction.instructions.get(pos).clear();
        for(byte b:instruction)topFunction.instructions.get(pos).add(b);
    }
    public int getInstructionNum(){return topFunction.instructionNum;}
    public Integer searchFunction(String name){
        for(String s:Analyser.stdio){
            if(s.equals(name))
                return null;
        }
        for(int i=1;i<functions.size();i++){
            if(functions.get(i).name.equals(name))
                return i;
        }
        throw new Error("cannot identify function ["+name+"] "+Analyser.getPos());
    }
}
