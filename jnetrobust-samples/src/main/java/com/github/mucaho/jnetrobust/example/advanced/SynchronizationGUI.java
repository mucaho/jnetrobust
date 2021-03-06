/**
 * Copyright (c) 2005-2006 JavaGameNetworking
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'JavaGameNetworking' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created: Jul 29, 2006
 */

package com.github.mucaho.jnetrobust.example.advanced;

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
    private String description = "";
    private JLabel descriptionLabel;
    private SynchronizationMain.HOST localHost;
    private Map<SynchronizationMain.HOST, JPanel> objects =
            new EnumMap<SynchronizationMain.HOST, JPanel>(SynchronizationMain.HOST.class);

    public SynchronizationGUI(SynchronizationMain.HOST type) {
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

        descriptionLabel = new JLabel("");
        descriptionLabel.setBounds(100, 0, 200, 100);
        windowPanel.add(descriptionLabel);

        objects.put(SynchronizationMain.HOST.CLIENTA,
                createPanel(0, 0, Color.RED, SynchronizationMain.HOST.CLIENTA.toString()));
        objects.put(SynchronizationMain.HOST.CLIENTB,
                createPanel(300, 0, Color.BLUE, SynchronizationMain.HOST.CLIENTB.toString()));
        objects.put(SynchronizationMain.HOST.SERVER,
                createPanel(150, 300, Color.GREEN, SynchronizationMain.HOST.SERVER.toString()));

        if (type == SynchronizationMain.HOST.SERVER) {
            setLocation(200, 410);
        } else if (type == SynchronizationMain.HOST.CLIENTA) {
            setLocation(0, 0);
        } else if (type == SynchronizationMain.HOST.CLIENTB) {
            setLocation(410, 0);
        }
        windowPanel.repaint();
    }

    private JPanel createPanel(int x, int y, Color color, String description) {
        JPanel panel = new JPanel();
        panel.addKeyListener(this);
        panel.setBounds(x, y, 50, 50);
        panel.setBackground(color);
        panel.setVisible(true);
        JLabel label = new JLabel(description);
        label.setBounds(x, y, 50, 50);
        panel.add(label);
        windowPanel.add(panel);
        return panel;
    }

    protected JPanel getHostObject() {
        return objects.get(localHost);
    }

    protected JPanel getRemoteObject(SynchronizationMain.HOST host) {
        return objects.get(host);
    }

    public void addDescription(String description) {
        this.description += description + "<br>";
        descriptionLabel.setText("<html>" + this.description + "</html>");
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

    void sendGUI(SynchronizationMain.Vector2D data) throws IOException {
        JPanel hostObject = getHostObject();
        data.setX(hostObject.getX());
        data.setY(hostObject.getY());
    }

    void updateGUI(final SynchronizationMain.Vector2D data) {
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