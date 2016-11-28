package project.loader.terrain;

class Token {
    Type type = Type.DEFAULT;
    String data;

    public Token(Type type, String data) {
        this.type = type;
        this.data = data;
    }
}
