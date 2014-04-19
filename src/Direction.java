//
// Direction.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//


import java.awt.*;

public class Direction {
    static final Byte TYPE_FLAG_EAST = 1;
    static final Byte TYPE_FLAG_WEST = 2;
    static final Byte TYPE_FLAG_NORTHEAST = 4;
    static final Byte TYPE_FLAG_NORTHWEST = 8;
    static final Byte TYPE_FLAG_SOUTHEAST = 16;
    static final Byte TYPE_FLAG_SOUTHWEST = 32;
    static final Point TYPE_POINT_OFFSET_EAST = new Point(1, 0);
    static final Point TYPE_POINT_OFFSET_WEST = new Point(-1, 0);
    static final Point TYPE_POINT_OFFSET_NORTHEAST = new Point(0, 1);
    static final Point TYPE_POINT_OFFSET_NORTHWEST = new Point(-1, 1);
    static final Point TYPE_POINT_OFFSET_SOUTHEAST = new Point(1, -1);
    static final Point TYPE_POINT_OFFSET_SOUTHWEST = new Point(0, -1);


    public static Point getNeighborPosition(Point /*origin Monomer*/ start, Byte /*direction*/direction) {
        if (direction.equals(TYPE_FLAG_EAST))
            return new Point(start.x + TYPE_POINT_OFFSET_EAST.x, start.y + TYPE_POINT_OFFSET_EAST.y);
        if (direction.equals(TYPE_FLAG_WEST))
            return new Point(start.x + TYPE_POINT_OFFSET_WEST.x, start.y + TYPE_POINT_OFFSET_WEST.y);
        if (direction.equals(TYPE_FLAG_SOUTHWEST))
            return new Point(start.x + TYPE_POINT_OFFSET_SOUTHWEST.x, start.y + TYPE_POINT_OFFSET_SOUTHWEST.y);
        if (direction.equals(TYPE_FLAG_NORTHWEST))
            return new Point(start.x + TYPE_POINT_OFFSET_NORTHWEST.x, start.y + TYPE_POINT_OFFSET_NORTHWEST.y);
        if (direction.equals(TYPE_FLAG_NORTHEAST))
            return new Point(start.x + TYPE_POINT_OFFSET_NORTHEAST.x, start.y + TYPE_POINT_OFFSET_NORTHEAST.y);
        if (direction.equals(TYPE_FLAG_SOUTHEAST))
            return new Point(start.x + TYPE_POINT_OFFSET_SOUTHEAST.x, start.y + TYPE_POINT_OFFSET_SOUTHEAST.y);
        return start;
    }

    public static byte dirFromPoints(Point ORIGIN_monomerPoint1, Point NEIGHBOR_monomerPoint2) {
        return pointOffsetToDirByte(new Point(NEIGHBOR_monomerPoint2.x - ORIGIN_monomerPoint1.x, NEIGHBOR_monomerPoint2.y - ORIGIN_monomerPoint1.y));
    }

    public static Point dirToPointOffset(byte direction) {
        if (TYPE_FLAG_EAST.equals(direction))
            return TYPE_POINT_OFFSET_EAST;
        if (TYPE_FLAG_NORTHEAST.equals(direction))
            return TYPE_POINT_OFFSET_NORTHEAST;
        if (TYPE_FLAG_SOUTHEAST.equals(direction))
            return TYPE_POINT_OFFSET_SOUTHEAST;
        if (TYPE_FLAG_NORTHWEST.equals(direction))
            return TYPE_POINT_OFFSET_NORTHWEST;
        if (TYPE_FLAG_SOUTHWEST.equals(direction))
            return TYPE_POINT_OFFSET_SOUTHWEST;
        if (TYPE_FLAG_WEST.equals(direction))
            return TYPE_POINT_OFFSET_WEST;

        return new Point(0, 0);
    }

    public static Byte getOppositeDir(byte directon) {
        return (pointOffsetToDirByte(new Point(dirToPointOffset(directon).x * -1, dirToPointOffset(directon).y * -1)));
    }

    public static Point translatedPointByDir(Point point, byte dir) {
        return new Point(point.x + dirToPointOffset(dir).x, point.y + dirToPointOffset(dir).y);
    }


    public static byte deltaFromDirs(byte dir1, byte dir2) {
        return pointOffsetToDirByte(new Point(dirToPointOffset(dir2).x - dirToPointOffset(dir1).x, dirToPointOffset(dir2).y - dirToPointOffset(dir1).y));
    }

    public static byte pointOffsetToDirByte(Point off) {
        if (off.equals(TYPE_POINT_OFFSET_EAST))
            return TYPE_FLAG_EAST;
        if (off.equals(TYPE_POINT_OFFSET_WEST))
            return TYPE_FLAG_WEST;
        if (off.equals(TYPE_POINT_OFFSET_NORTHEAST))
            return TYPE_FLAG_NORTHEAST;
        if (off.equals(TYPE_POINT_OFFSET_NORTHWEST))
            return TYPE_FLAG_NORTHWEST;
        if (off.equals(TYPE_POINT_OFFSET_SOUTHWEST))
            return TYPE_FLAG_SOUTHWEST;
        if (off.equals(TYPE_POINT_OFFSET_SOUTHEAST))
            return TYPE_FLAG_SOUTHEAST;
        return 0;

    }

    public static byte dirFromPosInts(int x1, int y1, int x2, int y2) {
        return pointOffsetToDirByte(new Point(x2 - x1, y2 - y1));
    }

    static byte stringToFlag(String dir) {
        if (dir.matches("E"))
            return TYPE_FLAG_EAST;
        if (dir.matches("W"))
            return TYPE_FLAG_WEST;
        if (dir.matches("NE"))
            return TYPE_FLAG_NORTHEAST;
        if (dir.matches("NW"))
            return TYPE_FLAG_NORTHWEST;
        if (dir.matches("SE"))
            return TYPE_FLAG_SOUTHEAST;
        if (dir.matches("SW"))
            return TYPE_FLAG_SOUTHWEST;

        System.out.println("Improper direction string format.");
        return 0;
    }
}

