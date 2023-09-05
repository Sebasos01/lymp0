package org.ospi;

import org.ospi.lexer.Lexer;
import org.ospi.lexer.Token;
import org.ospi.parser.Parser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// Java 17 es usado
public class Main {
    public static void main(String[] args) throws Exception {
        // Insert here the source code path
        String source = new String(Files.readAllBytes(Paths.get("data/source.txt")));
        Lexer lexer = new Lexer();
        try {
            List<Token> tokens = lexer.getTokens(source);
            System.out.println(tokens);
            Parser parser = new Parser(tokens);
            parser.parseP();
        } catch (Exception e) {
            System.out.println("NO. " + e.getMessage());
        }
    }
}