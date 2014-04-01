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

    private JMenuItem loadR = new JMenuItem("Load Rules");

    //  WindowMain default constructor
    public Display()
    {
        mainFrame.setBackground(Color.WHITE);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setSize(Simulation.windowSize.getValue0(), Simulation.windowSize.getValue1());
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setVisible(true);

        initMenuBar();

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // create all main menus
        JMenu file = new JMenu("File");
        JMenu simulation = new JMenu("Simulation");
        JMenu settings = new JMenu("Settings");
        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");

        // create sub-menus for each menu
        loadR.addActionListener(this);

        menuBar.add(file);
        file.add(loadR);

        mainFrame.setJMenuBar(menuBar);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loadR)
        {
            System.out.println("Hello");
        }
    }
}