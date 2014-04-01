//
// Simulation.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//


import java.awt.*;

public class Simulation
{
    public static Dimension frameSize = new Dimension(800, 600);
    public static Dimension canvasSize = new Dimension(800,600);
    public static Point canvasXYoffset = new Point(400,300);
    public static int monomerRadius = 600/60;
    public static boolean configLoaded = false;
    public static boolean rulesLoaded = false;
    public static boolean debugMode  = false;


    public static Point getCanvasPosition(Point gridPosition)
    {
       return new Point(canvasXYoffset.x +gridPosition.x * 2 * monomerRadius + gridPosition.y * monomerRadius - monomerRadius,   canvasXYoffset.y +  gridPosition.y * 2 * monomerRadius  - monomerRadius);


    }
}
