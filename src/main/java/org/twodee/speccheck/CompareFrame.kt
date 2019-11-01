package org.twodee.speccheck

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class CompareFrame<E : JComponent> @JvmOverloads constructor(isManual: Boolean = true) : JDialog() {

  private val label: JTextArea
  private val ourScroller: JScrollPane
  private val theirScroller: JScrollPane
  var isSame: Boolean = false
    private set
  private var sameButton: JButton

  init {
    layout = GridBagLayout()
    isModal = true

    val constraints = GridBagConstraints()

    constraints.gridx = 0
    constraints.gridy = 0
    constraints.fill = GridBagConstraints.HORIZONTAL
    constraints.gridwidth = GridBagConstraints.REMAINDER
    constraints.insets = Insets(10, 10, 10, 10)

    label = JTextArea()
    label.wrapStyleWord = true
    label.lineWrap = true
    label.isEditable = false
    label.background = Color(255, 255, 255, 0)

    add(label, constraints)

    constraints.insets = Insets(0, 0, 0, 0)

    constraints.gridwidth = 1
    ++constraints.gridy
    constraints.weightx = 1.0
    constraints.fill = GridBagConstraints.NONE
    constraints.anchor = GridBagConstraints.CENTER
    val expectedLabel = JLabel("Expected")
    add(expectedLabel, constraints)
    ++constraints.gridx
    add(JLabel("Actual"), constraints)

    constraints.gridx = 0
    constraints.gridwidth = 2
    ++constraints.gridy
    constraints.fill = GridBagConstraints.BOTH
    constraints.weightx = 1.0
    constraints.weighty = 1.0

    ourScroller = JScrollPane()
    theirScroller = JScrollPane()

    val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ourScroller, theirScroller)
    splitPane.resizeWeight = 0.5
    add(splitPane, constraints)

    ++constraints.gridy
    if (isManual) {
      constraints.weighty = 0.0
      constraints.gridx = 0
      val button = JButton("Different")

      class PassFailListener(private val isPass: Boolean) : ActionListener {

        override fun actionPerformed(arg0: ActionEvent) {
          isSame = isPass
          isVisible = false
        }
      }
      button.addActionListener(PassFailListener(false))
      add(button, constraints)

      constraints.gridx = 1
      sameButton = JButton("Same")
      sameButton.addActionListener(PassFailListener(true))
      add(sameButton, constraints)
    } else {
      constraints.weighty = 0.0
      constraints.gridx = 0
      constraints.gridwidth = 2
      sameButton = JButton("Okay")
      class OkayListener : ActionListener {
        override fun actionPerformed(arg0: ActionEvent) {
          isSame = false
          isVisible = false
        }
      }
      sameButton.addActionListener(OkayListener())
      add(sameButton, constraints)
    }

    minimumSize = Dimension(500, 0)
    defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
  }

  fun compare(msg: String, ours: E, theirs: E): CompareFrame<E> {
    label.text = msg
    ourScroller.setViewportView(ours)
    theirScroller.setViewportView(theirs)
    pack()
    pack()

    sameButton.requestFocusInWindow()
    isVisible = true

    return this
  }
}