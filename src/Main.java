//
// Main.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import javax.swing.*;

public class Main
{
    public static void main(String args[])
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Display mainWindow = new Display();
            }
        });

    }
}
