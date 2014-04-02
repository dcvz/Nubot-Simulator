//
// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Quartet;
import sun.security.krb5.Config;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

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

    public void executeFrame()
    {
        ActionSet actions = computeActionSet();
        numberOfActions = actions.size();
        Action selected;

        do
        {
            selected = actions.selectArbitrary();
        }
        while (!executeAction(selected));

        timeElapsed += computeExponentialDistribution(actions.size() + 1);
    }

    // given a ruleset, compute a list of all possible actions
    // that can be executed in our current configuration
    private ActionSet computeActionSet()
    {
        ActionSet actSet = new ActionSet();
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
                        Quartet<String, String, Byte, Byte> keyRig;

                        if (this.containsKey(neighborPoint))
                        {
                            // there is a monomer in this neighboring position
                            Monomer neighbor = this.get(neighborPoint);
                            keyRig = Quartet.with(m.getState(), neighbor.getState(), m.getBondTo(neighborPoint), Direction.dirFromPoints(m.getLocation(), neighborPoint) );
                        }
                        else
                        {
                            // there is no monomer at this location
                            keyRig = Quartet.with(m.getState(), "empty", Bond.TYPE_NONE, Direction.dirFromPoints(m.getLocation(), neighborPoint));
                        }

                        // pass it on to RuleSet and see if any actions
                        // apply to this particular pair
                        if (rules.containsKey(keyRig))
                        {
                            // there is rules that apply to this particular pair
                            // iterate through the returned list and add to actions
                            for (Rule r : rules.get(keyRig))
                            {
                                actSet.add(new Action(m.getLocation(), neighborPoint, r));
                            }
                        }
                    }
                }
            }
        }

       return actSet;
    }

    private boolean executeAction(Action a)
    {
        return false;
    }

    private double computeExponentialDistribution(int i)
    {
        Random rand = new Random();
        double randNum = rand.nextDouble();

        while (randNum == 0.0)
            randNum = rand.nextDouble();

        return (-1 * Math.log(randNum)) / i;
    }
}