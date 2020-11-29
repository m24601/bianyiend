package miniplc0java.Analyser;

public class Variable {
    String name;
    boolean isConst;
//    boolean isInitialized;
    boolean isGlobal;
    boolean isParam;
    boolean isLocal;
    int offset;
    VariableType variableType;
    String value;
    public Variable(String name,boolean isConst,VariableType variableType,boolean isGlobal,boolean isParam,boolean isLocal,String value){
        this.name=name;
        this.isConst=isConst;
//        this.isInitialized=isInitialized;
        this.variableType=variableType;
        this.isGlobal=isGlobal;
        this.isParam=isParam;
        this.isLocal=isLocal;
        this.value=value;
    }
}
