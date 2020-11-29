package miniplc0java.Analyser;

public enum StackEnum {
    INT,
    DOUBLE,
    ADDR,
    BOOL;
    @Override
    public String toString(){
        switch (this){
            case INT:
                return "int";
            case DOUBLE:
                return "double";
            case ADDR:
                return "address";
            case BOOL:
                return "bool";
            default:
                return null;
        }
    }
}
