import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NubotDrawer {
    public synchronized void drawBond(Monomer m, Graphics2D g, int monomerRadius, Point offset ) {

        Monomer tempMon = new Monomer(m);
        if (tempMon.hasBonds()) {
            ArrayList<Byte> rigidDirList = tempMon.getDirsByBondType(Bond.TYPE_RIGID);
            ArrayList<Byte> flexibleDirList = tempMon.getDirsByBondType(Bond.TYPE_FLEXIBLE);

            g.setColor(Color.RED);
            for (Byte dir : rigidDirList) {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
            }

            for (Byte dir : flexibleDirList) {
                Point start = Simulation.getCanvasPosition(tempMon.getLocation());
                Point end = Simulation.getCanvasPosition(Direction.getNeighborPosition(tempMon.getLocation(), dir));
                start.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                end.translate(Simulation.monomerRadius, Simulation.monomerRadius);
                g.setStroke(new BasicStroke(canvasStrokeSize * 1.20f));
                g.setColor(Color.RED);
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(canvasStrokeSize * .80f));
                g.draw(new Line2D.Float(start.x - Simulation.monomerRadius / 3.5f, start.y, end.x, end.y));
            }
        }
    }

    public synchronized void drawMonomers(Graphics g, ArrayList<Monomer> monList, int monomerRadius, Point offset) {


        Monomer[] mapTemp = new Monomer[monList.size()];
        monList.toArray(mapTemp);

        ArrayList<Monomer> tempMonList = new ArrayList<Monomer>();

        for (Monomer m : mapTemp) {
            if(Simulation.monomerRadius >=7)
                drawBond(m, (Graphics2D) g);
            tempMonList.add(new Monomer(m));

        }


        for (Monomer m : tempMonList) {

            drawMonomer(m, (Graphics2D) g);
        }


    }
    public synchronized void drawMonomer(Monomer m, Graphics2D g) {

        Point xyPos = Simulation.getCanvasPosition(m.getLocation());

        int monomerWidthAdjustment = Simulation.monomerRadius / 4;
        int monomerWidth = Simulation.monomerRadius * 2 - monomerWidthAdjustment;
        int monomerHeight = Simulation.monomerRadius * 2;
        g.setColor(Color.WHITE);
        g.fillOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setStroke(new BasicStroke(Simulation.monomerRadius / 10));
        g.setColor(new Color(2, 180, 206));
        g.drawOval(
                /*X coord*/   xyPos.x,
                /*Y coord*/   xyPos.y,//  -1* (m.getLocation().y * (int)(Math.sqrt(3) * Simulation.monomerRadius)),
                /*Width  */   monomerWidth,
                /*Height */   monomerHeight);
        g.setColor(Color.white);
        Rectangle2D bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        while (bounds.getWidth() < monomerWidth - 4 && bounds.getHeight() < monomerHeight - 4) {
            g.setFont(g.getFont().deriveFont((float) ++fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        while (bounds.getWidth() > monomerWidth - 4 || bounds.getHeight() > monomerHeight - 4) {
            g.setFont(g.getFont().deriveFont((float) --fontSize));
            bounds = g.getFont().getStringBounds(m.getState(), 0, m.getState().length(), g.getFontRenderContext());
        }
        g.setColor(Color.BLACK);
        g.drawString(
                /*String */     m.getState(),
                /*X Coord */    xyPos.x + Simulation.monomerRadius - (int) bounds.getWidth() / 2 - monomerWidthAdjustment / 2,
                /*Y Coord */    xyPos.y + Simulation.monomerRadius + (int) (bounds.getHeight() / 3.5));
    }

    public void drawNubotVideoFrame(BufferedImage bfi, String topLeftString, ArrayList<Monomer> monList)
    {

        int width = bfi.getWidth();
        int height = bfi.getHeight();
        Graphics2D gfx = (Graphics2D)bfi.getGraphics();

        gfx.setColor(Color.white);
        gfx.fillRect(0,0,width,height);
        gfx.setColor(Color.BLACK);
        int lc = 1;
        for(String line : topLeftString.split("\n"))
        {
            Rectangle2D strDim = gfx.getFont().getStringBounds(line, 0, line.length(), gfx.getFontRenderContext());
            gfx.drawString(line, 20, 40+(int)strDim.getHeight()*lc++);
        }

        try
        {

            drawMonomers(gfx, monList);

        }
        catch (Exception e)
        {

            System.out.println(e.getMessage());
        }

    }


}
