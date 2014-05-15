import org.javatuples.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Driver {
    Thread simHeartBeat;
    Runnable simRunnable;
    MainFrame mainFrame;
    Canvas simCanvas;
    NubotVideo nubotVideo;
    Configuration map = Configuration.getInstance();

    public Driver(final Dimension size)
    {
        Canvas.setSimInstance(new Canvas(size));
        simCanvas = Canvas.getSimInstance();
        mainFrame = new MainFrame(size,this );

        simRunnable = new Runnable() {
            @Override
            public void run() {


                simCanvas.repaint();
                JComponent yo;
                int frameRate = 60;
                if(Simulation.isRecording){
                    frameRate = nubotVideo.getFrameRate();
                }

                while (Simulation.isRunning) {
                    try {
                        if (!Simulation.isRecording)
                            Thread.sleep((long) (30 + Simulation.speedRate * 1000.0 * map.timeStep));


                        double t = map.computeTimeStep();


                        if (Simulation.animate) {
                            simCanvas.repaint();
                        }

                        if (Simulation.isRecording) {
                            double realt = t * nubRatio;
                            double fakeFloorRealT = realt - (realt % nubotVideo.getFrameDuration());

                            if(fakeFloorRealT - Simulation.lastr >= nubotVideo.getFrameDuration())
                            {

                                drawNubotVideoFrame(nubotVideo.getBFI(), "#Monomers: " + map.size() + "\nStep: " + map.markovStep + "\nTime: " + Double.toString(map.timeElapsed).substring(0, 6), new ArrayList<Monomer>(map.values()));
                                nubotVideo.encodeFrame((long)Math.round((fakeFloorRealT - Simulation.lastr) / nubotVideo.getFrameDuration() )   -1    );
                                System.out.println( (((fakeFloorRealT - Simulation.lastr) / nubotVideo.getFrameDuration() )  - 1 )  + "ffrt/gfd-1 ") ;
                                map.executeFrame();
                                nubotVideo.encodeFrame(1);
                                Simulation.lastr = fakeFloorRealT;




                                //get min and max frame draw points0
                                Pair<Point, Point> minMaxXY = Simulation.calculateMinMax(new ArrayList<Monomer> (map.values()), Simulation.monomerRadius, new Point(0, 0), nubotVideo.getRes());
                                //get the caculation dimension of the map
                                Dimension nubotDimension = new Dimension(minMaxXY.getValue1().x - minMaxXY.getValue0().x + Simulation.monomerRadius*2  , minMaxXY.getValue1().y - minMaxXY.getValue0().y + Simulation.monomerRadius*2);
                                //reduce monomer radius if it exceeds the video resolution
                                if(nubotDimension.width > nubotVideo.getResWidth() || nubotDimension.getHeight() > nubotVideo.getResHeight())
                                {
                                    if(Simulation.monomerRadius>1)
                                        Simulation.monomerRadius--;
                                    canvasStrokeSize = Simulation.monomerRadius / 3;
                                }
                                //re-calculate dimensions
                                minMaxXY = Simulation.calculateMinMax(new ArrayList<Monomer> (map.values()), Simulation.monomerRadius, new Point(0, 0), nubotVideo.getRes());
                                nubotDimension = new Dimension(minMaxXY.getValue1().x - minMaxXY.getValue0().x + Simulation.monomerRadius*2  , minMaxXY.getValue1().y - minMaxXY.getValue0().y + Simulation.monomerRadius*2);



                                if(!Simulation.agitationON)
                                {
                                    //translate the canvas xy offset left or up if there is a draw point outside the right and bottom bounderies
                                    Simulation.canvasXYoffset.translate(minMaxXY.getValue1().x + 2*Simulation.monomerRadius > nubotVideo.getResWidth() ? -minMaxXY.getValue1().x - 2*Simulation.monomerRadius  +  nubotVideo.getResWidth() -(nubotVideo.getResWidth()- nubotDimension.width )/2 : 0,  minMaxXY.getValue1().y + 2*Simulation.monomerRadius > nubotVideo.getResHeight() ? minMaxXY.getValue1().y + 2*Simulation.monomerRadius - nubotVideo.getResHeight()  + (nubotVideo.getResHeight()- nubotDimension.height )/2 : 0);

                                    //translate right and down if minimum draw points are outside
                                    Simulation.canvasXYoffset.translate(minMaxXY.getValue0().x < 0 ? Math.abs(minMaxXY.getValue0().x) + (nubotVideo.getResWidth() - minMaxXY.getValue1().x) /2  : 0 , minMaxXY.getValue0().y < 0 ? -Math.abs(minMaxXY.getValue0().y) - (nubotVideo.getResHeight() - minMaxXY.getValue1().y)/2  : 0  );
                                }
                                else
                                {

                                       /* Simulation.canvasXYoffset.translate(minMaxXY.getValue1().x + Simulation.monomerRadius*2 > nubotVideo.getResWidth() ? -(minMaxXY.getValue0().x)/2 : 0, minMaxXY.getValue1().y + Simulation.monomerRadius*2 > nubotVideo.getResHeight() ? (minMaxXY.getValue0().y)/2 : 0);
                                         Simulation.canvasXYoffset.translate(minMaxXY.getValue0().x < 0 ? (nubotDimension.width - minMaxXY.getValue1().x)/2 : 0, minMaxXY.getValue0().y < 0 ? -(nubotVideo.getResHeight() - minMaxXY.getValue1().y)/2 : 0 );    */

                                }

                            }
                            else
                            {
                                map.executeFrame();

                            }





                        }

                        statusSimulation.setText("Simulating...");
                        totalTime += map.executeTime;
                        statusMonomerNumber.setText("Monomers: " + map.getSize());
                        statusTime.setText("Time: " + map.timeElapsed);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                try
                {

                }
                catch(Exception e)
                {
                    System.out.println("exception closing");
                }

                statusSimulation.setText("Simulation finished ");
                if (map.isFinished)
                {
                    drawNubotVideoFrame(nubotVideo.getBFI(), "#Monomers: " + map.size() + "\nStep: " + map.markovStep + "\nTime: " + Double.toString(map.timeElapsed).substring(0, 6), new ArrayList<Monomer>(map.values()));
                    nubotVideo.encodeFrame(1);
                    nubotVideo.finish();


                    JOptionPane.showMessageDialog(canvas, "No more rules can be applied!", "Finished", JOptionPane.OK_OPTION);
                }
            }
        };
    }

    public void simStart()
    {
        simStop();
        simHeartBeat = new Thread(simRunnable);
        simHeartBeat.start();
    }
    public void simStop()
    {
        if(simHeartBeat !=null && simHeartBeat.isAlive())
            simHeartBeat.interrupt();
    }






}
