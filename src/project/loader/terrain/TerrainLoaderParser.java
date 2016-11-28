package project.loader.terrain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

class TerrainLoaderParser {
    private TerrainLoader terrainLoader;
    Scanner iScanner;
    Iterator<Token> tokens;
    Token current;
    boolean inComment;
    boolean inBlockComment;
    private Token next;

    public TerrainLoaderParser(TerrainLoader terrainLoader, Scanner iScanner) {
        this.terrainLoader = terrainLoader;
        this.iScanner = iScanner;
        iScanner.useDelimiter("(\\b|[\\r\\n\\t\\f\\v ])");
        ProcessFile();
    }

    private void ProcessFile() {
        ArrayList<Token> iterator = new ArrayList<>();
        while (iScanner.hasNext()) {
            String next = iScanner.next();
            Token toAdd = createToken(next);
            if (toAdd != null || toAdd.type != null || toAdd.type != Type.COMMENT || toAdd.type != Type.EOL)
                iterator.add(toAdd);
        }
        tokens = iterator.iterator();
    }

    private Token createToken(String toTokenize) {
        if (toTokenize.matches("\n")) {
            inComment = false;
            return new Token(Type.EOL, toTokenize);
        } else if (toTokenize.matches("\\*/")) {
            inBlockComment = false;
            return new Token(Type.COMMENT, toTokenize);
        } else if (inBlockComment || inComment) {
            return null;
        } else if (toTokenize.matches("//")) {
            inComment = true;
            return new Token(Type.COMMENT, toTokenize);
        } else if (toTokenize.matches("\\."))
            return new Token(Type.PERIOD, toTokenize);
        else if (toTokenize.matches(":"))
            return new Token(Type.COLON, toTokenize);
        else if (toTokenize.matches("\\w+"))
            return characterToken(toTokenize);
        else if (toTokenize.matches("\\d+"))
            return new Token(Type.INTEGER, toTokenize);

        return null;
    }

    private Token characterToken(String toTokenize) {
        if (toTokenize.matches("RLAT"))
            return new Token(Type.RLAT, toTokenize);
        else if (toTokenize.matches("RLON"))
            return new Token(Type.RLON, toTokenize);
        else if (toTokenize.matches("RALT"))
            return new Token(Type.RALT, toTokenize);
        else if (toTokenize.matches("NODE"))
            return new Token(Type.NODE, toTokenize);
        else if (toTokenize.matches("SURFACE"))
            return new Token(Type.SURFACE, toTokenize);
        else if (toTokenize.matches("TERRAIN"))
            return new Token(Type.TERRAIN, toTokenize);
        return null;
    }

    public boolean hasTokens() {
        return tokens.hasNext();
    }

    public Token getToken() {
        return current;
    }

    public void ConsumeLine() {
        while (current.type != Type.EOL)
            current = tokens.next();
        current = tokens.next();
    }

    public Token nextToken() {
        if (next != null) {
            current = next;
            next = null;
        }
        current = tokens.next();
        return getToken();
    }

    public Token peek() {
        if (next == null)
            next = tokens.next();
        return next;

    }

}
