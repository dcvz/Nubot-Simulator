//
// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Quartet;

import javax.xml.bind.annotation.XmlElementDecl;
import java.awt.*;
import java.util.HashMap;

public class Configuration extends HashMap<Point, Monomer>
{
    public RuleSet rules;
    public boolean isFinished;
    public double timeElapsed;
    public int numberOfActions;
    public int numberOfMonomers;

    public Configuration()
    {
        rules = new RuleSet();
        isFinished = false;
        timeElapsed = 0.0;
        numberOfActions = numberOfMonomers = 0;
    }

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
        } while (!executeAction(selected));

        timeElapsed += Simulation.calculateExpDistribution(numberOfActions + 1);
    }

    // given a ruleset, compute a list of all possible actions
    // that can be executed in our current configuration
    public ActionSet computeActionSet()
    {
        ActionSet ret = new ActionSet();
        for (Monomer m : this.values())
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; j++)
                {
                    if (i == j)
                    {
                        // do nothing.
                    }
                    else
                    {
                        Point neighborPoint = new Point(m.getLocation().x + i, m.getLocation().y + j);
                        Quartet<String, String, Byte, Byte> key = Quartet.with("","", (byte)0, (byte)0);

                        if (this.containsKey(neighborPoint))
                        {
                            // there is a monomer in this neighboring position
                            Monomer neighbor = this.get(neighborPoint);
                            key = Quartet.with(m.getState(), neighbor.getState(), m.getBondTo(neighborPoint), Direction.dirFromPoints(m.getLocation(), neighborPoint) );
                        }
                        else
                        {
                            // there is no monomer at this location
                            key = Quartet.with(m.getState(), "empty", Bond.TYPE_NONE, Direction.dirFromPoints(m.getLocation(), neighborPoint));
                        }

                        // pass it on to RuleSet and see if any actions
                        // apply to this particular pair
                        if (rules.containsKey(key))
                        {
                            // there is rules that apply to this particular pair
                            // iterate through the returned list and add to actions
                            for (Rule r : rules.get(key))
                            {
                                ret.add(new Action(m.getLocation(), neighborPoint, r));
                            }
                        }
                    }
                }
            }
        }

       return ret;
    }

    private boolean executeAction(Action a)
    {
        if (a.getRule().getClassification() == Rule.RuleType.STATECHANGE)
        {
            return performStateChange(a);
        }

        return false;
    }

    // action execution types
    private boolean performStateChange(Action a)
    {
        boolean exMon1 = false;
        boolean exMon2 = false;

        // if monomer 1 exists (we could have the case of "A, empty, 0, NE -> B, empty, 0, NE").
        if (this.containsKey(a.getMon1()))
        {
            Monomer tmp = this.get(a.getMon1());
            tmp.setState(a.getRule().getS1p());
            exMon1 = true;
        }

        // if monomer 2 exists (we could have the case of "empty, A, 0, NE -> empty, B, 0, NE").
        if (this.containsKey(a.getMon2()))
        {
            Monomer tmp = this.get(a.getMon2());
            tmp.setState(a.getRule().getS2p());
            exMon2 = true;
        }

        // check if there is a change in bond type
        if (a.getRule().getBond() != a.getRule().getBondp())
        {
            if (exMon1 && exMon2)
            {
                adjustBondTo(Direction.dirFromPoints(a.getMon1(), a.getMon2()), /*complete this */);
            }
        }

    }
}