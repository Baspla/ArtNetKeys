import artnet4j.ArtNetException;
import artnet4j.ArtNetServer;
import artnet4j.events.ArtNetServerListener;
import artnet4j.packets.ArtDmxPacket;
import artnet4j.packets.ArtNetPacket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;

//Ab 112 F1,F2,F3...
//Ab 65 A,B,C,...
//KeyEvent.VK_ ...

public class Main {
    private Robot r;
    private ArtNetServer artNetServer;
    private int oldkey;
    private int oldkey2;
    private int oldkey3;
    private int start;

    public static void main(String[] args) {
        System.out.println("http://www.kbdedit.com/manual/low_level_vk_list.html");
        int i = 0;
        if (args.length >= 1) {
            try {
                i = new Integer(args[0]);
                if(i>508){
                    System.out.println("Whoah! Nur bis 508 (+4 >> 512).");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            new Main(i);
        }
    }

    private Main(int i) {
        start = i;
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        startArtNetServer();
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            JFrame frame = new JFrame("ArtNetKeys");
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

    private void startArtNetServer() {
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
                        int d0 = Byte.toUnsignedInt(dmxdata[start]);
                        int d1 = Byte.toUnsignedInt(dmxdata[start + 1]);
                        int d2 = Byte.toUnsignedInt(dmxdata[start + 2]);
                        int d3 = Byte.toUnsignedInt(dmxdata[start + 3]);
                        int d4 = d2 + d3;
                        if (oldkey != d0) {
                            if (d0 != 0) {
                                click(64 + ((d0 - 1) % 26) + 1);
                            }
                            oldkey = d0;
                        }
                        if (oldkey2 != d1) {
                            if (d1 != 0) {
                                click(d1);
                            }
                            oldkey2 = d1;
                        }
                        if (oldkey3 != d4) {
                            if (d4 != 0) {
                                click(d4);
                            }
                            oldkey3 = d4;
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
        } catch (SocketException | ArtNetException e) {
            e.printStackTrace();
        }
    }

    private void click(int key) {
        try {
            System.out.println("Taste " + key + " gedrueckt. (" + (char) key + ")");
            r.keyPress(key);
            r.keyRelease(key);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}