package project.loader.terrain;

import f16cs350.atc.datatype.CoordinatesWorld3D_ATC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TerrainLoader {
    private enum Type{
        RLAT, RLON, RALT, NODE, SURFACE, TERRAIN
    }

    private class Coordinate{
        final int index;
        final int degrees;
        final int minutes;
        final double seconds;
        final String type;

        public Coordinate(int index, int degrees, int minutes, double seconds, String type){
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
                    && this.degrees == that.degrees
                    && this.minutes == that.minutes
                    && Double.compare(that.seconds, this.seconds) == 0;
        }
    }

    private class Altitude{
        public final int index;
        public final int altiude;
        public Altitude(int index, int altitude){
            this.index = index;
            this.altiude = altitude;
        }

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (o == null || !(o instanceof Altitude)) return false;

            Altitude that = (Altitude)o;

            return this.altiude == that.altiude && this.index == that.index;
        }
    }

    private class Node{
        Coordinate Latitude, Longitude;
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
    }

    private class Surface{
        ArrayList<Node> nodes = new ArrayList<>();
        int[] indices;
        final int index;

        public Surface(int index, int[] indices){
            this.indices = indices;
            this.index = index;
        }
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

    private TerrainLoader(){}

    public TerrainLoader(File definition){
        this.terrainDefinition = definition;
    }

    public List<CoordinatesWorld3D_ATC> parse(){
        Scanner fin;

        try{
            fin = new Scanner(terrainDefinition);
        }catch (FileNotFoundException e){
            throw new RuntimeException(e.getMessage());
        }

        ArrayList<CoordinatesWorld3D_ATC> terrain = new ArrayList<>();

        return null;
    }

    private void fillTables(Scanner fin) {

    }

    public String toString(){
        return super.toString();
    }
}
