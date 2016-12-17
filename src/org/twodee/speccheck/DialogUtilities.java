package org.twodee.speccheck;

import java.util.ArrayList;
import java.awt.Insets;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class DialogUtilities {
  public static boolean isYes(String message) {
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
    while (t != null) {
      try {
        t.join();
        t = null;
      } catch (InterruptedException e) {
      }
    }

    return p.getValue().equals(new Integer(JOptionPane.YES_OPTION));
  }

  public static boolean isChecked(String... messages) {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(3, 0, 3, 0);

    ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
    for (String message : messages) {
      c.gridx = 0;
      JCheckBox checkbox = new JCheckBox();
      panel.add(checkbox, c);
      checkboxes.add(checkbox);
      c.gridx = 1;
      panel.add(new JLabel("<html>" + StringUtilities.wrap(message, 50).replace("\n", "<br>") + "</html>"), c);
      ++c.gridy;
    }

    JOptionPane p = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
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
    while (t != null) {
      try {
        t.join();
        t = null;
      } catch (InterruptedException e) {
      }
    }

    for (JCheckBox checkbox : checkboxes) {
      if (!checkbox.isSelected()) {
        return false;
      }
    }
    return true;
  }
}
