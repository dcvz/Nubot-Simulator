//
// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Quartet;

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
        for (Monomer m : this.values())
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; i++)
                {
                    if (i == j)
                    {
                        // do nothing.
                    }
                    else
                    {
                        Point neighborPoint = new Point(m.getLocation().x + i, m.getLocation().y + j);
                        if (this.containsKey(neighborPoint))
                        {
                            // there is a monomer in this neighboring position
                            // pass it on to RuleSet and see if any actions
                            // apply to this pair of momomers
                            Monomer neighbor = this.get(neighborPoint);
                            Quartet<String, String, Byte, Byte> key = Quartet.with(m.getState(), neighbor.getState(), )
                            if (rules.containsKey(key)
                            {
                                // there is rules that apply to this particular pair
                                // itirate through the returned list and add to actions
                            }
                        }
                    }
                }
            }
        }
    }
}