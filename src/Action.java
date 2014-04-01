//
// Action.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;

// Point a, Point b, rule
public class Action
{
    private Point mono1, mono2;
    private Rule rule;

    public Action(Point a, Point b, Rule r)
    {
        mono1 = a;
        mono2 = b;
        rule = r;
    }
    public Point getMono1()   {return mono1;}
    public Point getMono2()     {return mono2;}
    public Rule getRule()   {return rule;}

    public void setMono1(Point a)
    {
        mono1 = a;
    }
    public void setMono2(Point a)
    {
        mono2 = a;
    }
    public void setRule(Rule r)
    {
        rule = r;
    }

}
