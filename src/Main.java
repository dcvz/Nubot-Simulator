//
// Main.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String args[]) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        final Dimension windowSize = new Dimension((int) (dim.width * .9), (int) (dim.height * .9));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Driver mainWindow = new Driver(windowSize);
            }
        });


    }
}
