//
// Display.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Display implements ActionListener
{
    int fontSize = 20;
    Timer timer;
    final JFrame mainFrame = new JFrame("Nubot Simulator");
    JComponent canvas;
    //Config and rules
    Configuration map;
    RuleSet rules;
    BufferedImage nubotImage;
    Graphics2D nubotGFX;
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



    public Display()
    {
        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(Simulation.frameSize.width, Simulation.frameSize.height);
        mainFrame.setLayout(new BorderLayout());


        initMenuBar();
        Simulation.canvasSize = mainFrame.getContentPane().getSize();
        map = new Configuration();
        rules = new RuleSet();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initCanvas();
        mainFrame.add(canvas);
        mainFrame.setVisible(true);

        //for the nubot graphics/image & visuals
        nubotImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        nubotGFX = (Graphics2D)nubotImage.getGraphics();
        nubotGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        nubotGFX.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));

        timer = new Timer(1000/60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nubotGFX.setComposite(AlphaComposite.Clear);
                nubotGFX.fillRect(0, 0, nubotImage.getWidth(), nubotImage.getHeight());
                nubotGFX.setComposite(AlphaComposite.SrcOver);
                for(Monomer m : map.values())
                  drawMonomer(m);
                //System.out.println(map.size());
                canvas.repaint();

            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    public void initCanvas()
    {
        canvas = new JComponent() {

            @Override
            public void paintComponent(Graphics g)
            {
                g.drawImage(nubotImage, 0 , 0 , null );
            }

        };
        canvas.setSize(Simulation.canvasSize);




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
        simulation.add(simPause);
        simulation.add(new JSeparator(SwingConstants.HORIZONTAL));
        simulation.add(simStop);
        settings.add(agitation);
        settings.add(speed);

        mainFrame.setJMenuBar(menuBar);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loadR)
        {

            System.out.println("Load Rules");
        }
        else if (e.getSource() == loadC)
        {

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
                        if (fname.length() > 5 && fname.substring(fname.length() - 5, fname.length()).matches(".conf")) {
                            return true;
                        }

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
                                        System.out.println("SDFFS@$QQ");

                                    }
                                } else if (inBonds) {

                                    String[] splitted = line.split(" ");
                                  //  map.adjustBonds(new Point(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1])), new Point(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3])), Integer.parseInt(splitted[4]));

                                } else if (Simulation.debugMode)
                                    System.out.println("We don't have more sections.");

                            }

                        }
                        bre.close();
                       // canvas.repaint();
                        Simulation.configLoaded = true;
                        if (Simulation.configLoaded && Simulation.rulesLoaded) {
                            simStart.setEnabled(true);
                        }

                    }

                }

            } catch (Exception exc) {


            }
            System.out.println("Load config");
        }
        else if (e.getSource() == menuClear)
        {
            System.out.println("clear config");
        }
        else if (e.getSource() == menuQuit)
        {
            System.out.println("quit application");
            System.exit(0);
        }
        else if (e.getSource() == about)
        {
            System.out.println("about this application");
        }
        else if (e.getSource() == simStart)
        {
            System.out.println("start");
        }
        else if (e.getSource() == simStop)
        {
            System.out.println("stop");
        }
        else if (e.getSource() == simPause)
        {
            System.out.println("pause");
        }
    }

    private void drawMonomer(Monomer m) {
        nubotGFX.setColor(Color.BLACK);


       /*          Simulation.canvasXYoffset.x + m.getLocation().x * 2 * Simulation.monomerRadius + m.getLocation().y * Simulation.monomerRadius,
                   Simulation.canvasXYoffset.y + -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                   Simulation.monomerRadius*2,
                   Simulation.monomerRadius*2);*/
        Point xyPos = Simulation.getCanvasPosition(m.getLocation());
        int monomerWidthAdjustment =   Simulation.monomerRadius/8;
        int monomerWidth = Simulation.monomerRadius*2 - monomerWidthAdjustment;
        int monomerHeight = Simulation.monomerRadius*2;
        nubotGFX.fillOval(
                /*X coord*/   xyPos.x ,
                /*Y coord*/   xyPos.y ,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        nubotGFX.setColor(Color.WHITE);
        Rectangle2D bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext() );
        while(bounds.getWidth() < monomerWidth && bounds.getHeight() < monomerHeight)
        {
            nubotGFX.setFont(nubotGFX.getFont().deriveFont((float)++fontSize));
            bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext() );

        }
        while(bounds.getWidth() > monomerWidth || bounds.getHeight() > monomerHeight)
        {
            nubotGFX.setFont(nubotGFX.getFont().deriveFont((float)--fontSize));
            bounds = nubotGFX.getFont().getStringBounds(m.getState(), 0, m.getState().length(), nubotGFX.getFontRenderContext() );
        }
        nubotGFX.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + Simulation.monomerRadius - (int)bounds.getWidth()/2 - monomerWidthAdjustment  ,
                /*Y Coord */    xyPos.y + Simulation.monomerRadius + (int)bounds.getHeight()/3 );

    }
}