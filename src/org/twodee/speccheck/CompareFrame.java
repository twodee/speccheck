package org.twodee.speccheck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class CompareFrame<E extends JComponent> extends JDialog {

  private JTextArea label;
  private JScrollPane ourScroller;
  private JScrollPane theirScroller;
  private boolean isSame;
  private JButton sameButton;

  public CompareFrame() {
    this(true);
  }

  public CompareFrame(boolean isManual) {
    setLayout(new GridBagLayout());
    setModal(true);

    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(10, 10, 10, 10);

    label = new JTextArea();
    label.setWrapStyleWord(true);
    label.setLineWrap(true);
    label.setEditable(false);
    label.setBackground(new Color(255, 255, 255, 0));

    add(label, constraints);

    constraints.insets = new Insets(0, 0, 0, 0);

    constraints.gridwidth = 1;
    ++constraints.gridy;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    JLabel expectedLabel = new JLabel("Expected");
    add(expectedLabel, constraints);
    ++constraints.gridx;
    add(new JLabel("Actual"), constraints);

    constraints.gridx = 0;
    constraints.gridwidth = 2;
    ++constraints.gridy;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    ourScroller = new JScrollPane();
    theirScroller = new JScrollPane();

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ourScroller, theirScroller);
    splitPane.setResizeWeight(0.5);
    add(splitPane, constraints);

    ++constraints.gridy;
    if (isManual) {
      constraints.weighty = 0.0;
      constraints.gridx = 0;
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
    } else {
      constraints.weighty = 0.0;
      constraints.gridx = 0;
      constraints.gridwidth = 2;
      sameButton = new JButton("Okay");
      class OkayListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          isSame = false;
          setVisible(false);
        }
      }
      sameButton.addActionListener(new OkayListener());
      add(sameButton, constraints);
    }

    setMinimumSize(new Dimension(500, 0));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  public CompareFrame<E> compare(String msg,
                                 E ours,
                                 E theirs) {
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
