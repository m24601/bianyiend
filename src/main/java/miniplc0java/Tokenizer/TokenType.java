package miniplc0java.Tokenizer;

public enum TokenType {
    FN_KW,
    LET_KW,
    CONST_KW,
    AS_KW,
    WHILE_KW,
    IF_KW,
    ELSE_KW,
    RETURN_KW,
    BREAK_KW,
    CONTINUE_KW,

    TYPE_KW,

    IDENT,

    INT,
    STRING,
    DOUBLE,
    CHAR,

    PlUS,
    MINUS,
    MUL,
    DIV,
    ASSIGN,
    EQ,
    NEQ,
    LT,
    GT,
    LE,
    GE,
    L_PAREN,
    R_PAREN,
    L_BRACE,
    R_BRACE,
    ARROW,
    COMMA,
    COLON,
    SEMICOLON;

    @Override
    public String toString() {
        switch (this){
            case ELSE_KW:return "else";
            case CONTINUE_KW:return "continue";
            case BREAK_KW:return "break";
            case AS_KW:return "as";
            case FN_KW:return "fn";
            case IF_KW:return "if";
            case LET_KW:return "let";
            case CONST_KW:return "const";
            case WHILE_KW:return "while";
            case RETURN_KW:return "return";
            case IDENT:return "ident";
            case INT:return "int";
            case STRING:return "string";
            case CHAR:return "char";
            case DOUBLE:return "double";
            case PlUS:return "+";
            case MINUS:return "-";
            case MUL:return "*";
            case DIV:return "/";
            case ASSIGN:return "=";
            case EQ:return "==";
            case NEQ:return "!=";
            case LT:return "<";
            case GT:return ">";
            case LE:return "<=";
            case GE:return ">=";
            case L_PAREN:return "(";
            case R_PAREN:return ")";
            case L_BRACE:return "{";
            case R_BRACE:return "}";
            case ARROW:return "->";
            case COMMA:return ",";
            case COLON:return ":";
            case SEMICOLON:return ";";
            case TYPE_KW:return "ty";
            default:return null;
        }
    }
}
