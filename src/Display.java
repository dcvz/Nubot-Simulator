//
// Display.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Display implements ActionListener, ComponentListener, MouseWheelListener, MouseMotionListener{
    int fontSize = 20;
    Timer timer;
    final JFrame mainFrame = new JFrame("Nubot Simulator");
    final JFrame aboutF = new JFrame("A.S.A.R.G");
    JComponent canvas;
    //Config and rules
    Configuration map;
    BufferedImage nubotImage;
    BufferedImage bondLayerImage;
    BufferedImage hudImage;
    Graphics2D nubotGFX;
    Graphics2D bondLayerGFX;
    Graphics2D hudLayerGFX;

    //Menus
    private JMenu file = new JMenu("File");
    private JMenu simulation = new JMenu("Simulation");
    private JMenu settings = new JMenu("Settings");
    private JMenu help = new JMenu("Help");
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
    private JMenuItem agitation = new JMenuItem("Agitation");
    private JMenuItem speed = new JMenuItem("Speed");
    //Data
    String rulesFileName = "";
    String configFileName = "";
    //change to default starting value later
    Double agitationRate;
    Double speedRate;



    //Threads
    Thread simHeartBeat;
    Runnable simRunnable;

   //For panning
    Point lastXY = new Point(0,0);

    public Display() {
        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(Simulation.frameSize.width, Simulation.frameSize.height);
        mainFrame.setLayout(new BorderLayout());


        initMenuBar();
        //post creation menubar setup
        simStart.setEnabled(false);
        simPause.setEnabled(false);
        simStop.setEnabled(false);


        map = new Configuration();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initCanvas();
        mainFrame.add(canvas);
        System.out.println(canvas.getSize());
        mainFrame.setVisible(true);
        mainFrame.addComponentListener(this);
        canvas.addMouseWheelListener(this);
        canvas.addMouseMotionListener(this);
        //for the nubot graphics/image & visuals
        nubotImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        nubotGFX = (Graphics2D) nubotImage.getGraphics();
        nubotGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        nubotGFX.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
        bondLayerImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bondLayerGFX = (Graphics2D) bondLayerImage.getGraphics();
        bondLayerGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hudImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        hudLayerGFX = (Graphics2D)hudImage.getGraphics();
        hudLayerGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        nubotGFX.setFont(new Font("TimesRoman", Font.BOLD, fontSize));


        //////
        ///Threads/Timer
        /////
        timer = new Timer(1000/60 , new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!Simulation.isPaused)
                {
                    if (!map.isFinished)
                    {     clearImages();
                       try{     for (Monomer m : map.values()) {

                        drawBond(m);
                        drawMonomer(m);

                    }       }
                       catch(Exception exc)
                       {

                       }
                        drawHud();

                    }
                }

                canvas.repaint();
            }
        });
        timer.setRepeats(true);

        simRunnable = new Runnable() {
            @Override
            public void run() {
                while(Simulation.isRunning) {
                    try {
                        Thread.sleep(80);
                        map.executeFrame();
                        System.out.println("SDFSD");
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getCause().getMessage());
                    }



                }

            }
        };



    }

    public void initCanvas() {
        canvas = new JComponent() {

            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(bondLayerImage, 0, 0, null);
                g.drawImage(nubotImage, 0, 0, null);
                g.drawImage(hudImage, 0, 0, null);
            }

        };
        canvas.setSize(Simulation.canvasSize);
    }

    private void initMenuBar() {
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
        agitation.addActionListener(this);
        speed.addActionListener(this);

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
        settings.add(agitation);
        settings.add(speed);

        mainFrame.setJMenuBar(menuBar);

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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadR) {
            map.rules.clear();
            try {
                final JFileChooser jfc = new JFileChooser();

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

                        if (Simulation.rulesLoaded && Simulation.configLoaded)
                            simStart.setEnabled(true);
                    }

                    System.out.println(map.rules.values());
                }
            } catch (Exception exc) {

            }

            System.out.println("Load Rules");
        } else if (e.getSource() == loadC) {

            map.clear();
            try {
                final JFileChooser jfc = new JFileChooser();

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
                                    map.get(monomerPoint1).adjustBond(Direction.dirFromPoints(monomerPoint1, monomerPoint2), bondType);
                                    map.get(monomerPoint2).adjustBond(Direction.dirFromPoints(monomerPoint2, monomerPoint1), bondType);
                                } else if (Simulation.debugMode)
                                    System.out.println("We don't have more sections.");
                            }
                        }

                        bre.close();
                        Simulation.configLoaded = true;
                        drawMonomers();
                        canvas.repaint();
                        if (Simulation.configLoaded && Simulation.rulesLoaded)
                            simStart.setEnabled(true);
                    }
                }
            } catch (Exception exc) {
            }
            System.out.println("Load config");
        } else if (e.getSource() == menuClear) {
            clearImages();
            canvas.repaint();
            map.clear();
            map.rules.clear();
            Simulation.configLoaded = false;
            Simulation.rulesLoaded = false;
            Simulation.isRunning = false;
            simStart.setEnabled(false);
            simPause.setEnabled(false);
            simStop.setEnabled(false);
            loadC.setEnabled(true);
            loadR.setEnabled(true);
            System.out.println("clear config");
        } else if (e.getSource() == menuQuit) {
            System.out.println("quit application");
            System.exit(0);
        } else if (e.getSource() == about) {
            System.out.println("about this application");
            aboutF.setVisible(true);
        } else if (e.getSource() == simStart) {
            Simulation.isRunning = true;
            map.isFinished=false;
            Simulation.isPaused = false;
            loadC.setEnabled(false);
            loadR.setEnabled(false);
            simPause.setEnabled(true);
            simStop.setEnabled(true);
            simHeartBeat = new Thread(simRunnable);
            simHeartBeat.start();
            timer.start();

            System.out.println("start");
        } else if (e.getSource() == simStop) {
            Simulation.isRunning = false;
            timer.stop();
            simHeartBeat.interrupt();
            System.out.println("stop");
        } else if (e.getSource() == simPause) {
            timer.stop();
            Simulation.isRunning = false;
            System.out.println("pause");
        } else if (e.getSource() == agitation) {
            String agitationRateString = JOptionPane.showInputDialog(mainFrame,"Set Agitation Rate","Agitation",JOptionPane.PLAIN_MESSAGE);
            if (agitationRateString != null) {
                agitationRate = Double.parseDouble(agitationRateString);
                System.out.println("Agitation Rate changed");
            }
        } else if (e.getSource() == speed) {
            String speedRateString = JOptionPane.showInputDialog(mainFrame,"Set Speed", "Speed",JOptionPane.PLAIN_MESSAGE);
            if (speedRateString != null) {
                speedRate = Double.parseDouble(speedRateString);
                System.out.println("speed changed");
            }
        } else if (e.getSource() == record) {
            System.out.println("record button started");
        }
    }

    private void drawMonomer(Monomer m) {
        nubotGFX.setColor(Color.BLACK);

        Point xyPos = Simulation.getCanvasPosition(m.getLocation());
        int monomerWidthAdjustment = Simulation.monomerRadius / 4;
        int monomerWidth = Simulation.monomerRadius * 2 - monomerWidthAdjustment;
        int monomerHeight = Simulation.monomerRadius * 2;
        nubotGFX.fillOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        nubotGFX.setColor(Color.WHITE);
        Rectangle2D bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext());
        while (bounds.getWidth() < monomerWidth -2 && bounds.getHeight() < monomerHeight - 2) {
            nubotGFX.setFont(nubotGFX.getFont().deriveFont((float) ++fontSize));
            bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext());
        }
        while (bounds.getWidth() > monomerWidth -2 || bounds.getHeight() > monomerHeight -2) {
            nubotGFX.setFont(nubotGFX.getFont().deriveFont((float) --fontSize));
            bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext());
        }

        nubotGFX.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + Simulation.monomerRadius - (int) bounds.getWidth() / 2 - monomerWidthAdjustment,
                /*Y Coord */    xyPos.y + Simulation.monomerRadius + (int) (bounds.getHeight() / 3.5));
    }

    private void drawMonomers()
    {
        for (Monomer m : map.values()) {

            drawBond(m);
            drawMonomer(m);

        }
      //  canvas.repaint();

    }


    public void drawBond(Monomer m) {
        bondLayerGFX.setStroke(new BasicStroke(2f));
        if (m.hasBonds()) {
            ArrayList<Byte> rigidDirList = m.getDirsByBondType(Bond.TYPE_RIGID);
            ArrayList<Byte> flexibleDirList = m.getDirsByBondType(Bond.TYPE_FLEXIBLE);

            bondLayerGFX.setColor(Color.RED);
            for (Byte dir : rigidDirList) {
                Point start = Simulation.getCanvasPosition(m.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(m.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                bondLayerGFX.drawLine(start.x - 2, start.y, end.x - 2, end.y);
            }
            bondLayerGFX.setColor(Color.CYAN);
            for (Byte dir : flexibleDirList) {
                Point start = Simulation.getCanvasPosition(m.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(m.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                bondLayerGFX.drawLine(start.x-2, start.y, end.x - 2, end.y);
            }
        }
    }
    private void drawHud()
    {
        hudLayerGFX.drawString("dsfgdfgdfgdfg2342342dsf", 0, 100);

    }

    private void clearImages() {
        nubotGFX.setComposite(AlphaComposite.Clear);
        nubotGFX.fillRect(0, 0, nubotImage.getWidth(), nubotImage.getHeight());
        nubotGFX.setComposite(AlphaComposite.SrcOver);
        bondLayerGFX.setComposite(AlphaComposite.Clear);
        bondLayerGFX.fillRect(0, 0, nubotImage.getWidth(), nubotImage.getHeight());
        bondLayerGFX.setComposite(AlphaComposite.SrcOver);
        hudLayerGFX.setComposite(AlphaComposite.Clear);
        hudLayerGFX.fillRect(0, 0, nubotImage.getWidth(), nubotImage.getHeight());
        hudLayerGFX.setComposite(AlphaComposite.SrcOver);

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
            if(!Simulation.isRunning)
            clearImages();
            nubotGFX.scale(.95,.95);
            bondLayerGFX.scale(.95,.95);
            nubotGFX.translate( Simulation.canvasSize.width -Simulation.canvasSize.width * .972, Simulation.canvasSize.height -Simulation.canvasSize.height * .972);
            bondLayerGFX.translate( Simulation.canvasSize.width -Simulation.canvasSize.width * .972, Simulation.canvasSize.height -Simulation.canvasSize.height * .972);
            if(!Simulation.isRunning) {
                drawMonomers();
                canvas.repaint();
            }

        }

        else
        {
            if(!Simulation.isRunning)
            clearImages();
            nubotGFX.scale(1.05,1.05);
            bondLayerGFX.scale(1.05,1.05);
            nubotGFX.translate( Simulation.canvasSize.width -Simulation.canvasSize.width * 1.025, Simulation.canvasSize.height -Simulation.canvasSize.height * 1.025);
            bondLayerGFX.translate( Simulation.canvasSize.width -Simulation.canvasSize.width * 1.025, Simulation.canvasSize.height -Simulation.canvasSize.height * 1.025);

            if(!Simulation.isRunning) {
                drawMonomers();
                canvas.repaint();
            }
        }


    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if(lastXY.x > e.getX()) {
            nubotGFX.translate(-2, 0);
            bondLayerGFX.translate(-2,0);
        }
        else if(lastXY.x < e.getX()) {
            nubotGFX.translate(2, 0);
            bondLayerGFX.translate(2,0);
        }
        if(lastXY.y > e.getY()) {
            nubotGFX.translate(0, -2);
            bondLayerGFX.translate(0,-2);
        }
        else if(lastXY.y < e.getY()) {
            nubotGFX.translate(0, 2);
            bondLayerGFX.translate(0, 2);
        }

        lastXY = e.getPoint();
        if(!Simulation.isRunning)
        {
            clearImages();
            drawMonomers();
            canvas.repaint();

        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}