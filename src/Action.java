//
// Action.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;
import java.io.Serializable;

// Point a, Point b, rule
public class Action implements Serializable {
    static final long serialVersionUID = 1234L;
    /**
     * Location of monomer1 on grid
     *
     * @serial
     */
    private Point mon1;
    /**
     * Location of monomer2 on grid
     *
     * @serial
     */
    private Point mon2;
    /**
     * Rule that applies to the two monomers
     *
     * @serial
     */
    private Rule rule;

    //================================================================================
    // Constructors
    //================================================================================

    public Action(Point a, Point b, Rule r) {
        this.mon1 = a;
        this.mon2 = b;
        this.rule = r;
    }

    //================================================================================
    // Accessors
    //================================================================================

    public Point getMon1() {
        return mon1;
    }

    public Point getMon2() {
        return mon2;
    }

    public Rule getRule() {
        return rule;
    }

    //================================================================================
    // Mutators
    //================================================================================

    public void setMon1(Point p) {
        this.mon1 = p;
    }

    public void setMon2(Point p) {
        this.mon2 = p;
    }

    public void setRule(Rule r) {
        this.rule = r;
    }
}
