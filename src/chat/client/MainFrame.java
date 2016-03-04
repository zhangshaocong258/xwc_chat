package chat.client;

/**
 * Created by zsc on 2015/10/3.
 */


import java.awt.AWTException;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class MainFrame {
    public static boolean WarningFlag = true;// 是否需要消息提示的標志
    public static boolean ChatRecordSaveFlag = true;// 是否需要保存聊天记录的標志

    public static void main(String[] args) {

        // System.out.println(System.getProperty("user.dir"));

        try {
            JFrame.setDefaultLookAndFeelDecorated(true);// com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");// javax.swing.plaf.metal.MetalLookAndFeel
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        String path = "chat/img/chat.jpg";
        URL url = MainFrame.class.getClassLoader().getResource(path);
        Image icon = Toolkit.getDefaultToolkit().getImage(url);
        TrayIcon trayIcon = new TrayIcon(icon, "群组聊天器_XWC");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int width = toolkit.getScreenSize().width;
        int height = toolkit.getScreenSize().height;
        int lx = (width - 480) / 2;
        int ly = (height - 455) / 2;

        final JFrame frame = new JFrame();
        frame.setLayout(null);
        frame.setBounds(new Rectangle(lx, ly, 480, 455));
        final DataPanel dataPanel = new DataPanel(trayIcon, frame);
        frame.add(dataPanel);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMaximizedBounds(new Rectangle(lx, ly, 480, 455));
        frame.setTitle("群组聊天器");
        frame.setState(Frame.ICONIFIED);
        frame.setVisible(false);
        frame.addWindowListener(new WinCloseEvent(dataPanel, frame));

        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (frame.isVisible()) {
                    if (frame.getState() == Frame.ICONIFIED) {
                        frame.setState(Frame.NORMAL);
                    } else {
                        frame.setVisible(false);
                    }
                } else {
                    frame.setVisible(true);
                    frame.setState(Frame.NORMAL);
                }
            }
        });
        if (SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

        final MenuItem itemExit = new MenuItem("Exit Application");
        itemExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
//                if (JOptionPane.showConfirmDialog(frame, "您确定要关闭吗?", "警告!",
//                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                    dataPanel.close();
//                    System.exit(0);
//                }
                dataPanel.close();
                System.exit(0);
            }
        });

        final MenuItem itemWarning = new MenuItem("Close Message Warning");
        itemWarning.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (itemWarning.getLabel().equals("Close Message Warning")) {
                    itemWarning.setLabel("Open Message Warning");
                    MainFrame.WarningFlag = false;
                } else {
                    itemWarning.setLabel("Close Message Warning");
                    MainFrame.WarningFlag = true;
                }
            }
        });
        final MenuItem itemChatRecord = new MenuItem("Unsave Chat Record");
        itemChatRecord.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (itemChatRecord.getLabel().equals("Unsave Chat Record")) {
                    itemChatRecord.setLabel("Save Chat Record");
                    MainFrame.ChatRecordSaveFlag = false;
                } else {
                    itemChatRecord.setLabel("Unsave Chat Record");
                    MainFrame.ChatRecordSaveFlag = true;
                }
            }
        });
        final MenuItem itemOpenChatRecord = new MenuItem("Open Chat Record");
        itemOpenChatRecord.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ChatRecordMgr("").openChatRecord();
            }
        });
        final MenuItem itemReback = new MenuItem("Resume Default setting");
        itemReback.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dataPanel.RebackDefault();
            }
        });

        PopupMenu menu = new PopupMenu();
        menu.add(itemWarning);
        menu.addSeparator();
        menu.add(itemChatRecord);
        menu.addSeparator();
        menu.add(itemOpenChatRecord);
        menu.addSeparator();
        menu.add(itemReback);
        menu.addSeparator();
        menu.add(itemExit);
        trayIcon.setPopupMenu(menu);
        dataPanel.getBtnConnect().doClick();// online

        frame.addMouseListener(new MouseAdapter() {
            public void mouseEntered(final MouseEvent e) {
                Point loc = frame.getLocation();
                if (loc.y == (5 - frame.getHeight())) {
                    frame.setLocation(frame.getX(), 0);
                }
            }

            public void mouseExited(final MouseEvent e) {
                if (e.getX() <= 0 || e.getX() >= frame.getWidth() || e.getY() <= 0
                        || e.getY() >= frame.getHeight()) {

                    Point loc = frame.getLocation();
                    if (loc.y <= 0) {
                        frame.setLocation(frame.getX(), 5 - frame.getHeight());
                    }
                }
            }
        });
    }

    private static class WinCloseEvent extends WindowAdapter {

        private DataPanel dataPanel;
        private JFrame frame;

        public WinCloseEvent(DataPanel dataPanel, JFrame frame) {
            this.dataPanel = dataPanel;
            this.frame = frame;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setState(Frame.ICONIFIED);
            frame.setVisible(false);
        }

    }
}
