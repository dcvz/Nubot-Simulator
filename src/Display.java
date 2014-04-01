//
// Display.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Display implements ActionListener
{
    final JFrame mainFrame = new JFrame("Nubot Simulator");

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

    public Display()
    {
        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(Simulation.windowSize.width, Simulation.windowSize.height);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setVisible(true);

        initMenuBar();

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        loadR.addActionListener(this);
        about.addActionListener(this);
        loadC.addActionListener(this);
        menuClear.addActionListener(this);
        menuQuit.addActionListener(this);

        menuBar.add(file);
        menuBar.add(simulation);
        menuBar.add(settings);
        menuBar.add(help);
        menuBar.add(about);
        file.add(loadR);
        file.add(loadC);
        file.add(menuClear);
        file.add(menuQuit);


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
            System.out.println("Load config");
        }
        else if (e.getSource() == menuClear)
        {
            System.out.println("clear config");
        }
        else if (e.getSource() == menuQuit)
        {
            System.out.println("quit application");
        }
        else if (e.getSource() == about)
        {
            System.out.println("about this application");
        }
    }
}