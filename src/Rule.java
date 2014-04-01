//
// Rule.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Quartet;

import java.awt.*;

public class Rule
{
    private String s1, s2, s1p, s2p;
    private byte bond, bondp;
    private Byte dir, dirp;

    public Rule(String State1, String State2, Byte initialBondType, Byte S2initialDirection, String State1P, String State2P,Byte endBondType, Byte S2EndDirection)
    {
        this.s1 = State1; this.s2 = State2; this.bond = initialBondType; this.dir = S2initialDirection;
        this.s1p = State1P; this.s2p = State2P; this.bondp = endBondType; this.dirp = S2EndDirection;
    }

    // accessor methods
    public String getS1() { return s1; }
    public String getS2() { return s2; }
    public Byte getBond() { return bond; }
    public Byte getDir() { return dir; }
    public String getS1p() { return s1p; }
    public String getS2p() { return s2p; }
    public Byte getBondp() { return bondp; }
    public Byte getDirp() { return dirp; }

    // mutator methods
    public void setS1(String s) { this.s1 = s; }
    public void setS2(String s) { this.s2 = s; }
    public void setStart(byte b) { this.bond = b; }
    public void setDir(Byte p) { this.dir = p; }
    public void setS1p(String s) { this.s1p = s; }
    public void setS2p(String s) { this.s2p = s; }
    public void setEnd(Byte b) { this.bondp = b; }
    public void setDirp(Byte p) { this.dirp = p; }
    public Quartet<String, String, Byte, Byte> getInputQuartet()
    {


        return Quartet.with(s1, s2, bond, dir);
    }
    public Quartet<String, String, Byte,Byte> getOutputQuartet()
    {
        return Quartet.with(s1p, s2p, bondp, dirp);
    }
}
