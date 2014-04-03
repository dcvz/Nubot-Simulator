//
// Agtion.java
// Nubot Simulator
//
// Created by David Chavez on 4/2/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;

public class Agtion
{
    private Point mon;
    private Byte dir;

    //================================================================================
    // Accessors
    //================================================================================

    public Point getMon() { return mon; }
    public Byte getDire() { return dir; }

    //================================================================================
    // Mutators
    //================================================================================\

    public void setMon(Point p) { this.mon = p; }
    public void setDir(Byte b) { this.dir = b; }
}
