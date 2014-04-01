//
// Rule.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;

public class Rule
{
    private String s1, s2, s1p, s2p;
    private Bond start, end;
    private Point dir, dirp;

    public Rule(String a, String b, Bond c, Point d, String e, String f, Bond g, Point h)
    {
        this.s1 = a; this.s2 = b; this.start = c; this.dir = d;
        this.s1p = e; this.s2p = f; this.end = g; this.dirp = h;
    }

    // accessor methods
    public String getS1() { return s1; }
    public String getS2() { return s2; }
    public Bond getStart() { return start; }
    public Point getDir() { return dir; }
    public String getS1p() { return s1p; }
    public String getS2p() { return s2p; }
    public Bond getEnd() { return end; }
    public Point getDirp() { return dirp; }

    // mutator methods
    public void setS1(String s) { this.s1 = s; }
    public void setS2(String s) { this.s2 = s; }
    public void setStart(Bond b) { this.start = b; }
    public void setDir(Point p) { this.dir = p; }
    public void setS1p(String s) { this.s1p = s; }
    public void setS2p(String s) { this.s2p = s; }
    public void setEnd(Bond b) { this.end = b; }
    public void setDirp(Point p) { this.dirp = p; }
}
