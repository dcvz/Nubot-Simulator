//
// Direction.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//


import java.awt.*;

public class Direction
{
    static byte TYPE_FLAG_EAST = 1;
    static byte TYPE_FLAG_WEST = 2;
    static byte TYPE_FLAG_NORTHEAST = 4;
    static byte TYPE_FLAG_NORTHWEST = 8;
    static byte TYPE_FLAG_SOUTHEAST = 16;
    static byte TYPE_FLAG_SOUTHWEST = 32;
    static byte DIRECTION_TYPE_FLAG_MASK = 63;
    static Point TYPE_POINT_OFFSET_EAST = new Point(1,0);
    static Point TYPE_POINT_OFFSET_WEST = new Point(-1,0);
    static Point  TYPE_POINT_OFFSET_NORTHEAST = new Point(0,1);
    static Point TYPE_POINT_OFFSET_NORTHWEST = new Point(-1, 1);
    static Point TYPE_POINT_OFFSET_SOUTHEAST = new Point(1, -1);
    static Point TYPE_POINT_OFFSET_SOUTHWEST = new Point(0, -1);


    static Point getNeighborPosition(Point /*origin Monomer*/ start, byte /*direction*/direction)
    {
        if(direction == TYPE_FLAG_EAST)
            return new Point(start.x + TYPE_POINT_OFFSET_EAST.x, start.y + TYPE_POINT_OFFSET_EAST.y);
        if(direction == TYPE_FLAG_WEST)
            return new Point(start.x + TYPE_POINT_OFFSET_WEST.x, start.y + TYPE_POINT_OFFSET_WEST.y);
        if(direction == TYPE_FLAG_SOUTHWEST)
            return new Point(start.x + TYPE_POINT_OFFSET_SOUTHWEST.x, start.y + TYPE_POINT_OFFSET_SOUTHWEST.y);
        if(direction == TYPE_FLAG_NORTHWEST)
            return new Point(start.x + TYPE_POINT_OFFSET_NORTHWEST.x, start.y + TYPE_POINT_OFFSET_NORTHWEST.y);
        if(direction == TYPE_FLAG_NORTHEAST)
            return new Point(start.x + TYPE_POINT_OFFSET_NORTHEAST.x, start.y + TYPE_POINT_OFFSET_NORTHEAST.y);
        if(direction == TYPE_FLAG_SOUTHEAST)
            return new Point(start.x + TYPE_POINT_OFFSET_SOUTHEAST.x, start.y + TYPE_POINT_OFFSET_SOUTHEAST.y);
        return start;

    }

}

