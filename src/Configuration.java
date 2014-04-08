// Configuration.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.*;
import org.monte.media.Format;
import org.monte.media.avi.AVIWriter;
import org.monte.media.avi.AbstractAVIStream;
import org.monte.media.quicktime.QuickTimeWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

public class Configuration extends HashMap<Point, Monomer>
{
    public RuleSet rules;
    public boolean isFinished;
    public double timeElapsed;
    public int numberOfActions;
    private Random rand = new Random();
    public int getSize(){
        return this.size();
    }
    public double executeTime = 1.0;


    //================================================================================
    // For saving Config
    //================================================================================


    Runnable recordRunnable;
    String recordLocation = ".";
    // ExecutorService executorService =  Executors.newFixedThreadPool(4);
    private ArrayList<Pair<Double, ArrayList<Monomer>>> recordFrameHistory;

    //================================================================================
    // Constructors
    //================================================================================

    public Configuration() {
        rules = new RuleSet();
        isFinished = false;
        timeElapsed = 0.0;
        numberOfActions = 0;
        initRecord();


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
        Action selectedAc = null;
        Agtion selectedAg;
        double frametime = 0;

        if (Simulation.agitationON)
            agtions = computeAgtionSet();

        numberOfActions = actions.size() + agtions.size();

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
                        frametime = Simulation.calculateExpDistribution(numberOfActions + 1);
                        executeTime = frametime;
                        timeElapsed += frametime;
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
                    frametime = Simulation.calculateExpDistribution(numberOfActions + 1);
                    executeTime = frametime;
                    timeElapsed += frametime;
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
                executeTime = frametime;
                timeElapsed += frametime;
            }
        }
        else
        {
            isFinished = true;

            System.out.println("End Simulation.");

            Simulation.isRunning = false;
        }
        if(Simulation.isRecording) {
            ArrayList<Monomer> monList = new ArrayList<Monomer>();
            for(Monomer m : this.values())
            {
                monList.add(new Monomer(m));
            }

            recordFrameHistory.add(Pair.with(frametime, monList));
            if(timeElapsed > Simulation.recordingLength || isFinished)
            {
                saveRecord("dog.ser");
                saveVideo("dog.ser");
                Simulation.isRecording = false;
                Simulation.isRunning = false;
                Simulation.recordingLength = 0;
                isFinished = true;

            }




        }

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

    public void saveRecord(String saveLocation)
    {

        try{
            FileOutputStream fileOut = new FileOutputStream(saveLocation);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);


            try{
                objOut.writeObject(recordFrameHistory);

            }catch(Exception e)
            {
                System.out.println("Exception writing object.");
            }
            finally
            {
                fileOut.close();
                objOut.close();
            }



        }
        catch(Exception e)
        {
            System.out.println("Exception thrown writing history: " +  e.toString());
        }

    }
    public ArrayList<Pair<Double, ArrayList<Monomer>>> readRecord(String location)
    {
        try{
            FileInputStream fileIn = new FileInputStream(location);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            try{
                return (ArrayList<Pair<Double,ArrayList<Monomer>>>)objIn.readObject();

            }catch(Exception e)
            {
                System.out.println("Exception reading object");
            }
            finally
            {
                fileIn.close();
                objIn.close();
            }


        }catch(Exception e)
        {
            System.out.println("Exception thrown reading history: " + e.getMessage());

        }
        return null;
    }
    public void initRecord()
    {
        recordFrameHistory = new ArrayList<Pair<Double, ArrayList<Monomer>>>();
    }

    public void saveVideo(String recordLocation)
    {
        System.out.println("DFGDFG");
        ArrayList<Pair<Double, ArrayList<Monomer>>> record = readRecord(recordLocation);
        int frameCount = 0;
        boolean avi = false;


        try{
            QuickTimeWriter qtWr = new QuickTimeWriter(new File("Test.mov"));
            qtWr.addVideoTrack(QuickTimeWriter.VIDEO_PNG, 30, 800, 600);

            double timeElapsed = 0;

            for(Pair<Double, ArrayList<Monomer>> pba : record)
            {

                try {


                  //  System.out.println("out of if block "  + (frameCount*.033333 )  + " frame time "  + pba.getValue0()  +"frame# " +frameCount);


                   //System.out.println("inside of if block "  + (frameCount*.033333)  + " frame time "  + pba.getValue0() +"frame# " +frameCount);
                    File output = new File(recordLocation + ++frameCount + ".png");
                    BufferedImage tempBFI = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                    int  radius = 15;
                    Point offset = new Point(400, -300 );

                    Graphics2D g2 = (Graphics2D)tempBFI.getGraphics();
                    g2.setBackground(Color.white);
                    g2.setColor(Color.white);
                    g2.fillRect(0,0,800,600);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(Color.black);
                    g2.drawString("Time: "  + timeElapsed + " Frame #: "  + frameCount, 0, 20 );
                    radius = Simulation.caclulateProperRadiusMutateOffset(pba.getValue1(), radius, offset, new Dimension(800,600));

                    for(Monomer m : pba.getValue1())
                    {
                        drawBond(m, g2, radius, offset);

                    }
                    for(Monomer m : pba.getValue1())
                    {
                        drawMonomer(m, g2, radius, offset);

                    }

                    try{
                        long dur = 30*pba.getValue0() < 1 ? 2 - 1*(Math.round(29*pba.getValue0()) )  : (long)(30*pba.getValue0())  ;


                        qtWr.write(0, tempBFI, dur);
                      //  System.out.println("MovieTimeScale: " + qtWr.getMovieTimeScale() + " mediaTimeScale: " +qtWr.getMediaTimeScale(0) + " Record size: " + record.size() + " mediaDuration(): " + qtWr.getMediaDuration(0) + " MoveDuration: " + qtWr.getMovieDuration());
                        //ImageIO.write(tempBFI, "png", output);

                    }

                    catch(Exception e){

                        System.out.println(e.getMessage());

                    }
                    timeElapsed += pba.getValue0();

                }
                catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }

            } //For Pair<>
            qtWr.close();
        }
        catch(Exception e)
        {

        }

    }


    private void drawMonomer(Monomer m, Graphics2D g, int radius, Point offset)
    {

        Point xyPos = Simulation.getCanvasPosition(m.getLocation(),offset, radius);

        int fontSize = 15;
        int monomerWidthAdjustment = radius / 4;
        int monomerWidth = radius * 2 - monomerWidthAdjustment;
        int monomerHeight = radius * 2;
        g.setColor(Color.WHITE);
        g.fillOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setStroke(new BasicStroke(radius / 10));
        g.setColor(new Color(2,180,206));
        g.drawOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setColor(Color.white);
        Rectangle2D bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        while (bounds.getWidth() < monomerWidth -2 && bounds.getHeight() < monomerHeight - 2)
        {
            g.setFont(g.getFont().deriveFont((float) ++fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        while (bounds.getWidth() > monomerWidth -2 || bounds.getHeight() > monomerHeight -2)
        {
            g.setFont(g.getFont().deriveFont((float) --fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        g.setColor(Color.BLACK);
        g.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + radius - (int) bounds.getWidth() / 2 - monomerWidthAdjustment/2,
                /*Y Coord */    xyPos.y + radius + (int) (bounds.getHeight() / 3.5));
    }
    public  void drawBond(Monomer m, Graphics2D g, int radius, Point offset) {

        Monomer tempMon = new Monomer(m);
        if (tempMon.hasBonds())
        {
            ArrayList<Byte> rigidDirList =    tempMon.getDirsByBondType(Bond.TYPE_RIGID);
            ArrayList<Byte> flexibleDirList =   tempMon.getDirsByBondType(Bond.TYPE_FLEXIBLE);

            g.setColor(Color.RED);
            for (Byte dir : rigidDirList)
            {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation(), offset, radius);
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir), offset, radius);
                start.translate(radius, radius);
                end.translate(radius, radius);
                g.setStroke(new BasicStroke(radius/3));
                g.draw(new Line2D.Float(start.x - radius/3.5f, start.y, end.x , end.y));
            }

            for (Byte dir : flexibleDirList)
            {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation(), offset,radius);
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir), offset, radius);
                start.translate(radius, radius);
                end.translate(radius, radius);
                g.setStroke(new BasicStroke((radius/3) *1.20f));
                g.setColor(Color.RED);
                g.draw(new Line2D.Float(start.x - radius/3.5f, start.y, end.x , end.y));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke((radius/3) * .80f));
                g.draw(new Line2D.Float(start.x - radius/3.5f, start.y, end.x , end.y));
            }
        }
    }







}