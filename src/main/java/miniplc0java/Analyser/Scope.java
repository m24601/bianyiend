package miniplc0java.Analyser;

import java.util.ArrayList;

public class Scope {
    public ArrayList<ArrayList<Variable>>variableLists=new ArrayList<>();
    public void addScope(){
        variableLists.add(new ArrayList<>());
    }
    public void addVariable(Variable variable){
        ArrayList<Variable>variables=variableLists.get(variableLists.size()-1);
        for(int i=0;i<variables.size();i++){
            if(variables.get(i).name.equals(variable.name))
                throw new Error("variable ["+variable.name+"] redefined "+Analyser.getPos());
        }
        variableLists.get(variableLists.size()-1).add(variable);
    }
    public void addGlobalVariable(Variable variable){
        variableLists.get(0).add(variable);
    }
    public void removeScope(){
        variableLists.remove(variableLists.size()-1);
    }
    public Variable getVariable(String name){
        for(int i=variableLists.size()-1;i>=0;i--){
            ArrayList<Variable>variables=variableLists.get(i);
            for(int j=variables.size()-1;j>=0;j--){
                if(variables.get(j).name.equals(name))
                    return variables.get(j);
            }
        }
        throw new Error("cannot identify ident "+name+" "+Analyser.getPos());
    }
}
