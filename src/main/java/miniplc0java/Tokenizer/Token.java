package miniplc0java.Tokenizer;

public class Token {
    private TokenType tokenType;
    private Object value;
    private Pos startPos;
    private boolean isConst;
    public Token(TokenType tokenType,Object value,Pos startPos){
        this.tokenType=tokenType;
        this.value=value;
        this.startPos=startPos;
    }
    public Object getValue() {
        return value;
    }
    public TokenType getTokenType(){return tokenType;}
    public Pos getStartPos(){return startPos;}
}
