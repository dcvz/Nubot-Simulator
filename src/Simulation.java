//
// Simulation.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//


import java.awt.*;
import java.util.Random;

public class
        Simulation
{
    public static Point canvasXYoffset;
    public static volatile
    int monomerRadius = 15;
    public static boolean configLoaded = false;
    public static boolean rulesLoaded = false;
    public static boolean debugMode  = false;
    public static boolean isPaused = false;
    public static boolean isRunning = false;
    public static boolean agitationON = false;
    public static boolean isRecording = true;
    public static double recordingLength = 100;

    public static double agitationRate = 0.0;

    public static Point getCanvasPosition(Point gridPosition)
    {
        return new Point(canvasXYoffset.x +gridPosition.x * 2 * monomerRadius + gridPosition.y * monomerRadius - monomerRadius,  -1 * (canvasXYoffset.y +  gridPosition.y * 2 * monomerRadius  - monomerRadius));
    }
    public static Point getCanvasPosition(Point gridPosition, Point xyOffset,int radius)
    {
        return new Point(xyOffset.x +gridPosition.x * 2 * radius + gridPosition.y * radius - radius,  -1 * (xyOffset.y +  gridPosition.y * 2 * radius  - radius));
    }

    public static double calculateExpDistribution(int i)
    {
        Random rand = new Random();
        double randNum = rand.nextDouble();

        while (randNum == 0.0)
            randNum = rand.nextDouble();

        return (-1 * Math.log(randNum)) / i;
    }


}