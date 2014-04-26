// Display.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.monte.media.quicktime.QuickTimeWriter;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Display implements ActionListener, ComponentListener, MouseWheelListener, MouseMotionListener, MouseListener, KeyListener

{
    int fontSize = 20;
    Timer timer;
    final JFrame mainFrame = new JFrame("Nubot Simulator");
    final JFrame aboutF = new JFrame("A.S.A.R.G");
    JComponent canvas;
    //Config and rules
    Configuration map;

    //Graphics
    Monomer posLockMon;
    float canvasStrokeSize = 2.0f;

    //Menus
    private JMenu file = new JMenu("File");
    private JMenu simulation = new JMenu("Simulation");
    private JMenu settings = new JMenu("Settings");
    private JMenu help = new JMenu("Help");
    private JMenu agitationMenu = new JMenu("Agitation");
    private JMenu speedMenu = new JMenu("Speed");
    // create sub-menus for each menu
    private ButtonGroup bondGroup = new ButtonGroup();
    private ButtonGroup drawMode = new ButtonGroup();

    private JCheckBoxMenuItem editToggle = new JCheckBoxMenuItem("Edit Mode");
    private JRadioButton editBrush= new JRadioButton("Brush");
    private JRadioButton editEraser = new JRadioButton("Eraser");
    private JRadioButton editState = new JRadioButton("State");
    private JRadioButton single = new JRadioButton("Single");
    private JRadioButton rigid = new JRadioButton("Rigid");
    private JRadioButton flexible = new JRadioButton("Flexible");
    private JRadioButton noBond = new JRadioButton("None");

    private JMenuItem loadR = new JMenuItem("Load Rules");
    private JMenuItem exportC = new JMenuItem("Export Configuration");
    private JMenuItem about = new JMenuItem("About");
    private JMenuItem usagesHelp = new JMenuItem("Edit Mode Usage");
    private JMenuItem loadC = new JMenuItem("Load Configuration");
    private JMenuItem menuClear = new JMenuItem("Clear");
    private JMenuItem menuQuit = new JMenuItem("Quit");
    private JMenuItem simStart = new JMenuItem("Start");
    private JMenuItem simStop = new JMenuItem("Stop");
    private JMenuItem simPause = new JMenuItem("Pause");
    private JMenuItem record = new JMenuItem("Record");
    private JMenuItem ruleMk = new JMenuItem("Rule Creator");
    private JCheckBoxMenuItem agitationToggle = new JCheckBoxMenuItem("On");
    private JMenuItem agitationSetRate = new JMenuItem("Set Rate");

    private JPopupMenu editMonMenu = new JPopupMenu();

    // edit tool bar
    JMenuBar editToolBar = new JMenuBar();


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
    double speedRate = 1;
    int speedMax = 10;
    Double totalTime = 0.0;

    //Threads
    Thread simHeartBeat;
    Runnable simRunnable;

    //For panning
    Point lastXY;
    Point dragCnt = new Point(0, 0);


    //Monomer editing

    Monomer lastMon = null;


    //Video
    private QuickTimeWriter qtWr;
    private double timeAccum = 0;

    //Graphics
    BufferedImage hudBFI;
    Graphics2D hudGFx;

    //configurator modes/values
    boolean editMode = false;
    boolean brushMode = false;
    boolean singleMode = true;
    boolean flexibleMode = false;
    boolean rigidMode = false;
    boolean noBondMode = true;
    boolean statePaint = false;
    boolean eraser = false;

    String stateVal = "A";

    public Display(Dimension size) {
        //Threads
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(size.width, size.height);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        Simulation.canvasXYoffset = new Point(size.width / 2, -1 * (size.height / 2 - 60));

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
        mainFrame.addKeyListener(this);
        canvas.addMouseWheelListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(this);
        canvasStrokeSize = Simulation.monomerRadius / 3;
        //for the nubot graphics/image & visuals
        hudBFI = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        hudGFx = (Graphics2D)hudBFI.getGraphics();
        hudGFx.setColor(Color.BLACK);
        hudGFx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hudGFx.setFont(new Font("TimesRoman", Font.BOLD, 20));

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
        statusSpeed.setText("Speed: " + speedRate);
        statusTime.setText("Time: " + map.timeElapsed);
        statusMonomerNumber.setText("Monomers: " + map.getSize());

        JSeparator statusSeparator1 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator1.setMaximumSize(new Dimension(5, 25));
        JSeparator statusSeparator2 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator2.setMaximumSize(new Dimension(5, 25));
        JSeparator statusSeparator3 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator3.setMaximumSize(new Dimension(5, 25));
        JSeparator statusSeparator4 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator4.setMaximumSize(new Dimension(5, 25));
        JSeparator statusSeparator5 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator5.setMaximumSize(new Dimension(5, 25));
        JSeparator statusSeparator6 = new JSeparator(SwingConstants.VERTICAL);
        statusSeparator6.setMaximumSize(new Dimension(5, 25));

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
        timer = new Timer(1000 / 60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //  canvas.repaint();
            }
        });
        timer.setRepeats(true);

        simRunnable = new Runnable() {
            @Override
            public void run() {
                while (Simulation.isRunning) {
                    try {
                        if (!Simulation.isRecording)
                            Thread.sleep((long) (30 + speedRate * 1000.0 * map.executeTime));


                        timeAccum += map.executeFrame();

                        if (posLockMon != null && map.containsKey(posLockMon.getLocation())) {
                            if (Simulation.agitationON) {
                                Point monLockCVPos = Simulation.getCanvasPosition(posLockMon.getLocation());
                                if(Simulation.isRecording)
                                Simulation.canvasXYoffset.translate(800 / 2 - monLockCVPos.x , -600 / 2 + monLockCVPos.y);
                                else  Simulation.canvasXYoffset.translate(canvas.getWidth()/ 2 - monLockCVPos.x + dragCnt.x, -canvas.getHeight() / 2 + monLockCVPos.y - dragCnt.y);
                            }
                        } else {
                            Random rand = new Random();
                            posLockMon = (Monomer) map.values().toArray()[rand.nextInt(map.getSize())];
                        }

                        if (Simulation.animate)
                            canvas.repaint();
                        else if (Simulation.isRecording) {


                                    BufferedImage bfi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

                                    Graphics2D g2 = (Graphics2D)bfi.getGraphics();
                                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                    g2.setFont(new Font("TimesRoman", Font.PLAIN, 10));
                                    g2.setColor(Color.white);
                                    g2.fillRect(0,0,800,600);
                                    g2.setColor(Color.BLACK);
                                    g2.drawString("#Monomers: " + map.getSize(), 20, 40);
                                    g2.drawString("Step: " + map.nubotFrameNumber, 20, 50);
                                    g2.drawString("Elapsed: " +map.timeElapsed, 20, 60);
                                    try
                                    {
                                        if(timeAccum > .0050) {
                                            Dimension nubotDim = Simulation.calculateNubotDimension(new ArrayList<Monomer>(map.values()), Simulation.monomerRadius, new Point(0,0), new Dimension(800,600));
                                            while(nubotDim.width + Simulation.monomerRadius*2 > 800 || nubotDim.height + Simulation.monomerRadius*2
                                                    > 600) {
                                                Simulation.monomerRadius--;
                                                 nubotDim = Simulation.calculateNubotDimension(new ArrayList<Monomer>(map.values()), Simulation.monomerRadius, new Point(0,0), new Dimension(800,600));
                                            }



                                            drawMonomers(g2);
                                            qtWr.write(0, bfi, 1);
                                            timeAccum = 0;
                                        }

                                    }
                                    catch (Exception e)
                                    {

                                        System.out.println(e.getMessage());
                                    }




                        }

                        statusSimulation.setText("Simulating...");
                        totalTime += map.executeTime;
                        statusMonomerNumber.setText("Monomers: " + map.getSize());
                        statusTime.setText("Time: " + map.timeElapsed);
                    } catch (Exception e) {
                        System.out.println(e.getMessage() + "SDF");
                    }
                }
                try
                {
                    qtWr.close();
                }
                catch(Exception e)
                {
                    System.out.println("exception closing");
                }

                statusSimulation.setText("Simulation finished ");
                if (map.isFinished)
                    JOptionPane.showMessageDialog(canvas, "No more rules can be applied!", "Finished", JOptionPane.OK_OPTION);
            }
        };
    }

    public void initCanvas() {
        canvas = new JComponent() {


            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawMonomers(g);
                g.drawImage(hudBFI, 0, 0, null);
            }
        };
        canvas.setSize(mainFrame.getSize());
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        loadR.addActionListener(this);
        about.addActionListener(this);
        loadC.addActionListener(this);
        exportC.addActionListener(this);
        menuClear.addActionListener(this);
        menuQuit.addActionListener(this);
        simPause.addActionListener(this);
        simStart.addActionListener(this);
        simStop.addActionListener(this);
        record.addActionListener(this);
        agitationSetRate.addActionListener(this);
        agitationToggle.addActionListener(this);
        editToggle.addActionListener(this);
        editToolBar.setVisible(false);
        editToolBar.setFocusable(false);

        menuBar.add(file);
        menuBar.add(simulation);
        menuBar.add(settings);
        menuBar.add(help);
        menuBar.add(editToggle);
        editToggle.setMaximumSize(new Dimension(100,50));
        editToggle.setFocusable(false);
        menuBar.add(editToolBar);

        help.add(about);
        help.add(usagesHelp);
        usagesHelp.addActionListener(this);
       // file.add(ruleMk);
        file.add(loadR);
        file.add(loadC);
        file.add(exportC);
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

        //Edit ToolBar
        bondGroup = new ButtonGroup();
        drawMode = new ButtonGroup();
        bondGroup.add(rigid);
        rigid.setFocusable(false);
        bondGroup.add(flexible);
        flexible.setFocusable(false);
        bondGroup.add(noBond);
        noBond.setFocusable(false);
        noBond.setSelected(true);
        drawMode.add(editBrush);
        editBrush.setFocusable(false);
        drawMode.add(editState);
        editState.setFocusable(false);
        drawMode.add(single);
        single.setFocusable(false);
        drawMode.add(editEraser);
        editEraser.setFocusable(false);
        single.setSelected(true);
        editBrush.setHorizontalAlignment(JMenuItem.CENTER);

        editToolBar.add(editBrush);
        editToolBar.add(editState);
        editToolBar.add(single);
        editToolBar.add(editEraser);
        editToolBar.add(new JLabel("|Bonds:"));
        editToolBar.add(rigid);
        editToolBar.add(flexible);
        editToolBar.add(noBond);

        editToolBar.setPreferredSize(new Dimension(20, 20));
        //editToolBar.setFloatable(false);
        editBrush.addActionListener(this);
        editState.addActionListener(this);
        editEraser.addActionListener(this);
        single.addActionListener(this);
        rigid.addActionListener(this);
        flexible.addActionListener(this);
        noBond.addActionListener(this);

        mainFrame.setJMenuBar(menuBar);


        // speed Slider
        JSlider speedSlider = new JSlider(JSlider.VERTICAL, -speedMax, speedMax, 0);
        speedSlider.setMajorTickSpacing(20);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        Hashtable speedLabels = new Hashtable();
        speedLabels.put(-speedMax, new JLabel("Fast"));
        speedLabels.put(0, new JLabel("Normal"));
        speedLabels.put(speedMax, new JLabel("Slow"));
        speedSlider.setInverted(true);
        speedSlider.setLabelTable(speedLabels);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JSlider sliderSource = (JSlider) changeEvent.getSource();
                if (!sliderSource.getValueIsAdjusting()) {
                    speedRate = (double) sliderSource.getValue();
                    if (speedRate == 0)
                        speedRate = 1;
                    else if (speedRate < 1) {
                        speedRate = (speedMax + speedRate + 1) / 10;
                    }
                    System.out.println("speed changed to: " + speedRate);
                    statusSpeed.setText("Speed: " + speedRate);
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
        aboutGroupName.setFont(new Font("", Font.BOLD, 23));
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

        try {
            websiteLink = new URI("http://faculty.utpa.edu/orgs/asarg/");
            openURL = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(websiteLink);
                    } catch (IOException e1) {
                        System.out.println("error visiting the website URL");
                    }
                }
            };
        } catch (URISyntaxException e) {
            System.out.println("something is wrong with the URI or MouseAdapter");
        }
        groupLink.addMouseListener(openURL);

        aboutP.setBorder(new EmptyBorder(20, 20, 10, 20));
        aboutF.add(aboutP);
        aboutF.pack();
        aboutF.setVisible(false);


        //popup menu
        final JMenuItem removeMonomerMI = new JMenuItem("Remove");
        final JMenuItem changeStateMI = new JMenuItem("State");


        ActionListener edit = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == removeMonomerMI) {


                   map.removeMonomer(lastMon);


                    canvas.repaint();

                } else if (e.getSource() == changeStateMI) {
                    String state = JOptionPane.showInputDialog("New State:");
                    if (!state.isEmpty())
                        lastMon.setState(state);
                    canvas.repaint();
                }


            }
        };
        editMonMenu.add(changeStateMI);
        editMonMenu.add(removeMonomerMI);

        removeMonomerMI.addActionListener(edit);
        changeStateMI.addActionListener(edit);


    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == rigid)
        {
            flexibleMode = false;
            rigidMode = true;
            noBondMode = false;

        }
        if(e.getSource() == flexible)
        {
            rigidMode = false;
            flexibleMode = true;
            noBondMode = false;

        }
        if(e.getSource() == noBond)
        {
            rigidMode = false;
            flexibleMode = false;
            noBondMode = true;
        }
        if(e.getSource() == editBrush )
        {
            System.out.println("SDFDSF");
            brushMode = true;
            singleMode = false;
            statePaint = false;
            eraser = false;

        }
        if(e.getSource() == editEraser )
        {
            brushMode = false;
            singleMode = false;
            statePaint = false;
            eraser = true;

        }
        if(e.getSource() == editState )
        {
            brushMode = false;
            singleMode = false;
            statePaint = true;
            eraser = false;

        }
        if(e.getSource() == single )
        {
            brushMode = true;
            singleMode = true;
            statePaint = false;
            eraser = false;

        }


        if(e.getSource() == usagesHelp)
        {
            String commands = "Ctrl + Drag : Monomer/State\nAlt + Drag : Bonds\nCtrl + 1 : Brush\nCtrl + 2 : State Mode\nCtrl + 3 : Single\nCtrl + 4: Eraser\nAlt + 1 : Rigid\nAlt + 2 : Flexible\nAlt + 3 : No Bond\nCtrl + Shift + 1 : Set State Value";
            JOptionPane.showMessageDialog(canvas, commands);
        }
        if(e.getSource() == exportC)
        {
            int mapSize = map.size();


            if(mapSize > 0)
            {
           String saveFN = JOptionPane.showInputDialog("File Name", ".conf");
            System.out.println("sdfs");
           File file = new File(saveFN);
            try{
                BufferedWriter bfW = new BufferedWriter(new FileWriter(file));
                bfW.write("States:");
                bfW.newLine();

                Set<Map.Entry<Point, Monomer>> setPM = map.entrySet();

                    for(Map.Entry<Point, Monomer> set : setPM)
                    {
                         bfW.write((int)set.getKey().getX() + " " + (int)set.getKey().getY() + " " +set.getValue().getState() );
                         bfW.newLine();
                    }
                    bfW.newLine();
                    bfW.write("Bonds:");
                    bfW.newLine();
                    for(Map.Entry<Point, Monomer> set : setPM)
                    {
                        ArrayList<Byte> rigidList = set.getValue().getDirsByBondType(Bond.TYPE_RIGID);
                        ArrayList<Byte> flexList = set.getValue().getDirsByBondType(Bond.TYPE_FLEXIBLE);
                        Point startPoint = set.getKey();
                        for(Byte d : rigidList)
                        {
                            Point neighborPoint = Direction.getNeighborPosition(set.getKey(), d);
                            bfW.write(startPoint.x + " " + startPoint.y + " " + neighborPoint.x + " " + neighborPoint.y + " " + 1);
                            bfW.newLine();

                        }
                        for(Byte d : flexList)
                        {
                            Point neighborPoint = Direction.getNeighborPosition(set.getKey(), d);
                            bfW.write(startPoint.x + " " + startPoint.y + " " + neighborPoint.x + " " + neighborPoint.y + " " + 2);
                            bfW.newLine();

                        }


                    }



                bfW.close();
            }

            catch(IOException exc)
            {

            }
            }
            else JOptionPane.showMessageDialog(mainFrame, "No monomers in current configuration.");



        }
        if (e.getSource() == loadR) {
            map.timeElapsed = 0;
            map.rules.clear();
            try {
                final JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(new File("."));

                jfc.setDialogTitle("Select Rules File");
                // Creating a file filter for .conf
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory())
                            return true;
                        String fname = f.getName();
                        if (fname.length() > 6 && fname.substring(fname.length() - 6, fname.length()).matches(".rules"))
                            return true;

                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "Nubot Rules File - .rules";
                        //return null;
                    }
                });

                int resVal = jfc.showOpenDialog(mainFrame);

                // if the ret flag results as Approve, we parse the file
                if (resVal == JFileChooser.APPROVE_OPTION) {
                    statusRules.setText("Loading rules ");
                    File theFile = jfc.getSelectedFile();
                    //if the selected file is of the right extension
                    if (theFile.length() > 5 && theFile.getName().substring(theFile.getName().length() - 6, theFile.getName().length()).matches(".rules")) {
                        map.rules.clear();
                        rulesFileName = theFile.getName();
                        FileReader fre = new FileReader(theFile);
                        BufferedReader bre = new BufferedReader(fre);
                        boolean cont = true;

                        while (cont) {
                            String line = bre.readLine();

                            if (line == null)
                                cont = false;

                            //if it's not a comment line and not empty, we parse
                            if (line != null && !line.contains("[") && !line.isEmpty() && line != "") {
                                String[] splitted = line.split(" ");
                                map.rules.addRule(new Rule(splitted[0], splitted[1], (byte) Integer.parseInt(splitted[2]), Direction.stringToFlag(splitted[3]), splitted[4], splitted[5], (byte) Integer.parseInt(splitted[6]), Direction.stringToFlag(splitted[7])));
                            }
                        }

                        bre.close();

                        Simulation.rulesLoaded = true;
                        if (Simulation.debugMode)
                            System.out.println("We have " + map.rules.size() + " rules");

                        statusRules.setText("Rules loaded ");

                        if (Simulation.rulesLoaded && Simulation.configLoaded) {

                            Random rand = new Random(System.currentTimeMillis());
                            posLockMon = (Monomer) map.values().toArray()[rand.nextInt(map.size())];
                            System.out.println(rand.nextInt());
                            simStart.setEnabled(true);
                            record.setEnabled(true);
                            statusSimulation.setText("Ready to Start ");
                        }
                    }

                    System.out.println(map.rules.values());
                }
            } catch (Exception exc) {

            }


            System.out.println("Load Rules");
        } else if (e.getSource() == loadC) {
            map.timeElapsed = 0;
            statusTime.setText("Time: " + map.timeElapsed);
            map.clear();
            try {
                final JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(new File("."));

                jfc.setDialogTitle("Select Configuration File");
                // Creating a file filter for .conf
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory())
                            return true;
                        String fname = f.getName();
                        if (fname.length() > 5 && fname.substring(fname.length() - 5, fname.length()).matches(".conf"))
                            return true;

                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "Nubot Configuration File - .conf";
                        //return null;
                    }
                });

                int resVal = jfc.showOpenDialog(mainFrame);

                // if the ret flag results as Approve, we parse the file
                if (resVal == JFileChooser.APPROVE_OPTION) {
                    File theFile = jfc.getSelectedFile();
                    //if the selected file is of the right extension
                    if (theFile.length() > 5 && theFile.getName().substring(theFile.getName().length() - 5, theFile.getName().length()).matches(".conf")) {
                        configFileName = theFile.getName();
                        map.clear();
                        boolean inBonds = false;
                        FileReader fre = new FileReader(theFile);
                        BufferedReader bre = new BufferedReader(fre);
                        boolean cont = true;

                        while (cont) {
                            String line = bre.readLine();
                            if (line == null)
                                cont = false;
                            //if it's not a comment line and not empty, we parse
                            if (line != null && !line.contains("[") && !line.isEmpty() && !(line == "")) {
                                if (!inBonds) {
                                    if (line.contains("States:")) {

                                    } else if (line.contains("Bonds:")) {
                                        inBonds = true;
                                    } else {
                                        String[] splitted = line.split(" ");
                                        map.addMonomer(new Monomer(new Point(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1])), splitted[2]));
                                    }
                                } else if (inBonds) {
                                    String[] splitted = line.split(" ");
                                    // map.adjustBond(,);
                                    Point monomerPoint1 = new Point(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
                                    Point monomerPoint2 = new Point(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));
                                    byte bondType = (byte) Integer.parseInt(splitted[4]);
                                    if (map.containsKey(monomerPoint1) && map.containsKey(monomerPoint2) && Direction.dirFromPoints(monomerPoint1, monomerPoint2) > 0) {
                                        map.get(monomerPoint1).adjustBond(Direction.dirFromPoints(monomerPoint1, monomerPoint2), bondType);
                                        map.get(monomerPoint2).adjustBond(Direction.dirFromPoints(monomerPoint2, monomerPoint1), bondType);
                                    }
                                } else if (Simulation.debugMode)
                                    System.out.println("We don't have more sections.");
                            }
                        }

                        bre.close();
                        Simulation.configLoaded = true;

                        statusConfig.setText("Config loaded ");
                        statusMonomerNumber.setText("Monomers: " + map.getSize());


                        canvas.repaint();
                        if (Simulation.configLoaded && (Simulation.rulesLoaded || Simulation.agitationON)) {
                            Random rand = new Random();
                            posLockMon = (Monomer) map.values().toArray()[rand.nextInt(map.size())];
                            System.out.println(posLockMon.getState());
                            simStart.setEnabled(true);
                            record.setEnabled(true);
                            statusSimulation.setText("Ready to Start");
                        }
                    }
                }
            } catch (Exception exc) {
            }
            System.out.println("Load config");
        } else if (e.getSource() == menuClear) {
            clearGraphics();

            canvas.repaint();
            map.clear();
            map.rules.clear();
            map.timeElapsed = 0;
            statusTime.setText("Time: " + map.timeElapsed);
            /////Simulation Flags
            Simulation.configLoaded = false;
            Simulation.rulesLoaded = false;
            Simulation.isRunning = false;
            Simulation.isRecording = false;
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
        } else if (e.getSource() == menuQuit) {
            System.out.println("quit application");
            System.exit(0);
        } else if (e.getSource() == about) {
            System.out.println("about this application");
            aboutF.setVisible(true);
        } else if (e.getSource() == simStart) {

            Simulation.animate = true;
            Simulation.isRunning = true;
            map.isFinished = false;
            Simulation.isPaused = false;
            loadC.setEnabled(false);
            loadR.setEnabled(false);
            simPause.setEnabled(true);
            simStop.setEnabled(true);
            simHeartBeat = new Thread(simRunnable);
            simHeartBeat.start();
            // timer.start();

            System.out.println("start");
        } else if (e.getSource() == simStop) {
            Simulation.isRunning = false;
            //timer.stop();
            map.timeElapsed = 0;
            simHeartBeat.interrupt();
            System.out.println("stop");
        } else if (e.getSource() == simPause) {
            timer.stop();
            Simulation.isRunning = false;
            Simulation.isPaused = true;

            System.out.println("pause");
        } else if (e.getSource() == agitationToggle) {
            if (Simulation.agitationRate == 0.0) {
                JOptionPane.showMessageDialog(mainFrame, "Please set the agitation rate", "Error", JOptionPane.ERROR_MESSAGE);
                agitationToggle.setState(false);
            } else {
                Simulation.agitationON = agitationToggle.getState();
                if (Simulation.agitationON == true)
                    statusAgitation.setText("Agitation On: " + Simulation.agitationRate);
                else
                    statusAgitation.setText("Agitation Off ");
                System.out.println("Agitation is: " + Simulation.agitationON + ' ' + Simulation.agitationRate);
            }
        } else if (e.getSource() == agitationSetRate) {
            String agitationRateString = JOptionPane.showInputDialog(mainFrame, "Set Agitation Rate", "Agitation", JOptionPane.PLAIN_MESSAGE);
            if (agitationRateString != null) {
                Simulation.agitationRate = Double.parseDouble(agitationRateString);
                Simulation.agitationON = true;
                statusAgitation.setText("Agitation On: " + Simulation.agitationRate);
                agitationToggle.setState(true);
                System.out.println("Agitation Rate changed and set to on");

                if (Simulation.configLoaded && Simulation.agitationON) {
                    simStart.setEnabled(true);
                    statusSimulation.setText("Ready to Start");
                }
            }
        } else if (e.getSource() == record) {


            map.resetVals();
            String input = JOptionPane.showInputDialog(mainFrame, "Recording length(Nubot Time):");
            double recordingLength = Double.parseDouble(input);
            if (recordingLength > 0) {

                try{
                    qtWr = new QuickTimeWriter(new File("Test.mov"));
                    qtWr.addVideoTrack(QuickTimeWriter.VIDEO_PNG, 30, 800, 600);
                    Simulation.canvasXYoffset.setLocation(400, -300);

                }
                catch(Exception exc)
                {
                    System.out.println(exc.getMessage() + "SDF");
                }
                Simulation.animate = false;
                Simulation.recordingLength = recordingLength;
                Simulation.isRecording = true;
                map.timeElapsed = 0;
                map.initRecord();
                Simulation.isRunning = true;
                map.isFinished = false;
                Simulation.isPaused = false;
                simStop.setEnabled(true);
                simHeartBeat = new Thread(simRunnable);
                simHeartBeat.start();
                statusSimulation.setText("Recording.");
                JOptionPane.showMessageDialog(mainFrame, "The simulation is recording and will not be animated.");

            }
            System.out.println("record button started");
        } else if (e.getSource() == editToggle) {
            System.out.println("edit Toggle");
            editMode = true;
            editToolBar.setVisible(editToggle.getState());
        }
    }

    public synchronized void drawMonomer(Monomer m, Graphics2D g) {

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
        g.setColor(new Color(2, 180, 206));
        g.drawOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setColor(Color.white);
        Rectangle2D bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        while (bounds.getWidth() < monomerWidth - 4 && bounds.getHeight() < monomerHeight - 4) {
            g.setFont(g.getFont().deriveFont((float) ++fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        while (bounds.getWidth() > monomerWidth - 4 || bounds.getHeight() > monomerHeight - 4) {
            g.setFont(g.getFont().deriveFont((float) --fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        g.setColor(Color.BLACK);
        g.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + Simulation.monomerRadius - (int) bounds.getWidth() / 2 - monomerWidthAdjustment / 2,
                /*Y Coord */    xyPos.y + Simulation.monomerRadius + (int) (bounds.getHeight() / 3.5));
    }

    public synchronized void drawMonomers(Graphics g) {

        Monomer[] mapTemp = new Monomer[map.getSize()];
        map.values().toArray(mapTemp);

        ArrayList<Monomer> tempMonList = new ArrayList<Monomer>();

        for (Monomer m : mapTemp) {
            if(Simulation.monomerRadius >=7)
            drawBond(m, (Graphics2D) g);
            tempMonList.add(new Monomer(m));

        }


        for (Monomer m : tempMonList) {

            drawMonomer(m, (Graphics2D) g);
        }


    }
    public void showToast(int x, int y, String text, int duration)
    {
        hudGFx.setComposite(AlphaComposite.Clear);
        hudGFx.fillRect(0, 0, canvas.getWidth(), canvas.getWidth());
        hudGFx.setComposite(AlphaComposite.SrcOver);
        hudGFx.drawString(text, x, y);
        canvas.repaint();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable clearBFI = new Runnable() {
            @Override
            public void run() {
                hudGFx.setComposite(AlphaComposite.Clear);
                hudGFx.fillRect(0,0, canvas.getWidth(), canvas.getWidth());
                hudGFx.setComposite(AlphaComposite.SrcOver);
                canvas.repaint();



            }
        } ;


        executor.schedule(clearBFI, duration, TimeUnit.MILLISECONDS);




    }


    public synchronized void drawBond(Monomer m, Graphics2D g) {

        Monomer tempMon = new Monomer(m);
        if (tempMon.hasBonds()) {
            ArrayList<Byte> rigidDirList = tempMon.getDirsByBondType(Bond.TYPE_RIGID);
            ArrayList<Byte> flexibleDirList = tempMon.getDirsByBondType(Bond.TYPE_FLEXIBLE);

            g.setColor(Color.RED);
            for (Byte dir : rigidDirList) {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
            }

            for (Byte dir : flexibleDirList) {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize * 1.20f));
                g.setColor(Color.RED);
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(canvasStrokeSize * .80f));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
            }
        }
    }


    private void clearGraphics() {
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, mainFrame.getSize().width, mainFrame.getSize().height);
        g.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {


        if (e.getWheelRotation() == 1.0 && Simulation.monomerRadius > 2) {
            int newRadius = (int) Math.round(Simulation.monomerRadius * .92);
            Simulation.monomerRadius = newRadius;
            if(Simulation.monomerRadius == newRadius && Simulation.monomerRadius > 0)
            {
                 Simulation.monomerRadius--;
            }
            canvasStrokeSize = Simulation.monomerRadius / 3;


            //  if(!Simulation.isRunning)
            canvas.repaint();
        } else if (Simulation.monomerRadius < canvas.getWidth() / 10) {
            Simulation.monomerRadius = (int) Math.ceil(Simulation.monomerRadius * 1.08);
            canvasStrokeSize = Simulation.monomerRadius / 3;
            //if(!Simulation.isRunning)
            canvas.repaint();
        }


    }

    @Override
    public void mouseDragged(MouseEvent e) {


        if (SwingUtilities.isLeftMouseButton(e) && !e.isAltDown() && !e.isControlDown()) {
            if (lastXY == null)
                lastXY = e.getPoint();
            Simulation.canvasXYoffset.translate(e.getX() - lastXY.x, -(e.getY() - lastXY.y));
            dragCnt.translate(e.getX() - lastXY.x, e.getY() - lastXY.y);

            lastXY = e.getPoint();
        }


  /*      if (SwingUtilities.isRightMouseButton(e)) {


            boolean fin = false;
            Monomer tmp = null;
            Point gp = Simulation.getCanvasToGridPosition(e.getPoint());
            if (map.containsKey(gp)) {
                tmp = map.get(gp);


            } else fin = true;


            // if (!fin && lastMon != null && tmp != null && !tmp.equals(lastMon)) {
            //  System.out.println(tmp.getLocation() + " " + lastMon.getLocation());
            if (e.isAltDown() && e.isShiftDown()) {
                lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_NONE);
                tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_NONE);
            } else if (e.isShiftDown()) {
                lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_RIGID);
                tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_RIGID);

            } else if (e.isAltDown()) {
                lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_FLEXIBLE);
                tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_FLEXIBLE);

            }
            if (e.isControlDown()) {
                if (paint.isSelected())
                    map.addMonomer(new Monomer(Simulation.getCanvasToGridPosition(e.getPoint()), "A"));
                System.out.println("DD" + Simulation.getCanvasToGridPosition(e.getPoint()) + map.size());
            }

            canvas.repaint();


            // }
            lastMon = tmp;

        }   */


        if(editMode) {
            if (e.isAltDown()) {
                Point cPoint = Simulation.getCanvasToGridPosition(e.getPoint());

                if (brushMode) {

                    map.addMonomer(new Monomer(cPoint, stateVal));
                } else if (eraser) {

                    if (map.containsKey(cPoint)) {
                        map.removeMonomer(cPoint);
                        canvas.repaint();
                    }

                }
                else if(statePaint)
                {
                    if(map.containsKey(cPoint))
                    {
                        map.get(cPoint).setState(stateVal);
                    }
                }
            } else if (e.isControlDown()) {
                Monomer tmp = null;
                Point gp = Simulation.getCanvasToGridPosition(e.getPoint());
                if (map.containsKey(gp)) {
                    tmp = map.get(gp);


                }
                if (lastMon != null && tmp != null) {
                    if (flexibleMode) {
                        lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_FLEXIBLE);
                        tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_FLEXIBLE);
                    } else if (rigidMode) {
                        lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_RIGID);
                        tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_RIGID);
                    } else if (noBondMode) {
                        lastMon.adjustBond(Direction.dirFromPoints(lastMon.getLocation(), tmp.getLocation()), Bond.TYPE_NONE);
                        tmp.adjustBond(Direction.dirFromPoints(tmp.getLocation(), lastMon.getLocation()), Bond.TYPE_NONE);
                    }
                }


                lastMon = tmp;

            }
        }



        //if(!Simulation.isRunning)
        canvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        System.out.println(e.getPoint() + " and " + Simulation.getCanvasToGridPosition(e.getPoint()) + " canvXYOf: " + Simulation.canvasXYoffset);


        Point gp = Simulation.getCanvasToGridPosition(e.getPoint());
        if (map.containsKey(gp)) {
            Monomer tmp = map.get(Simulation.getCanvasToGridPosition(e.getPoint()));
            lastMon = tmp;
            //  tmp.setState("awe");
            posLockMon = tmp;
        }
        if (e.isControlDown() && SwingUtilities.isRightMouseButton(e)) {


            if (!map.containsKey(gp)) {

                String state = JOptionPane.showInputDialog("State: ");
                if (!state.isEmpty())
                    map.addMonomer(new Monomer(gp, state));
            }
        } else if (map.containsKey(gp) && SwingUtilities.isRightMouseButton(e)) {
            editMonMenu.show(canvas, e.getX(), e.getY());
        }

        canvas.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastXY = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {


    }

    @Override
    public void keyPressed(KeyEvent e) {

        System.out.println(e.getKeyCode());
        int keyCode = e.getKeyCode();
        if(e.isControlDown() && !e.isAltDown() && !e.isShiftDown())
        {

            switch(keyCode)
            {

                case KeyEvent.VK_1:
                    showToast(20, 40, "Brush", 1200);
                    brushMode =true;
                    singleMode =false;
                    eraser = false;
                    editBrush.setSelected(true);
                    break;
                case KeyEvent.VK_3:
                    showToast(20, 40, "Single", 1200);
                    brushMode =false;
                    singleMode =true;
                    single.setSelected(true);
                    eraser = false;
                    break;
                case KeyEvent.VK_4:
                    showToast(20,40, "Eraser", 1200);
                    brushMode = false;
                    singleMode = false;
                    editEraser.setSelected(true);
                    eraser = true;
                    break;
                case KeyEvent.VK_2:
                    editState.setSelected(true);
                    brushMode = false;
                    eraser = false;
                    singleMode = false;
                    statePaint = true;
                    showToast(20,40, "State Paint", 1200);
                    break;

            }

        }
        else if(e.isAltDown() && !e.isControlDown() && !e.isShiftDown())
        {

            switch(keyCode)
            {

                case KeyEvent.VK_1:
                    showToast(20, 40, "Rigid", 1200);
                    flexibleMode = false;
                    rigid.setSelected(true);
                    rigidMode = true;
                    break;
                case KeyEvent.VK_2:
                    showToast(20, 40, "Flexible", 1200);
                    rigidMode =false;
                    flexibleMode =true;
                    flexible.setSelected(true);
                    break;
                case KeyEvent.VK_3:
                    showToast(20, 40, "No Bond", 1200);
                    rigidMode =false;
                    noBond.setSelected(true);
                    flexibleMode =false;
                    break;
            }

        }
        else if(e.isShiftDown() && e.isControlDown())
        {
            switch(keyCode)
            {
                case KeyEvent.VK_1:
                     String state = JOptionPane.showInputDialog("Set paint state:");
                    if(state.length() > 0)
                     stateVal = state;
                    break;



            }

        }


    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            if (!Simulation.isPaused) {
                timer.stop();
                Simulation.isRunning = false;
                Simulation.isPaused = true;
            } else {
                timer.start();
                simHeartBeat = new Thread(simRunnable);
                simHeartBeat.start();
                Simulation.isRunning = true;
                Simulation.isPaused = false;
            }
        }
    }
}