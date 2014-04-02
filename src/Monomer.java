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

public class Monomer
{
    private Point location;
    private String state;
   private HashMap<Byte, Byte> neighborBonds = new HashMap<Byte, Byte>(); // Hashmap(direction,bondtype)
  private HashMap<Byte,ArrayList<Byte>>  neighborBondDirs = new HashMap<Byte, ArrayList<Byte>>();


    public Monomer(Point p, String s)
    {
        this.location = p;
        this.state = s;
        neighborBondDirs.put(Bond.TYPE_RIGID, new ArrayList<Byte>());
        neighborBondDirs.put(Bond.TYPE_FLEXIBLE, new ArrayList<Byte>());
    }

    // accessor methods
    public Point getLocation() { return location; }
    public String getState() { return state; }

    // mutator methods
    public void setLocation(Point p) { this.location= p; }
    public void setState(String s) { this.state = s; }
    public void adjustBond(byte direction, byte bondType)
    {
        neighborBonds.put(direction, bondType);
        neighborBondDirs.get(bondType).add(direction);
    }

    public byte getBondTypeByDir(byte direction) {
        return neighborBonds.get(direction);
    }
    public ArrayList<Byte> getDirsByBondType(byte bondType)
    {
        return neighborBondDirs.get(bondType);
    }

    public boolean hasBonds()
    {
        return !neighborBonds.isEmpty();
    }
    public byte getBondTo(Point neighborPoint)
    {
        return neighborBonds.get(Direction.dirFromPoints(location, neighborPoint));
    }

}
