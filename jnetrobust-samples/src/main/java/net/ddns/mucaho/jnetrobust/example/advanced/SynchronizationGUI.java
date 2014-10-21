/*
 * Any copyright is dedicated to the Public Domain.
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package net.ddns.mucaho.jnetrobust.example.advanced;

import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.HOST;
import net.ddns.mucaho.jnetrobust.example.advanced.SynchronizationMain.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


/**
 * @author Matthew D. Hicks
 * @author mucaho
 */
public class SynchronizationGUI extends JFrame implements KeyListener {
    private JPanel windowPanel;
    private HOST localHost;
    private Map<HOST, JPanel> objects =
            new EnumMap<HOST, JPanel>(HOST.class);

    public SynchronizationGUI(HOST type) {
        localHost = type;

        setTitle(type.toString());
        setSize(400, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(this);

        windowPanel = new JPanel();
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(BorderLayout.CENTER, windowPanel);
        windowPanel.setLayout(null);
        windowPanel.addKeyListener(this);

        objects.put(HOST.CLIENTA, createPanel(0, 0, Color.RED));
        objects.put(HOST.CLIENTB, createPanel(300, 0, Color.BLUE));
        objects.put(HOST.SERVER, createPanel(150, 300, Color.GREEN));

        if (type == HOST.SERVER) {
            setLocation(200, 410);
        } else if (type == HOST.CLIENTA) {
            setLocation(0, 0);
        } else if (type == HOST.CLIENTB) {
            setLocation(410, 0);
        }
        windowPanel.repaint();
    }

    private JPanel createPanel(int x, int y, Color color) {
        JPanel panel = new JPanel();
        panel.addKeyListener(this);
        panel.setBounds(x, y, 50, 50);
        panel.setBackground(color);
        panel.setVisible(true);
        windowPanel.add(panel);
        return panel;
    }

    protected JPanel getHostObject() {
        return objects.get(localHost);
    }

    protected JPanel getRemoteObject(HOST host) {
        return objects.get(host);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        int x = 0;
        int y = 0;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            x -= 5;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            x += 5;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            y -= 5;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            y += 5;
        }

        JPanel hostObject = getHostObject();
        hostObject.setLocation(hostObject.getX() + x, hostObject.getY() + y);
    }

    void sendGUI(Vector2D data) throws IOException {
        JPanel hostObject = getHostObject();
        data.setX(hostObject.getX());
        data.setY(hostObject.getY());
    }

    void updateGUI(final Vector2D data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel remoteObject = getRemoteObject(data.getHost());
                if (remoteObject.getX() != data.getX() || remoteObject.getY() != data.getY())
                    remoteObject.setLocation(data.getX(), data.getY());
            }
        });
    }
}