package miniplc0java.Analyser;

import java.util.ArrayList;
import java.util.Arrays;

public class Stack {
    ArrayList<StackEnum>stackEnums=new ArrayList<>();
    public void push(StackEnum...stackEnums){
        this.stackEnums.addAll(Arrays.asList(stackEnums));
    }
    public void push(VariableType...variableType){
        for (VariableType type : variableType) {
            this.stackEnums.add(change(type));
        }
    }
    public void pop(StackEnum...stackEnums){
        for(int i=stackEnums.length-1;i>=0;i--){
            if(stackEnums[i]==top()){
                this.stackEnums.remove(this.stackEnums.size()-1);
            }else
                throw new Error("type "+stackEnums[i].toString()+"cannot be here "+Analyser.getPos());
        }
        return;
    }
    public void pop(VariableType...variableTypes){
        for(int i=variableTypes.length-1;i>=0;i--){
            if(change(variableTypes[i])==this.stackEnums.get(this.stackEnums.size()-1)){
                this.stackEnums.remove(top());
            }else
                throw new Error("type "+variableTypes[i].toString()+" cannot be here "+Analyser.getPos());
        }
        return;
    }
    public StackEnum preTop() {
        return stackEnums.get(stackEnums.size()-2);
    }
    public StackEnum change(VariableType variableType){
        switch (variableType) {
            case INT:
                return StackEnum.INT;
            case DOUBLE:
                return StackEnum.DOUBLE;
            default:
                return null;
        }
    }
    public StackEnum top(){
        return stackEnums.get(stackEnums.size()-1);
    }
}
