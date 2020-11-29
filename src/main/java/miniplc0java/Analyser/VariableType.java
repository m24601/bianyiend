package miniplc0java.Analyser;

public enum VariableType {
    INT,
    DOUBLE,
    VOID,
    STRING;
//    FUNCTION,
    public String toString(){
        switch (this){
            case INT:
            case STRING:
                return "int";
            case DOUBLE:
                return "double";
            default:
                return null;
        }
    }
}
