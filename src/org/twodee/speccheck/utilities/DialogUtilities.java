package org.twodee.speccheck.utilities;

import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class DialogUtilities {
  public static boolean isYes(String message) throws InterruptedException {
    JOptionPane p = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
    final JDialog d = p.createDialog("Manual Check");
    d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    d.setModalityType(ModalityType.MODELESS);

    p.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("value")) {
          d.dispose();
        }
      }
    });

    final String lock = "glabber";

    d.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent arg0) {
        synchronized (lock) {
          d.setVisible(false);
          lock.notify();
        }
      }
    });
    d.setVisible(true);

    Thread t = new Thread() {
      public void run() {
        synchronized (lock) {
          while (d.isVisible()) {
            try {
              lock.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      }
    };

    t.start();
    t.join();

    return p.getValue().equals(new Integer(JOptionPane.YES_OPTION));
  }
}
