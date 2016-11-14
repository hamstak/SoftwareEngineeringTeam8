package project.loader.terrain;

import f16cs350.atc.datatype.CoordinatesWorld3D_ATC;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

public class TerrainLoader {

    private File terrainDefinition;

    private TerrainLoader(){}

    public TerrainLoader(File definition){
        this.terrainDefinition = definition;
    }

    public List<CoordinatesWorld3D_ATC> parse(){
        return null;
    }

    public String toString(){
        return super.toString();
    }
}
