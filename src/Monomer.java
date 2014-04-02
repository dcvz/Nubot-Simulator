//
// Monomer.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Monomer {
    private Point location;
    private String state;
    private HashMap<Byte, Byte> neighborBonds = new HashMap<Byte, Byte>(); // Hashmap(direction,bondtype)
    private HashMap<Byte, ArrayList<Byte>> neighborBondDirs = new HashMap<Byte, ArrayList<Byte>>();

    //================================================================================
    // Constructors
    //================================================================================

    public Monomer(Point p, String s) {
        this.location = p;
        this.state = s;
        neighborBondDirs.put(Bond.TYPE_RIGID, new ArrayList<Byte>());
        neighborBondDirs.put(Bond.TYPE_FLEXIBLE, new ArrayList<Byte>());
        neighborBondDirs.put(Bond.TYPE_NONE, new ArrayList<Byte>());
        neighborBonds.put(Direction.TYPE_FLAG_EAST, Bond.TYPE_NONE);
        neighborBonds.put(Direction.TYPE_FLAG_NORTHEAST, Bond.TYPE_NONE);
        neighborBonds.put(Direction.TYPE_FLAG_SOUTHEAST, Bond.TYPE_NONE);
        neighborBonds.put(Direction.TYPE_FLAG_SOUTHWEST, Bond.TYPE_NONE);
        neighborBonds.put(Direction.TYPE_FLAG_NORTHWEST, Bond.TYPE_NONE);
        neighborBonds.put(Direction.TYPE_FLAG_WEST, Bond.TYPE_NONE);
    }

    //================================================================================
    // Accessors
    //================================================================================

    public Point getLocation() {
        return location;
    }

    public String getState() {
        return state;
    }

    //================================================================================
    // Mutators
    //================================================================================

    public void setLocation(Point p) {
        this.location = p;
    }

    public void setState(String s) {
        this.state = s;
    }

    //================================================================================
    // Functionality Methods
    //================================================================================

    public void adjustBond(Byte direction, Byte bondType) {
        neighborBonds.put(direction, bondType);

        neighborBondDirs.get(Bond.TYPE_FLEXIBLE).remove(direction);

        neighborBondDirs.get(Bond.TYPE_RIGID).remove(direction);

        neighborBondDirs.get(Bond.TYPE_NONE).remove(direction);

        neighborBondDirs.get(bondType).add(direction);
    }

    public void adjustBondTo(Monomer m, byte bondType) {
        byte Dir = Direction.dirFromPoints(location, m.getLocation());
        adjustBond(Dir, bondType);
        m.adjustBond(Direction.dirFromPoints(m.getLocation(), location), bondType);
    }

    public byte getBondTypeByDir(byte direction) {
        return neighborBonds.get(direction);
    }

    public ArrayList<Byte> getDirsByBondType(byte bondType) {
        return neighborBondDirs.get(bondType);
    }

    public boolean hasBonds() {
        return !neighborBonds.isEmpty();
    }

    public byte getBondTo(Point neighborPoint) {
        if (neighborBonds.containsKey(Direction.dirFromPoints(location, neighborPoint)))
            return neighborBonds.get(Direction.dirFromPoints(location, neighborPoint));
        else
            return Bond.TYPE_NONE;
    }

}
