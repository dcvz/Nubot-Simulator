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

    //================================================================================
    // Constructors
    //================================================================================

    public Configuration()
    {
        rules = new RuleSet();
        isFinished = false;
        timeElapsed = 0.0;
        numberOfActions = 0;
    }

    //================================================================================
    // Functionality Methods
    //================================================================================

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
            return performStateChange(a);
        else if (a.getRule().getClassification() == Rule.RuleType.INSERTION)
            return performInsertion(a);
        else if (a.getRule().getClassification() == Rule.RuleType.DELETION)
            return performDeletion(a);

        return false;
    }

    //================================================================================
    // Action Execution Types
    //================================================================================

    // action that consists of only changing the states of monomers and/or bond types
    private boolean performStateChange(Action a)
    {
        // flags to indicate whether the monomers exist in the configuration
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
            // if both monomers exist only then can we form bonds
            if (exMon1 && exMon2)
            {
                // adjust bond types
                adjustBonds(a.getMon1(), a.getMon2(), a.getRule().getBondp());
            }
        }

        return true;
    }

    // action that consists of inserting a monomer
    private boolean performInsertion(Action a)
    {
        // if monomer 1 exists then it is monomer 2 that is going to appear or vice-versa
        if (this.containsKey(a.getMon1()))
        {
            Monomer toIns = new Monomer(a.getMon2(), a.getRule().getS2p());
            if (!addMonomer(toIns))
                return false;

            // set monomer 1's state to whatever is in the rule
            // there might have been a change of state for monomer 1
            Monomer one = this.get(a.getMon1());
            one.setState(a.getRule().getS1p());
        }
        else
        {
            Monomer toIns = new Monomer(a.getMon1(), a.getRule().getS1p());
            if (!addMonomer(toIns))
                return false;

            // set monomer 2's state to whatever is in the rule
            // there might have been a change of state for monomer 2
            Monomer two = this.get(a.getMon2());
            two.setState(a.getRule().getS2p());
        }

        // adjust bond types
        adjustBonds(a.getMon1(), a.getMon2(), a.getRule().getBondp());

        return true;
    }

    // action that consists of deleting a monomer
    private boolean performDeletion(Action a)
    {
        // if monomer 1 wasn't empty then becomes empty, then it must have gotten deleted
        if (!a.getRule().getS1().equals("empty") && a.getRule().getS1p().equals("empty"))
        {
            destroyMonomer(a.getMon1());

            // if monomer 2 is not empty, there might have been a state change for it
            if (!a.getRule().getS2p().equals("empty"))
            {
                Monomer tmp = this.get(a.getMon2());
                tmp.setState(a.getRule().getS2p());
            }
        }

        // if monomer 2 wasn't empty then becomes empty, then it must have gotten deleted
        if (!a.getRule().getS2().equals("empty") && a.getRule().getS2p().equals("empty"))
        {
            destroyMonomer(a.getMon2());

            // if monomer 1 is not empty, there might have been a state change for it
            if (!a.getRule().getS1p().equals("empty"))
            {
                Monomer tmp = this.get(a.getMon1());
                tmp.setState(a.getRule().getS1p());
            }
        }
        return true;
    }

    //================================================================================
    // Helpers
    //================================================================================

    // adjusts bonds between two monomers, assumes that you are passing existing monomers
    private void adjustBonds(Point a, Point b, byte c)
    {
        Monomer one = this.get(a);
        Monomer two = this.get(b);

        one.adjustBond(Direction.dirFromPoints(a, b), c);
        two.adjustBond(Direction.dirFromPoints(b, a), c);
    }

    private void destroyMonomer(Point p)
    {
        Monomer one = this.get(p);

        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (i == j)
                {
                    // do nothing
                }
                else
                {
                    Point neighborPoint = new Point(one.getLocation().x + i, one.getLocation().y + j);

                    // check if there is a monomer in that neighboring position
                    if (this.containsKey(neighborPoint))
                    {
                        Monomer two = this.get(neighborPoint);

                        // check if there is a bond between this monomer and its neighbor, if so, break it
                        if (one.getBondTypeByDir(Direction.dirFromPoints(one.getLocation(), neighborPoint)) != Bond.TYPE_NONE)
                        {
                            two.adjustBond(Direction.dirFromPoints(two.getLocation(), one.getLocation()), Bond.TYPE_NONE);
                        }
                    }
                }
            }
        }
    }
}