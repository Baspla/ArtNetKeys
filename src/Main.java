import artnet4j.*;
import artnet4j.events.*;
import artnet4j.packets.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;

public class Main {
    private JFrame frame;
    private Robot r;
    private ArtNetServer artNetServer;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        startArtNetServer();
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            frame = new JFrame("ArtNetKeys");
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.addWindowStateListener(e -> {
                if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
                    artNetServer.stop();
                    System.exit(0);
                }
            });

            frame.setMinimumSize(new Dimension(220, 50));
            frame.setVisible(true);
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon;
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Beenden");

        exitItem.addActionListener(e -> {
            artNetServer.stop();
            System.exit(0);
        });
        popup.add(exitItem);
        try {
            trayIcon = new TrayIcon(ImageIO.read(Main.class.getResource("icon.png")));
            trayIcon.setPopupMenu(popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("ArtNetKeys");
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.println("TrayIcon could not be added.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ArtNet DMX Stuff
    void startArtNetServer() {
        artNetServer = new ArtNetServer();
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
                            if (dmxdata[0] != 0)
                                click(96 + dmxdata[0]);
                        }
                        if (dmxdata.length >= 2) {
                            if (dmxdata[1] != 0)
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
        System.out.println("Taste " + key + " gedrueckt.");
    }
}