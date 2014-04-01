//
// Monomer.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;

public class Monomer
{
    private Point location;
    private String state;

    public Monomer(Point p, String s)
    {
        this.location = p;
        this.state = s;
    }

    // accessor methods
    public Point getLocation() { return location; }
    public String getState() { return state; }

    // mutator methods
    public void setLocation(Point p) { this.location= p; }
    public void setState(String s) { this.state = s; }
}
