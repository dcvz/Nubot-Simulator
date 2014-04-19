// Monomer.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;
import java.io.Serializable;
import java.util.*;

public class Monomer implements Serializable {
    static final long serialVersionUID = 1234L;
    /**
     * Location of monomer on grid
     *
     * @serial
     */
    private Point location;

    /**
     * String state of monomer
     *
     * @serial
     */
    private String state;

    /**
     * Hashmap(direction,bondtype)   pass a Direction.Type as key, receive the bondtype, is 0 or Bond.TYPE_NONE if no bond exists, or if no neighbor
     *
     * @serial
     */
    private HashMap<Byte, Byte> neighborBonds = new HashMap<Byte, Byte>();

    /**
     * Hashmap(Bond.Type, ArrayList<Direction.TYPE>)  pass Bond.TYPE as key, receive an ArrayList of Direction.TYPE(Byte) that have this bond type
     *
     * @serial
     */
    private HashMap<Byte, ArrayList<Byte>> neighborBondDirs = new HashMap<Byte, ArrayList<Byte>>();//Hashmap(BondType, ArrayList<Direction>)


    /**
     * id
     *
     * @serial
     */
    private int id;

    //================================================================================
    // Constructors
    //================================================================================
    public Monomer(Monomer m) {


        this.id = m.getId();
        this.state = m.getState();
        this.location = m.getLocation();
        Set<Map.Entry<Byte, ArrayList<Byte>>> copyFrom1 = m.getNeighborBondDirs().entrySet();
        for (Map.Entry<Byte, ArrayList<Byte>> set : copyFrom1) {
            ArrayList<Byte> temp = new ArrayList<Byte>();
            for (Byte b : set.getValue()) {
                temp.add((Byte) b.byteValue());
            }

            this.neighborBondDirs.put(set.getKey(), temp);
        }
        Set<Map.Entry<Byte, Byte>> copyFrom2 = m.getNeighborBonds().entrySet();
        for (Map.Entry<Byte, Byte> set : copyFrom2) {
            this.neighborBonds.put((Byte) set.getKey().byteValue(), (Byte) set.getValue().byteValue());
        }


    }

    public int getId() {
        return id;
    }

    public Monomer(Point p, String s) {
        this.location = p;
        this.state = s;
        Random rand = new Random();
        this.id = rand.nextInt();
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

    public HashMap<Byte, Byte> getNeighborBonds() {
        return neighborBonds;
    }

    public HashMap<Byte, ArrayList<Byte>> getNeighborBondDirs() {
        return neighborBondDirs;
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

    /*
    public void adjustBondTo(Monomer m, byte bondType) {
        byte Dir = Direction.dirFromPoints(location, m.getLocation());
        adjustBond(Dir, bondType);
        m.adjustBond(Direction.dirFromPoints(m.getLocation(), location), bondType);
    }
    */

    public byte getBondTypeByDir(byte direction) {
        if (direction < 33 && direction % 2 == 0 || direction == 1)
            return neighborBonds.get(direction);
        return 0;

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

    public boolean conflicts(Monomer m, byte dir) {
        Point me = getLocation();
        Point meShifted = Direction.translatedPointByDir(me, dir);
        Point neighbor = m.getLocation();

        // check for a collision
        if (meShifted.equals(neighbor))
            return true;

        if (neighborBonds.get(Direction.dirFromPoints(me.getLocation(), neighbor.getLocation())) == Bond.TYPE_RIGID)
            return true;

        if (neighborBonds.get(Direction.dirFromPoints(me.getLocation(), neighbor.getLocation())) == Bond.TYPE_FLEXIBLE) {
            if (!m.adjacent(meShifted))
                return true;
        }

        return false;
    }

    // is monomer adjacent to point p?
    boolean adjacent(Point p) {
        if ((Math.abs(getLocation().x - p.x) > 1) || (Math.abs(getLocation().y - p.y) > 1))
            return false;
        if (getLocation().x == p.x && getLocation().y == p.y)
            return false;
        else if (Math.abs(getLocation().x - p.x + getLocation().y - p.y) <= 1)
            return true;
        else
            return false;
    }

    public void shift(byte dir) {
        location = Direction.translatedPointByDir(location, dir);
    }

    public void adjustFlexibleBond(Monomer m, byte dir, HashMap<Byte, Byte> buffer) {
        adjustBond(Direction.dirFromPoints(getLocation(), m.getLocation()), Bond.TYPE_NONE);
        m.adjustBond(Direction.dirFromPoints(m.getLocation(), getLocation()), Bond.TYPE_NONE);

        shift(dir);

        if (!adjacent(m.getLocation()))
            System.out.println("uh, oh...");

        //m.getBondTypeByDir(Direction.dirFromPoints(m.getLocation(), getLocation()));
        m.adjustBond(Direction.dirFromPoints(m.getLocation(), getLocation()), Bond.TYPE_FLEXIBLE);
        buffer.put(Direction.dirFromPoints(getLocation(), m.getLocation()), Bond.TYPE_FLEXIBLE);

        shift(Direction.getOppositeDir(dir));
    }
}