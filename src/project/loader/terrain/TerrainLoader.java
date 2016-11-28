package project.loader.terrain;

import f16cs350.atc.datatype.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class TerrainLoader {

    public static final int RLATLON_MINIMUM_SIZE = 7;
    private static final int RALT_MINIMUM_SIZE = 3;
    private static final int NODE_MINIMUM_SIZE = 5;
    private static final int SURFACE_MINIMUM_SIZE = 5;

    private class Coordinate implements Indexable{
        final int index;
        final int degrees;
        final int minutes;
        final double seconds;
        final Type type;

        public Coordinate(int index, int degrees, int minutes, double seconds, Type type){
            this.index = index;
            this.degrees = degrees;
            this.minutes = minutes;
            this.seconds = seconds;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Coordinate)) return false;

            Coordinate that = (Coordinate) o;
            
            return this.index == that.index
                    && this.type == that.type
                    && this.degrees == that.degrees
                    && this.minutes == that.minutes
                    && Double.compare(that.seconds, this.seconds) == 0;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private class Altitude implements Indexable{
        public final int index;
        public final double altitude;
        public Altitude(int index, double altitude){
            this.index = index;
            this.altitude = altitude;
        }

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (o == null || !(o instanceof Altitude)) return false;

            Altitude that = (Altitude)o;

            return this.altitude == that.altitude && this.index == that.index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private class Node implements Indexable{
        Coordinate latitude, longitude;
        Altitude altitude;
        public final int index;
        public final int latitudeIndex;
        public final int longitudeIndex;
        public final int altitudeIndex;

        public Node(int index, int latitudeIndex, int longitudeIndex, int altitudeIndex){
            this.index = index;
            this.latitudeIndex = latitudeIndex;
            this.longitudeIndex = longitudeIndex;
            this.altitudeIndex = altitudeIndex;
        }

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (o == null || !(o instanceof Node)) return false;

            Node that = (Node)o;

            return this.index == that.index
                    && this.latitudeIndex == that.latitudeIndex
                    && this.altitudeIndex == that.altitudeIndex
                    && this.longitudeIndex == that.longitudeIndex;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private class Surface implements Indexable{
        ArrayList<Node> nodes = new ArrayList<>();
        int[] indices;
        final int index;

        public Surface(int index, int[] indices){
            this.indices = indices;
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private interface Indexable {
         int getIndex();
    }

    private class Terrain{
        ArrayList<Surface> surfaces = new ArrayList<>();
        int[] indices;
        public Terrain (int indices[] ){
            this.indices = indices;
        }
    }

    private File terrainDefinition;
    private Hashtable<Integer, Coordinate> latitudeLookup = new Hashtable<>();
    private Hashtable<Integer, Coordinate> longitudeLookup = new Hashtable<>();
    private Hashtable<Integer, Altitude> altitudeLookup = new Hashtable<>();
    private Hashtable<Integer, Node> nodeLookup = new Hashtable<>();
    private Hashtable<Integer, Surface> surfaceLookup = new Hashtable<>();
    private Terrain terrain;

    private TerrainLoader(){}

    public TerrainLoader(File definition){
        this.terrainDefinition = definition;
    }

    public List<CoordinatesWorld3D_ATC> parse(){
        fillTables();
        return processTables();
    }

    private ArrayList<CoordinatesWorld3D_ATC> processTables() {
        processNodeTable();
        processSurfaceTable();
        processTerrain();
        ArrayList<CoordinatesWorld3D_ATC> terrainList = new ArrayList<>();
        for (Surface s: terrain.surfaces){
            for (Node n: s.nodes){
                terrainList.add(new CoordinatesWorld3D_ATC(new Latitude_ATC(n.latitude.degrees, n.latitude.minutes, n.latitude.seconds),
                        new Longitude_ATC(n.longitude.degrees, n.longitude.minutes, n.longitude.seconds),
                        new Altitude_ATC(n.altitude.altitude)));
            }
        }
        return terrainList;
    }

    private void processTerrain() {
        for (int index: terrain.indices){
            Surface tempSurface = surfaceLookup.get(index);
            if (tempSurface == null)
                throw new RuntimeException("Terrain invalid index");
            terrain.surfaces.add(tempSurface);
        }
    }

    private void processSurfaceTable() {
        for (Surface s: surfaceLookup.values()) {
            for (int index: s.indices){
                Node tempNode = nodeLookup.get(index);
                if (tempNode == null)
                    throw new RuntimeException("Surface index not valid");
                s.nodes.add(tempNode);
            }
        }
    }

    private void processNodeTable() {
        for (Node n: nodeLookup.values()) {
            Coordinate tempLat = latitudeLookup.get(n.latitudeIndex);
            if (tempLat == null)
                throw new RuntimeException("Node index not valid: Latitude");
            n.latitude = tempLat;
            Coordinate tempLon = longitudeLookup.get(n.longitudeIndex);
            if (tempLon == null)
                throw new RuntimeException("Node index not valid: Longitude");
            n.longitude = tempLon;
            Altitude tempAlt = altitudeLookup.get(n.altitudeIndex);
            if (tempAlt == null)
                throw new RuntimeException("Node index not valid: Altitude");
            n.altitude = tempAlt;
        }
    }

    private void fillTables() {
        TerrainLoaderParser tokenizer;
        try {
            tokenizer = new TerrainLoaderParser(this, new Scanner(terrainDefinition));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while(tokenizer.hasTokens()){
            Token token = tokenizer.nextToken();
            switch (token.type){
                case RLAT: Coordinate rlat = createCoordinate(createLatLonTokenGroup(tokenizer));
                    attemptPut(latitudeLookup, rlat);
                    break;
                case RLON: Coordinate rlon = createCoordinate(createLatLonTokenGroup(tokenizer));
                    attemptPut(longitudeLookup, rlon);
                    break;
                case RALT: Altitude ralt = createAltitude(createAltTokenGroup(tokenizer));
                    attemptPut(altitudeLookup, ralt);
                    break;
                case NODE: Node node = createNode(createNodeTokenGroup(tokenizer));
                    attemptPut(nodeLookup, node);
                    break;
                case SURFACE: Surface surface = createSurface(createSurfaceTokenGroup(tokenizer));
                    attemptPut(surfaceLookup, surface);
                    break;
                case TERRAIN: terrain = createTerrain(createTerrainTokenGroup(tokenizer));
                    break;
                default:
                    throw new RuntimeException("Format Error");
            }
        }
    }

    private Terrain createTerrain(Token[] terrainTokenGroup) {
        if(!validateTerrain(terrainTokenGroup))
            throw new RuntimeException("Invalid Terrain");
        int[] indicies = new int[terrainTokenGroup.length - 1];
        for (int i = 1; i < indicies.length; i++)
            indicies[i] = Integer.parseInt(terrainTokenGroup[i].data);
        return new Terrain(indicies);
    }

    private boolean validateTerrain(Token[] terrainTokenGroup) {
        if (terrainTokenGroup[0].type != Type.TERRAIN)
            return false;
        for (int i = 1; i < terrainTokenGroup.length; i++){
            if (terrainTokenGroup[i].type != Type.INTEGER)
                return false;
        }
        return true;
    }

    private Token[] createTerrainTokenGroup(TerrainLoaderParser tokenizer) {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Type.TERRAIN, "TERRAIN"));

        while (tokenizer.peek().type == Type.INTEGER)
            tokens.add(tokenizer.nextToken());

        return tokens.toArray(new Token[tokens.size()]);
    }

    private Surface createSurface(Token[] surfaceTokenGroup) {
        if(!validateSurface(surfaceTokenGroup))
            throw new RuntimeException("Invalid Surface Format");
        Token[] nodes = Arrays.copyOfRange(surfaceTokenGroup, 2, surfaceTokenGroup.length);
        int[] numbers = new int[nodes.length];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = Integer.parseInt(nodes[i].data);
        return new Surface(Integer.parseInt(surfaceTokenGroup[1].data), numbers);
    }

    private boolean validateSurface(Token[] surfaceTokenGroup) {
        if (surfaceTokenGroup[0].type != Type.SURFACE)
            return false;
        for (int i = 1; i < surfaceTokenGroup.length; i++){
            if (surfaceTokenGroup[i].type != Type.INTEGER)
                return false;
        }
        return true;
    }

    private Token[] createSurfaceTokenGroup(TerrainLoaderParser tokenizer) {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Type.SURFACE, "SURFACE"));
        while (tokenizer.peek().type == Type.INTEGER)
            tokens.add(tokenizer.nextToken());
        if (tokens.size() < SURFACE_MINIMUM_SIZE)
            throw new RuntimeException("Invalid surface size");
        return tokens.toArray(new Token[tokens.size()]);
    }

    private Node createNode(Token[] nodeTokenGroup) {
        if(!validateNode(nodeTokenGroup))
            throw new RuntimeException("Invalid Node");
        return new Node(Integer.parseInt(nodeTokenGroup[1].data),
                Integer.parseInt(nodeTokenGroup[2].data),
                Integer.parseInt(nodeTokenGroup[3].data),
                Integer.parseInt(nodeTokenGroup[4].data));
    }

    private boolean validateNode(Token[] nodeTokenGroup) {
        return (nodeTokenGroup[0].type == Type.NODE
        && nodeTokenGroup[1].type == Type.INTEGER
        && nodeTokenGroup[2].type == Type.INTEGER
        && nodeTokenGroup[3].type == Type.INTEGER
        && nodeTokenGroup[4].type == Type.INTEGER);
    }

    private Token[] createNodeTokenGroup(TerrainLoaderParser tokenizer) {
        Token[] toReturn = new Token[NODE_MINIMUM_SIZE];
        toReturn[0] = new Token(Type.NODE, "NODE");
        for (int i = 1; i < NODE_MINIMUM_SIZE; i++)
            toReturn[i] = tokenizer.nextToken();
        return toReturn;
    }

    private void attemptPut(Hashtable lookup, Indexable ralt) {
        if (lookup.get(ralt.getIndex()) != null)
            throw new RuntimeException("Index repeated");
        lookup.put(ralt.getIndex(), ralt);
    }

    private Altitude createAltitude(Token[] altTokenGroup) {
        if(!validateAltitude(altTokenGroup))
            throw new RuntimeException("Invalid format: bad altitude");
        return new Altitude(Integer.parseInt(altTokenGroup[1].data), Integer.parseInt(altTokenGroup[2].data));
    }

    private boolean validateAltitude(Token[] altTokenGroup) {
        return (altTokenGroup[0].type == Type.RALT
            && altTokenGroup[1].type == Type.INTEGER
            && altTokenGroup[2].type == Type.INTEGER);
    }

    private Token[] createAltTokenGroup(TerrainLoaderParser tokenizer) {
        Token[] toReturn = new Token[RALT_MINIMUM_SIZE];
        toReturn[0] = new Token(Type.RALT, "RALT");
        for (int i = 1; i < RALT_MINIMUM_SIZE; i++){
            toReturn[i] = tokenizer.nextToken();
        }

        String newData = toReturn[RALT_MINIMUM_SIZE - 1].data;
        if(tokenizer.peek().type == Type.PERIOD){
            newData += tokenizer.nextToken();
            if (tokenizer.peek().type == Type.INTEGER){
                newData += tokenizer.nextToken().data;
            }
        }
        toReturn[RALT_MINIMUM_SIZE - 1] = new Token(Type.DOUBLE, newData);
        return toReturn;
    }

    private Coordinate createCoordinate(Token[] coord) {
        if (!validateCoordinate(coord))
            throw new RuntimeException("Format Error");
        return new Coordinate(Integer.parseInt(coord[1].data),Integer.parseInt(coord[2].data), Integer.parseInt(coord[4].data),
                coord[6].type == Type.DOUBLE ? Double.parseDouble(coord[6].data) : Integer.parseInt(coord[6].data), coord[0].type );
    }

    private boolean validateCoordinate(Token[] coord){
        return !(coord[0].type == Type.RLAT || coord[0].type == Type.RLON)
                && coord[1].type != Type.INTEGER
                && coord[2].type != Type.INTEGER
                && coord[3].type != Type.COLON
                && coord[4].type != Type.INTEGER
                && coord[5].type != Type.COLON
                && !(coord[6].type == Type.INTEGER || coord[6].type == Type.DOUBLE);
    }

    private Token[] createLatLonTokenGroup(TerrainLoaderParser tokenizer) {
        Token[] toReturn = new Token[RLATLON_MINIMUM_SIZE];
        toReturn[0] = new Token(Type.RLAT, "RLAT");
        for(int i = 1; i < RLATLON_MINIMUM_SIZE; i++)
            toReturn[i] = tokenizer.nextToken();
        if (tokenizer.peek().type == Type.PERIOD){
            String toDouble = "";
            toDouble += toReturn[6].data;
            toDouble += tokenizer.nextToken().data;
            if(tokenizer.peek().type != Type.INTEGER)
                throw new RuntimeException("Format Error");
            toDouble += tokenizer.nextToken().data;
            toReturn[6] = new Token(Type.DOUBLE, toDouble);
        }
        return toReturn;
    }

    public String toString(){
        return super.toString();
    }
}
