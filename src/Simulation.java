//
// Simulation.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//



import org.javatuples.Pair;

import java.awt.*;
import java.util.ArrayList;
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
    public static boolean isRecording = false;
    public static double recordingLength = 0;
    public static boolean animate = true;

    public static double agitationRate = 0.0;


    public static Point getCanvasPosition(Point gridPosition)
    {
        return new Point(canvasXYoffset.x +gridPosition.x * 2 * monomerRadius + gridPosition.y * monomerRadius - monomerRadius,  -1 * (canvasXYoffset.y +  gridPosition.y * 2 * monomerRadius  - monomerRadius));
    }
    public static Point getCanvasToGridPosition(Point canvasPosition)
    {
        int gridY =  (int)Math.ceil ((-canvasPosition.y - canvasXYoffset.y + monomerRadius) / (2.0*monomerRadius));
        int gridX = (int)Math.ceil((canvasPosition.x - canvasXYoffset.x + monomerRadius - (gridY*monomerRadius) ) / (2.0*monomerRadius))  -1; // - 2*(gridY);

        return new Point(gridX, gridY);
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
    public static int caclulateProperRadiusMutateOffset(ArrayList<Monomer> ap, int radius, Point mutOffset,  Dimension imgSize) {

         Pair<Point, Point> minMaxPair = calculateMinMax(ap, radius, mutOffset, imgSize);
         Point minXY = minMaxPair.getValue0();
         Point maxXY = minMaxPair.getValue1();
         Dimension nubotDimension = calculateNubotDimension(ap, radius, mutOffset, imgSize);
        int numMonsRadius = (int)Math.ceil(Math.max(nubotDimension.width, nubotDimension.height) / radius);


        if (nubotDimension.width > imgSize.width || nubotDimension.height> imgSize.height) {
            if (nubotDimension.width > imgSize.width) {



                radius = (int)Math.floor(imgSize.width / numMonsRadius);
                mutOffset.translate(radius , 0);


            }
            if (nubotDimension.height > imgSize.height) {

                radius = (int)Math.floor(imgSize.height / numMonsRadius);
                mutOffset.translate(0, radius*2);

            }
        } else {
            if (minXY.x < 0) {
                mutOffset.translate(Math.abs(minXY.x) + radius, 0);
            }
            if (minXY.y < 0) {

                System.out.println("hit here minY" );
                mutOffset.translate(0, -1* minXY.y);
            }


            if (maxXY.x + radius * 2.5 > imgSize.width) {
                mutOffset.translate(-1 * Math.abs(imgSize.width - (maxXY.x + radius * 2)), 0);
            }
            if (maxXY.y + radius * 2.5 > imgSize.height) {
                System.out.println("hit here maxY");
                mutOffset.translate(0, -1 * (imgSize.height - (maxXY.y + radius * 2)));

            }
        }

        return radius;

    }


    public static Dimension calculateNubotDimension(ArrayList<Monomer> monList, int radius, Point mutOffset, Dimension bounds )
    {

        Pair<Point, Point> minMaxPair = calculateMinMax(monList, radius,mutOffset, bounds );
        Point minXY = minMaxPair.getValue0();
        Point maxXY = minMaxPair.getValue1();

        int nubotWidth = maxXY.x - minXY.x + radius * 2;
        int nubotHeight = maxXY.y - minXY.y + radius * 2;



        return new Dimension(nubotWidth, nubotHeight);
    }
    public static Pair<Point, Point> calculateMinMax(ArrayList<Monomer> monList, int radius, Point mutOffset, Dimension bounds)
    {


        Point maxXY = new Point(0, 0);
        Point minXY = new Point(bounds.width/2, bounds.height/2);

        for (Monomer m : monList) {
            Point gridLocation = m.getLocation();
            Point pixelLocation = Simulation.getCanvasPosition(gridLocation, mutOffset, radius);
            maxXY.x = Math.max(maxXY.x, pixelLocation.x);
            minXY.x = Math.min(minXY.x, pixelLocation.x);
            maxXY.y = Math.max(maxXY.y, pixelLocation.y);
            minXY.y = Math.min(minXY.y, pixelLocation.y);

        }


            return Pair.with(minXY, maxXY);
    }



}