package miniplc0java;
import miniplc0java.Analyser.Analyser;
import java.io.File;
import java.io.IOException;

public class App {
    public static void main(String[]args) throws IOException {
        Analyser analyser=new Analyser(new File(args[0]),new File(args[1]));
    }
}