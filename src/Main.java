import artnet4j.*;
import artnet4j.events.*;
import artnet4j.packets.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.SocketException;

public class Main {
    private Robot r;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    //ArtNet DMX Stuff
    void startArtNetServer() {
        ArtNetServer artNetServer = new ArtNetServer();
        try {
            artNetServer.start();
            artNetServer.addListener(new ArtNetServerListener() {
                @Override
                public void artNetPacketBroadcasted(ArtNetPacket artNetPacket) {

                }

                @Override
                public void artNetPacketReceived(ArtNetPacket artNetPacket) {
                    if (artNetPacket instanceof ArtDmxPacket) {
                        ArtDmxPacket artDmxPacket = (ArtDmxPacket) artNetPacket;
                        byte[] dmxdata = artDmxPacket.getDmxData();
                        System.out.println("Daten empfangen. SIZE: " + dmxdata.length);
                        if (dmxdata.length >= 1) {
                            if(dmxdata[0]!=0)
                            click(96+dmxdata[0]);
                        }
                        if (dmxdata.length >= 2) {
                            if(dmxdata[1]!=0)
                            click(dmxdata[1]);
                        }
                    }
                }

                @Override
                public void artNetPacketUnicasted(ArtNetPacket artNetPacket) {
                }

                @Override
                public void artNetServerStarted(ArtNetServer artNetServer) {
                }

                @Override
                public void artNetServerStopped(ArtNetServer artNetServer) {

                }
            });

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (ArtNetException e) {
            e.printStackTrace();
        }
    }

    private void click(int key) {
        r.keyPress(key);
        r.keyRelease(key);
        System.out.println("Taste "+key+" gedrueckt.");
    }
}