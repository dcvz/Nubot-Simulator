// Display.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Pair;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Display implements ActionListener, ComponentListener, MouseWheelListener, MouseMotionListener, MouseListener
{
    int fontSize = 20;
    Timer timer;
    final JFrame mainFrame = new JFrame("Nubot Simulator");
    final JFrame aboutF = new JFrame("A.S.A.R.G");
    JComponent canvas;
    //Config and rules
    Configuration map;

    //GRaphics

    float canvasStrokeSize = 2.0f;

    //Menus
    private JMenu file = new JMenu("File");
    private JMenu simulation = new JMenu("Simulation");
    private JMenu settings = new JMenu("Settings");
    private JMenu help = new JMenu("Help");
    private JMenu agitationMenu = new JMenu("Agitation");
    private JMenu speedMenu = new JMenu("Speed");
    // create sub-menus for each menu
    private JMenuItem loadR = new JMenuItem("Load Rules");
    private JMenuItem about = new JMenuItem("About");
    private JMenuItem loadC = new JMenuItem("Load Configuration");
    private JMenuItem menuClear = new JMenuItem("Clear");
    private JMenuItem menuQuit = new JMenuItem("Quit");
    private JMenuItem simStart = new JMenuItem("Start");
    private JMenuItem simStop = new JMenuItem("Stop");
    private JMenuItem simPause = new JMenuItem("Pause");
    private JMenuItem record = new JMenuItem("Record");
    private JCheckBoxMenuItem agitationToggle = new JCheckBoxMenuItem("On");
    private JMenuItem agitationSetRate = new JMenuItem("Set Rate");

    //Status bar

    JPanel statusBar = new JPanel();
    JLabel statusSimulation = new JLabel();
    JLabel statusRules = new JLabel();
    JLabel statusConfig = new JLabel();
    JLabel statusAgitation = new JLabel();
    JLabel statusSpeed = new JLabel();
    JLabel statusMonomerNumber = new JLabel();
    JLabel statusTime = new JLabel();
    //Data
    String rulesFileName = "";
    String configFileName = "";

    //change to default starting value later
    double speedRate= 1;
    int speedMax = 10;
    Double totalTime=0.0;

    //Threads
    Thread simHeartBeat;
    Runnable simRunnable;

    //For panning
    Point lastXY;

    public Display(Dimension size)
    {
        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(size.width, size.height);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        Simulation.canvasXYoffset = new Point(size.width/2, -1 * (size.height/2 - 60));

        initMenuBar();
        //post creation menubar setup
        simStart.setEnabled(false);
        simPause.setEnabled(false);
        simStop.setEnabled(false);
        record.setEnabled(false);

        map = new Configuration();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initCanvas();
        mainFrame.add(canvas);
        System.out.println(canvas.getSize());
        mainFrame.setVisible(true);
        mainFrame.addComponentListener(this);
        canvas.addMouseWheelListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(this);
        canvasStrokeSize = Simulation.monomerRadius/3;
        //for the nubot graphics/image & visuals



        ////
        //Status Bar  setup
        ////
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.setPreferredSize(new Dimension(mainFrame.getWidth(), 25));
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        mainFrame.add(statusBar, BorderLayout.SOUTH);
        statusSimulation.setText("Waiting on Files ");
        statusRules.setText("No Rules ");
        statusConfig.setText("No config ");
        statusAgitation.setText("Agitation off ");
        statusSpeed.setText("Speed: "+ speedRate);
        statusTime.setText("Time: " + totalTime);
        statusMonomerNumber.setText("Monomers: "+map.getSize());

        JSeparator statusSeparator1 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator1.setMaximumSize(new Dimension(5,25));
        JSeparator statusSeparator2 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator2.setMaximumSize(new Dimension(5,25));
        JSeparator statusSeparator3 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator3.setMaximumSize(new Dimension(5,25));
        JSeparator statusSeparator4 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator4.setMaximumSize(new Dimension(5,25));
        JSeparator statusSeparator5 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator5.setMaximumSize(new Dimension(5,25));
        JSeparator statusSeparator6 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator6.setMaximumSize(new Dimension(5,25));

        statusBar.add(statusSimulation);
        statusBar.add(statusSeparator1);
        statusBar.add(statusRules);
        statusBar.add(statusSeparator2);
        statusBar.add(statusConfig);
        statusBar.add(statusSeparator3);
        statusBar.add(statusAgitation);
        statusBar.add(statusSeparator4);
        statusBar.add(statusSpeed);
        statusBar.add(statusSeparator5);
        statusBar.add(statusMonomerNumber);
        statusBar.add(statusSeparator6);
        statusBar.add(statusTime);

        //******************

        //////
        ///Threads & Timer
        /////
        timer = new Timer(1000/60 , new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //  canvas.repaint();
            }
        });
        timer.setRepeats(true);

        simRunnable = new Runnable() {
            @Override
            public void run() {
                while(Simulation.isRunning)
                {
                    try
                    {
                        if(!Simulation.isRecording)
                        Thread.sleep((long) (speedRate*1000.0*map.executeTime));

                        map.executeFrame();
                        if(Simulation.animate)
                        {
                            Pair<Point,Point>  minMaxPair = Simulation.calculateMinMax(new ArrayList<Monomer>(map.values()), Simulation.monomerRadius, Simulation.canvasXYoffset, canvas.getSize());
                            Point minXY = minMaxPair.getValue0();
                            Point maxXY = minMaxPair.getValue1();
                            Dimension nubotDim = Simulation.calculateNubotDimension(new ArrayList<Monomer>(map.values()), Simulation.monomerRadius, Simulation.canvasXYoffset, canvas.getSize());
                            if(nubotDim.width < canvas.getWidth()  )
                            {
                            if(minXY.x < 0)
                                Simulation.canvasXYoffset.translate(-1*minXY.x,0);
                            if(maxXY.x + Simulation.monomerRadius*2 > canvas.getWidth());
                                Simulation.canvasXYoffset.translate(canvas.getWidth() - (maxXY.x + Simulation.monomerRadius*2), 0);
                            }
                            if(nubotDim.height < canvas.getHeight())
                            {
                            if(minXY.y < 0)
                               Simulation.canvasXYoffset.translate(0, minXY.y);
                            if(maxXY.y > canvas.getHeight())
                                Simulation.canvasXYoffset.translate(0, (maxXY.y + Simulation.monomerRadius*2) - canvas.getHeight());
                            }


                            canvas.repaint();
                        }
                        statusSimulation.setText("Simulating...");
                        totalTime+= map.executeTime;
                        statusMonomerNumber.setText("Monomers: "+map.getSize());
                        statusTime.setText("Time: "+totalTime);
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                statusSimulation.setText("Simulation finished ");
                if(map.isFinished)
                    JOptionPane.showMessageDialog(canvas, "No more rules can be applied!", "Finished", JOptionPane.OK_OPTION);
            }
        };
    }

    public void initCanvas()
    {
        canvas = new JComponent() {


            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawMonomers(g);
            }
        };
        canvas.setSize(mainFrame.getSize());
    }

    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        loadR.addActionListener(this);
        about.addActionListener(this);
        loadC.addActionListener(this);
        menuClear.addActionListener(this);
        menuQuit.addActionListener(this);
        simPause.addActionListener(this);
        simStart.addActionListener(this);
        simStop.addActionListener(this);
        record.addActionListener(this);
        agitationSetRate.addActionListener(this);
        agitationToggle.addActionListener(this);

        menuBar.add(file);
        menuBar.add(simulation);
        menuBar.add(settings);
        menuBar.add(help);
        help.add(about);
        file.add(loadR);
        file.add(loadC);
        file.add(new JSeparator(SwingConstants.HORIZONTAL));
        file.add(menuClear);
        file.add(menuQuit);
        simulation.add(simStart);
        simulation.add(record);
        simulation.add(simPause);
        simulation.add(new JSeparator(SwingConstants.HORIZONTAL));
        simulation.add(simStop);
        settings.add(agitationMenu);
        agitationMenu.add(agitationToggle);
        agitationMenu.add(agitationSetRate);
        settings.add(speedMenu);

        mainFrame.setJMenuBar(menuBar);
        // speed Slider
        JSlider speedSlider = new JSlider(JSlider.VERTICAL,-speedMax,speedMax,0);
        speedSlider.setMajorTickSpacing(20);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        Hashtable speedLabels = new Hashtable();
        speedLabels.put(-speedMax, new JLabel("Slow"));
        speedLabels.put(0, new JLabel("Normal"));
        speedLabels.put(speedMax, new JLabel("Fast"));
        speedSlider.setInverted(true);
        speedSlider.setLabelTable(speedLabels);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JSlider sliderSource = (JSlider) changeEvent.getSource();
                if (!sliderSource.getValueIsAdjusting()) {
                    speedRate = (double)sliderSource.getValue();
                    if (speedRate == 0)
                        speedRate = 1;
                    else if (speedRate < 1) {
                        speedRate = (speedMax +speedRate +1)/10;
                    }
                    System.out.println("speed changed to: "+speedRate);
                    statusSpeed.setText("Speed: "+speedRate);
                }
            }
        });

        speedMenu.add(speedSlider);
        //about screen
        final JPanel aboutP = new JPanel();
        aboutF.setResizable(false);
        aboutP.setLayout(new BoxLayout(aboutP, BoxLayout.PAGE_AXIS));
        aboutF.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JLabel aboutGroupName = new JLabel("Algorithmic Self-Assembly Research Group");
        JLabel aboutSchool = new JLabel("The University of Texas - Pan American");
        aboutGroupName.setFont(new Font("",Font.BOLD, 23));
        aboutSchool.setFont(new Font("", Font.BOLD, 20));
        aboutGroupName.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutSchool.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel groupLink = new JLabel("Visit Our Website");
        groupLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        groupLink.setForeground(Color.blue);

        aboutP.add(aboutGroupName);
        aboutP.add(Box.createRigidArea(new Dimension(0, 15)));
        aboutP.add(aboutSchool);
        aboutP.add(Box.createRigidArea(new Dimension(0, 30)));
        aboutP.add(groupLink);
        final URI websiteLink;
        MouseAdapter openURL = null;

        try
        {
            websiteLink = new URI("http://faculty.utpa.edu/orgs/asarg/");
            openURL = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    try
                    {
                        Desktop.getDesktop().browse(websiteLink);
                    }
                    catch (IOException e1)
                    {
                        System.out.println("error visiting the website URL");
                    }
                }
            };
        }
        catch (URISyntaxException e)
        {
            System.out.println("something is wrong with the URI or MouseAdapter");
        }
        groupLink.addMouseListener(openURL);

        aboutP.setBorder(new EmptyBorder(20, 20, 10, 20));
        aboutF.add(aboutP);
        aboutF.pack();
        aboutF.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loadR)
        {
            map.timeElapsed=0;
            map.rules.clear();
            try
            {
                final JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(new File("."));

                jfc.setDialogTitle("Select Rules File");
                // Creating a file filter for .conf
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f)
                    {
                        if (f.isDirectory())
                            return true;
                        String fname = f.getName();
                        if (fname.length() > 6 && fname.substring(fname.length() - 6, fname.length()).matches(".rules"))
                            return true;

                        return false;
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Nubot Rules File - .rules";
                        //return null;
                    }
                });

                int resVal = jfc.showOpenDialog(mainFrame);

                // if the ret flag results as Approve, we parse the file
                if (resVal == JFileChooser.APPROVE_OPTION)
                {
                    statusRules.setText("Loading rules ");
                    File theFile = jfc.getSelectedFile();
                    //if the selected file is of the right extension
                    if (theFile.length() > 5 && theFile.getName().substring(theFile.getName().length() - 6, theFile.getName().length()).matches(".rules"))
                    {
                        map.rules.clear();
                        rulesFileName = theFile.getName();
                        FileReader fre = new FileReader(theFile);
                        BufferedReader bre = new BufferedReader(fre);
                        boolean cont = true;

                        while (cont)
                        {
                            String line = bre.readLine();

                            if (line == null)
                                cont = false;

                            //if it's not a comment line and not empty, we parse
                            if (line != null && !line.contains("[") && !line.isEmpty() && line != "")
                            {
                                String[] splitted = line.split(" ");
                                map.rules.addRule(new Rule(splitted[0], splitted[1], (byte) Integer.parseInt(splitted[2]), Direction.stringToFlag(splitted[3]), splitted[4], splitted[5], (byte) Integer.parseInt(splitted[6]), Direction.stringToFlag(splitted[7])));
                            }
                        }

                        bre.close();

                        Simulation.rulesLoaded = true;
                        if (Simulation.debugMode)
                            System.out.println("We have " + map.rules.size() + " rules");

                        statusRules.setText("Rules loaded ");

                        if (Simulation.rulesLoaded && Simulation.configLoaded)
                        {
                            simStart.setEnabled(true);
                            record.setEnabled(true);
                            statusSimulation.setText("Ready to Start ");
                        }
                    }

                    System.out.println(map.rules.values());
                }
            }
            catch (Exception exc)
            {

            }

            System.out.println("Load Rules");
        }
        else if (e.getSource() == loadC)
        {
            map.timeElapsed = 0;
            map.clear();
            try
            {
                final JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(new File("."));

                jfc.setDialogTitle("Select Configuration File");
                // Creating a file filter for .conf
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f)
                    {
                        if (f.isDirectory())
                            return true;
                        String fname = f.getName();
                        if (fname.length() > 5 && fname.substring(fname.length() - 5, fname.length()).matches(".conf"))
                            return true;

                        return false;
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Nubot Configuration File - .conf";
                        //return null;
                    }
                });

                int resVal = jfc.showOpenDialog(mainFrame);

                // if the ret flag results as Approve, we parse the file
                if (resVal == JFileChooser.APPROVE_OPTION)
                {
                    File theFile = jfc.getSelectedFile();
                    //if the selected file is of the right extension
                    if (theFile.length() > 5 && theFile.getName().substring(theFile.getName().length() - 5, theFile.getName().length()).matches(".conf"))
                    {
                        configFileName = theFile.getName();
                        map.clear();
                        boolean inBonds = false;
                        FileReader fre = new FileReader(theFile);
                        BufferedReader bre = new BufferedReader(fre);
                        boolean cont = true;

                        while (cont)
                        {
                            String line = bre.readLine();
                            if (line == null)
                                cont = false;
                            //if it's not a comment line and not empty, we parse
                            if (line != null && !line.contains("[") && !line.isEmpty() && !(line == ""))
                            {
                                if (!inBonds)
                                {
                                    if (line.contains("States:"))
                                    {

                                    }
                                    else if (line.contains("Bonds:"))
                                    {
                                        inBonds = true;
                                    }
                                    else
                                    {
                                        String[] splitted = line.split(" ");
                                        map.addMonomer(new Monomer(new Point(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1])), splitted[2]));
                                    }
                                }
                                else if (inBonds)
                                {
                                    String[] splitted = line.split(" ");
                                    // map.adjustBond(,);
                                    Point monomerPoint1 = new Point(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
                                    Point monomerPoint2 = new Point(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));
                                    byte bondType = (byte) Integer.parseInt(splitted[4]);
                                    if(map.containsKey(monomerPoint1) && map.containsKey(monomerPoint2) && Direction.dirFromPoints(monomerPoint1, monomerPoint2) > 0)
                                    {
                                        map.get(monomerPoint1).adjustBond(Direction.dirFromPoints(monomerPoint1, monomerPoint2), bondType);
                                        map.get(monomerPoint2).adjustBond(Direction.dirFromPoints(monomerPoint2, monomerPoint1), bondType);
                                    }
                                }
                                else if (Simulation.debugMode)
                                    System.out.println("We don't have more sections.");
                            }
                        }

                        bre.close();
                        Simulation.configLoaded = true;

                        statusConfig.setText("Config loaded ");
                        statusMonomerNumber.setText("Monomers: "+map.getSize());


                        canvas.repaint();
                        if (Simulation.configLoaded && (Simulation.rulesLoaded || Simulation.agitationON))
                        {

                            simStart.setEnabled(true);
                            record.setEnabled(true);
                            statusSimulation.setText("Ready to Start");
                        }
                    }
                }
            }
            catch (Exception exc)
            {
            }
            System.out.println("Load config");
        }
        else if (e.getSource() == menuClear)
        {
            clearGraphics();
            canvas.repaint();
            map.clear();
            map.rules.clear();
            map.timeElapsed = 0;
            /////Simulation Flags
            Simulation.configLoaded = false;
            Simulation.rulesLoaded = false;
            Simulation.isRunning = false;
            Simulation.agitationON = false;

            ///// Statusbar Text
            statusSimulation.setText("Waiting on Files ");
            statusRules.setText("No Rules ");
            statusConfig.setText("No config ");
            statusAgitation.setText("Agitation off ");
            totalTime = 0.0;
            statusMonomerNumber.setText("Monomers: 0");

            simHeartBeat.interrupt();
            simStart.setEnabled(false);
            simPause.setEnabled(false);
            simStop.setEnabled(false);
            loadC.setEnabled(true);
            loadR.setEnabled(true);
            System.out.println("clear ");
        }
        else if (e.getSource() == menuQuit)
        {
            System.out.println("quit application");
            System.exit(0);
        }
        else if (e.getSource() == about)
        {
            System.out.println("about this application");
            aboutF.setVisible(true);
        }
        else if (e.getSource() == simStart)
        {

            Simulation.animate = true;
            Simulation.isRunning = true;
            map.isFinished=false;
            Simulation.isPaused = false;
            loadC.setEnabled(false);
            loadR.setEnabled(false);
            simPause.setEnabled(true);
            simStop.setEnabled(true);
            simHeartBeat = new Thread(simRunnable);
            simHeartBeat.start();
           // timer.start();

            System.out.println("start");
        }
        else if (e.getSource() == simStop)
        {
            Simulation.isRunning = false;
            //timer.stop();
            map.timeElapsed = 0;
            simHeartBeat.interrupt();
            System.out.println("stop");
        }
        else if (e.getSource() == simPause)
        {
            timer.stop();
            Simulation.isRunning = false;
            Simulation.isPaused = true;

            System.out.println("pause");
        }
        else if (e.getSource() == agitationToggle)
        {
            if (Simulation.agitationRate == 0.0)
            {
                JOptionPane.showMessageDialog(mainFrame,"Please set the agitation rate", "Error",JOptionPane.ERROR_MESSAGE);
                agitationToggle.setState(false);
            } else
            {
                Simulation.agitationON = agitationToggle.getState();
                if (Simulation.agitationON == true)
                    statusAgitation.setText("Agitation On: "+Simulation.agitationRate);
                else
                    statusAgitation.setText("Agitation Off ");
                System.out.println("Agitation is: "+Simulation.agitationON+' '+ Simulation.agitationRate);
            }
        }
        else if (e.getSource() == agitationSetRate)
        {
            String agitationRateString = JOptionPane.showInputDialog(mainFrame,"Set Agitation Rate","Agitation",JOptionPane.PLAIN_MESSAGE);
            if (agitationRateString != null)
            {
                Simulation.agitationRate = Double.parseDouble(agitationRateString);
                Simulation.agitationON = true;
                statusAgitation.setText("Agitation On: "+Simulation.agitationRate);
                agitationToggle.setState(true);
                System.out.println("Agitation Rate changed and set to on");

                if (Simulation.configLoaded && Simulation.agitationON)
                {
                    simStart.setEnabled(true);
                    statusSimulation.setText("Ready to Start");
                }
            }
        }
        else if (e.getSource() == record)
        {

            String input = JOptionPane.showInputDialog(mainFrame, "Recording length(Nubot Time):");
            double recordingLength = Double.parseDouble(input);
            if(recordingLength > 0)
            {
                Simulation.animate = false;
                Simulation.recordingLength = recordingLength;
                Simulation.isRecording = true;
                map.timeElapsed = 0;
                map.initRecord();
                Simulation.isRunning = true;
                map.isFinished=false;
                Simulation.isPaused = false;
                simStop.setEnabled(true);
                simHeartBeat = new Thread(simRunnable);
                simHeartBeat.start();
                statusSimulation.setText("Recording.");
                JOptionPane.showMessageDialog(mainFrame, "The simulation is recording and will not be animated.");


            }
            System.out.println("record button started");
        }
    }

    public synchronized void drawMonomer(Monomer m, Graphics2D g)
    {

        Point xyPos = Simulation.getCanvasPosition(m.getLocation());

        int monomerWidthAdjustment = Simulation.monomerRadius / 4;
        int monomerWidth = Simulation.monomerRadius * 2 - monomerWidthAdjustment;
        int monomerHeight = Simulation.monomerRadius * 2;
        g.setColor(Color.WHITE);
        g.fillOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setStroke(new BasicStroke(Simulation.monomerRadius / 10));
        g.setColor(new Color(2,180,206));
        g.drawOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setColor(Color.white);
        Rectangle2D bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        while (bounds.getWidth() < monomerWidth -4 && bounds.getHeight() < monomerHeight - 4)
        {
            g.setFont(g.getFont().deriveFont((float) ++fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        while (bounds.getWidth() > monomerWidth -4 || bounds.getHeight() > monomerHeight -4)
        {
            g.setFont(g.getFont().deriveFont((float) --fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        g.setColor(Color.BLACK);
        g.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + Simulation.monomerRadius - (int) bounds.getWidth() / 2 - monomerWidthAdjustment/2,
                /*Y Coord */    xyPos.y + Simulation.monomerRadius + (int) (bounds.getHeight() / 3.5));
    }

    public synchronized void drawMonomers(Graphics g)
    {

        Monomer[] mapTemp = new Monomer[map.getSize()];
        map.values().toArray(mapTemp);

        ArrayList<Monomer> tempMonList = new ArrayList<Monomer>();
        for (Monomer m: mapTemp)
        {
            drawBond(m,(Graphics2D)g);

            tempMonList.add(new Monomer(m));
        }
        for (Monomer m : tempMonList)
        {

            drawMonomer(m, (Graphics2D)g);
        }


    }


    public  synchronized void drawBond(Monomer m, Graphics2D g) {

        Monomer tempMon = new Monomer(m);
        if (tempMon.hasBonds())
        {
            ArrayList<Byte> rigidDirList =    tempMon.getDirsByBondType(Bond.TYPE_RIGID);
            ArrayList<Byte> flexibleDirList =   tempMon.getDirsByBondType(Bond.TYPE_FLEXIBLE);

            g.setColor(Color.RED);
            for (Byte dir : rigidDirList)
            {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius/3.5f, start.y, end.x , end.y));
            }

            for (Byte dir : flexibleDirList)
            {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize *1.20f));
                g.setColor(Color.RED);
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius/3.5f, start.y, end.x , end.y));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(canvasStrokeSize * .80f));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius/3.5f, start.y, end.x , end.y));
            }
        }
    }


    private void clearGraphics()
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, mainFrame.getSize().width, mainFrame.getSize().height);
        g.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    public void componentResized(ComponentEvent e)
    {

    }

    @Override
    public void componentHidden(ComponentEvent e)
    {

    }

    @Override
    public void componentMoved(ComponentEvent e)
    {

    }

    @Override
    public void componentShown(ComponentEvent e)
    {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.getWheelRotation() == 1.0)
        {
            Simulation.monomerRadius = (int)Math.ceil(Simulation.monomerRadius * .92);
            canvasStrokeSize = Simulation.monomerRadius/3;

            //  if(!Simulation.isRunning)
            canvas.repaint();
        }

        else
        {
            Simulation.monomerRadius = (int)Math.ceil(Simulation.monomerRadius * 1.08);
            canvasStrokeSize = Simulation.monomerRadius/3;
            //if(!Simulation.isRunning)
            canvas.repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(lastXY==null)
            lastXY = e.getPoint();
        Simulation.canvasXYoffset.translate(e.getX()  - lastXY.x, -(e.getY()  - lastXY.y) );

        lastXY = e.getPoint();
        //if(!Simulation.isRunning)
        canvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        lastXY = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }
}