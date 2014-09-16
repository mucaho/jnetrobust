/**
 * Copyright (c) 2005-2006 JavaGameNetworking
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'JavaGameNetworking' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
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
package net.ddns.mucaho.jnetrobust.example;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * @author Matthew D. Hicks
 * @author mucaho
 */
public class ObjectSynchronizationGUI extends JFrame implements KeyListener {
    private JPanel windowPanel;
    private JPanel hostObject;
    private JPanel remoteObject;

    public ObjectSynchronizationGUI(ObjectSynchronizationController.HOST type) {
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

        if (type == ObjectSynchronizationController.HOST.SERVER) {
            hostObject = createPanel(0, 0, Color.BLUE);
            remoteObject = createPanel(300, 300, Color.GREEN);
        } else {
            hostObject = createPanel(300, 300, Color.RED);
            remoteObject = createPanel(0, 0, Color.GREEN);
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
        return hostObject;
    }

    protected JPanel getRemoteObject() {
        return remoteObject;
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

        hostObject.setLocation(hostObject.getX() + x, hostObject.getY() + y);
    }
}