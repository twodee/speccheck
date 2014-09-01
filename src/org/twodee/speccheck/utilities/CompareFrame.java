package org.twodee.speccheck.utilities;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class CompareFrame<E extends JComponent> extends JDialog {

  private JLabel label;
  private JScrollPane ourScroller;
  private JScrollPane theirScroller;
  private boolean isSame;
  private JButton sameButton;

  public CompareFrame() {
    setLayout(new GridBagLayout());
    setModal(true);

    GridBagConstraints constraints = new GridBagConstraints();
    
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    label = new JLabel();
    add(label, constraints);
    
    constraints.gridwidth = 1;
    ++constraints.gridy;
    constraints.weightx = 1.0;
    add(new JLabel("Expected"), constraints);
    ++constraints.gridx;
    add(new JLabel("Actual"), constraints);

    constraints.gridx = 0;
    ++constraints.gridy;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    ourScroller = new JScrollPane();
    add(ourScroller, constraints);

    theirScroller = new JScrollPane();
    ++constraints.gridx;
    add(theirScroller, constraints);
    
    constraints.weighty = 0.0;
    constraints.gridx = 0;
    ++constraints.gridy;
    JButton button = new JButton("Different");
    class PassFailListener implements ActionListener {
      private boolean isPass;
      public PassFailListener(boolean isPass) {
        this.isPass = isPass;
      }
      @Override
      public void actionPerformed(ActionEvent arg0) {
        isSame = isPass;
        setVisible(false);
      }
    }
    button.addActionListener(new PassFailListener(false));
    add(button, constraints);
    
    constraints.gridx = 1;
    sameButton = new JButton("Same");
    sameButton.addActionListener(new PassFailListener(true));
    add(sameButton, constraints);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  public CompareFrame<E> compare(String msg, E ours, E theirs) {
    label.setText(msg);
    ourScroller.setViewportView(ours);
    theirScroller.setViewportView(theirs);
    pack();
    
    sameButton.requestFocusInWindow();
    setVisible(true);

    return this;
  }
  
  public boolean isSame() {
    return isSame;
  }
}