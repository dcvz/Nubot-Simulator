//
// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;
import java.util.HashMap;

public class Configuration extends HashMap<Point, Monomer>
{
    public RuleSet rules;
    public boolean isFinished;
    public double timeElapsed;
    public int numberOfActions;
    public int numberOfMonomers;

    public boolean addMonomer(Monomer m)
    {
        if(!this.containsKey(m.getLocation()))
        {
            this.put(m.getLocation(), m);
            return true;
        }
        return false;
    }

    public void executeAction()
    {
        ActionSet actions = computeActionSet();
        numberOfActions = actions.size();
    }

    // given a ruleset, compute a list of all possible actions
    // that can be executed in our current configuration
    private ActionSet computeActionSet()
    {

    }
}