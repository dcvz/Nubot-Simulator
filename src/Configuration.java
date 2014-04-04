// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class Configuration extends HashMap<Point, Monomer>
{
    public RuleSet rules;
    public boolean isFinished;
    public double timeElapsed;
    public int numberOfActions;
    private Random rand = new Random();


    //================================================================================
    // For saving Config
    //================================================================================


    Runnable recordRunnable;
    String recordLocation = ".";
    ExecutorService executorService =  Executors.newFixedThreadPool(4);
    private ArrayList<Pair<Double, HashMap<Point, Monomer>>> recordArrayList;

    //================================================================================
    // Constructors
    //================================================================================

    public Configuration() {
        rules = new RuleSet();
        isFinished = false;
        timeElapsed = 0.0;
        numberOfActions = 0;
        recordRunnable = new Runnable() {
            @Override
            public void run() {


            }
        };

        recordArrayList = new ArrayList<Pair<Double, HashMap<Point, Monomer>>>();

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
        else
        {
            System.out.println("There is already a monomer at that location!");
            return false;
        }
    }

    public void executeFrame()
    {
        ActionSet actions = computeActionSet();
        AgtionSet agtions = new AgtionSet();
        Action selectedAc;
        Agtion selectedAg;
        double frametime = 0;

        if (Simulation.agitationON)
            agtions = computeAgtionSet();

        numberOfActions = actions.size() + agtions.size();

        System.out.printf("Number of actions: %d", numberOfActions);

        if (numberOfActions > 0)
        {
            if (Simulation.agitationON)
            {
                double percent = (double)actions.size() / (double)(actions.size() + agtions.size());
                double pick = rand.nextDouble();

                if (pick <= percent)
                {
                    do
                    {
                        if (actions.size() + agtions.size() < 1)
                        {
                            isFinished = true;
                            Simulation.isRunning = false;
                            break;
                        }
                        selectedAc = actions.selectArbitrary();
                    } while (!executeAction(selectedAc));
                }
                else
                {
                    do
                    {
                        if (actions.size() + agtions.size() < 1)
                        {
                            isFinished = true;
                            Simulation.isRunning = false;
                            break;
                        }
                        selectedAg = agtions.selectArbitrary();
                    } while (!executeAgtion(selectedAg));
                }
            }
            else
            {
                do
                {
                    if (actions.size() < 1)
                    {
                        isFinished = true;
                        Simulation.isRunning = false;
                        break;
                    }
                    selectedAc = actions.selectArbitrary();
                } while (!executeAction(selectedAc));
                frametime = Simulation.calculateExpDistribution(numberOfActions + 1);
                timeElapsed = frametime;
            }
        }
        else
        {
            isFinished = true;
            Simulation.isRunning = false;
        }

        recordArrayList.add(Pair.with(frametime, (HashMap<Point,Monomer>)this));

    }

    // given a ruleset, compute a list of all possible actions
    // that can be executed in our current configuration
    private ActionSet computeActionSet()
    {
        ActionSet ret = new ActionSet();
        for (Monomer m : this.values())
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; j++)
                {
                    if (i == j) { /* do nothing */ }
                    else
                    {
                        Point neighborPoint = new Point(m.getLocation().x + i, m.getLocation().y + j);
                        Quartet<String, String, Byte, Byte> key;

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

    private AgtionSet computeAgtionSet()
    {
        AgtionSet ret = new AgtionSet();

        for (Monomer m : this.values())
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; j++)
                {
                    if (i == j) { /* do nothing */ }
                    else
                    {
                        ret.add(new Agtion(m.getLocation(), Direction.dirFromPoints(m.getLocation(), new Point(m.getLocation().x + i, m.getLocation().y + j))));
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
        else if (a.getRule().getClassification() == Rule.RuleType.BOTH)
            return performBoth(a);
        else
            return performMovement(a);
    }

    private boolean executeAgtion(Agtion a)
    {
        Monomer source = this.get(a.getMon());

        Configuration movableSet = new Configuration();
        movableSet.addMonomer(source);

        greedyExpand(movableSet, a.getDir());
        shift(movableSet, a.getDir());

        return true;
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
        if (!a.getRule().getBond().equals(a.getRule().getBondp()))
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

    private boolean performBoth(Action a)
    {
        // if monomer 1 exists then it is monomer 2 that is going to appear or vice-versa
        if (this.containsKey(a.getMon1()))
        {
            destroyMonomer(a.getMon1());
            addMonomer(new Monomer(a.getMon2(), a.getRule().getS2p()));
        }
        else
        {
            destroyMonomer(a.getMon2());
            addMonomer(new Monomer(a.getMon1(), a.getRule().getS1p()));
        }

        return true;
    }

    private boolean performMovement(Action a)
    {
        Monomer one = this.get(a.getMon1());
        Monomer two = this.get(a.getMon2());
        double coinFlip = rand.nextDouble();

        Configuration movableSet = new Configuration();

        // in movement you must arbitrarily select who will be the base and
        // who will be the arm, so we simulate a coin toss to select which
        if (coinFlip < 0.5)
            movableSet.addMonomer(two);
        else
            movableSet.addMonomer(one);

        adjustBonds(one.getLocation(), two.getLocation(), Bond.TYPE_NONE);

        if (coinFlip < 0.5)
        {
            greedyExpand(movableSet, Direction.deltaFromDirs(a.getRule().getDir(), a.getRule().getDirp()));

            if (movableSet.containsValue(one))
            {
                System.out.println("Hai <");
                adjustBonds(one.getLocation(), two.getLocation(), a.getRule().getBond());
                return false;
            }
            else
                shift(movableSet, Direction.deltaFromDirs(a.getRule().getDir(), a.getRule().getDirp()));
        }
        else
        {
            greedyExpand(movableSet, Direction.deltaFromDirs(a.getRule().getDirp(), a.getRule().getDir()));

            if (movableSet.containsValue(two))
            {
                System.out.println("Hai >");
                adjustBonds(one.getLocation(), two.getLocation(), a.getRule().getBond());
                return false;
            }
            else
                shift(movableSet, Direction.deltaFromDirs(a.getRule().getDirp(), a.getRule().getDir()));
        }

        one.setState(a.getRule().getS1p());
        two.setState(a.getRule().getS2p());
        adjustBonds(one.getLocation(), two.getLocation(), a.getRule().getBondp());

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
                if (i == j) { /* do nothing */ }
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
                            // adjust bond types
                            two.adjustBond(Direction.dirFromPoints(two.getLocation(), one.getLocation()), Bond.TYPE_NONE);
                        }
                    }
                }
            }
        }

        this.remove(one.getLocation());
    }

    // will keep expanding the movable set until nothing else can be added
    private void greedyExpand(Configuration movableSet, byte dir)
    {
        int increase;
        do
        {
            increase = expandMovableSet(movableSet, dir);
        } while(increase > 0);
    }

    //
    private int expandMovableSet(Configuration movableSet, byte dir)
    {
        int initalSize = movableSet.size();
        HashMap<Point, Monomer> conflictMap = new HashMap<Point, Monomer>();

        for (Monomer m : movableSet.values())
        {
            addConflicts(m, dir, conflictMap);
        }

        movableSet.merge(conflictMap);

        return movableSet.size() - initalSize;
    }

    private void addConflicts(Monomer m, byte dir, HashMap<Point,Monomer> conflictMap)
    {
        //loop through 6 neighbor positions
        for(int i = -1; i <= 1; i++)
        {
            for(int j = -1; j <= 1; j++)
            {
                if (i == j) { /* do nothing */ }
                else
                {
                    Point neighborPoint = new Point(m.getLocation().x + i, m.getLocation().y + j);

                    if (this.containsKey(neighborPoint))
                    {
                        Monomer tmp = this.get(neighborPoint);

                        if (m.conflicts(tmp, dir))
                            conflictMap.put(neighborPoint, tmp);
                    }
                }
            }
        }
    }

    public void merge(HashMap<Point,Monomer> incoming)
    {
        for(Monomer m : incoming.values())
        {
            put(m.getLocation(), m);
        }
    }

    private void shift(Configuration movableSet, byte dir)
    {
        for( Monomer m : movableSet.values() )
        {
            adjustFlexibleBonds(m, dir, movableSet);
        }

        for( Monomer m : movableSet.values() )
        {
            remove(m.getLocation());
            m.shift(dir);
        }

        for( Monomer m : movableSet.values() )
            put(m.getLocation(), m);
    }

    private void adjustFlexibleBonds(Monomer m, byte dir, Configuration movableSet)
    {
        HashMap<Byte, Byte> buffer = new HashMap<Byte, Byte>();

        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (i == j) { /* do nothing */ }
                else
                {
                    if (!movableSet.containsKey(new Point(m.getLocation().x + i, m.getLocation().y +j)) && this.containsKey(new Point(m.getLocation().x + i, m.getLocation().y +j)))
                    {
                        if (m.getBondTypeByDir(Direction.pointOffsetToDirByte(new Point(i, j))) == Bond.TYPE_FLEXIBLE)
                        {
                            Monomer w = get(new Point(m.getLocation().x + i, m.getLocation().y + j));
                            m.adjustFlexibleBond(this.get(new Point(m.getLocation().x + i, m.getLocation().y + j)), dir, buffer);
                        }
                    }
                }
            }
        }

        for (Map.Entry<Byte, Byte> entry : buffer.entrySet())
        {
            Byte key = entry.getKey();
            Byte value = entry.getValue();

            if (value == Bond.TYPE_FLEXIBLE)
                m.adjustBond(key, Bond.TYPE_FLEXIBLE);
        }
    }

    public void saveConfig(String saveLocation)
    {

    }

    public void beginRecording()
    {


        ///loop here based on flag for finish called by the GUI or when simulation has finished


        //reading from


    }


}