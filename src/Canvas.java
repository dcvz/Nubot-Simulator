import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Canvas  extends JComponent {

    Configuration map = Configuration.getInstance();
    //Singleton
    private static Canvas simInstance;
    public static Canvas getSimInstance()
    {
        if(simInstance == null)
        {
            System.out.println("Canvas.java: simInstance is null.");
            return null;
        }
        return simInstance;
    }
    public static void setSimInstance(Canvas canvas)
    {
        simInstance = canvas;
    }
    private Point XYDrawOffset = new Point(0,0);
    private BufferedImage canvasBFI;
    private BufferedImage hudBFI;
    private Graphics2D hudGFX;
    private Graphics2D canvasGFX;
    private Dimension canvasDimension = new Dimension(0,0);

    public Canvas(Dimension size)
    {
        setSize(size);
        canvasDimension.setSize(size);
        init();

    }
    public void render()
    {

    }
    public void init()
    {
        canvasBFI = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_ARGB);
        canvasGFX = (Graphics2D)canvasBFI.getGraphics();
        canvasGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        XYDrawOffset.setLocation(canvasDimension.width/2, -canvasDimension.height/2);
        //for the nubot graphics/image & visuals
        hudBFI = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_ARGB);
        hudGFX = (Graphics2D)hudBFI.getGraphics();
        hudGFX.setColor(Color.BLACK);
        hudGFX.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hudGFX.setFont(new Font("TimesRoman", Font.BOLD, 20));

    }
    @Override
    public void paintComponent(Graphics g)
    {
        g.drawImage(canvasBFI, 0,0, null);
        g.drawImage(hudBFI, 0, 0, null);

;   }
    public void showToast(int x, int y, String text, int duration)
    {
        clearGraphics(hudGFX);
        hudGFX.drawString(text, x, y);
        repaint();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable clearBFI = new Runnable() {
            @Override
            public void run() {
                clearGraphics(hudGFX);
                repaint();



            }
        } ;


        executor.schedule(clearBFI, duration, TimeUnit.MILLISECONDS);

    }
    private void clearGraphics( Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, canvasDimension.width, canvasDimension.height);
        g2.setComposite(AlphaComposite.SrcOver);
    }

}
