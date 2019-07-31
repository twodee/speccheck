package org.twodee.speccheck

import java.awt.*
import java.awt.event.*
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock
import javax.swing.*
import kotlin.concurrent.withLock

object DialogUtilities {
  @JvmStatic fun main(args: Array<String>) {
    isAllChecked("foo", "a", "b", "c")
  }

  fun isAllChecked(title: String, vararg messages: String): Boolean {
    val panel = JPanel(GridBagLayout())
    val c = GridBagConstraints().apply {
      gridy = 0
      gridwidth = 1
      gridheight = 1
      anchor = GridBagConstraints.NORTHWEST
      insets = Insets(3, 0, 3, 0)
    }

    val checkboxes = ArrayList<JCheckBox>()
    for (message in messages) {
      c.gridx = 0
      val checkbox = JCheckBox()
      panel.add(checkbox, c)
      checkboxes.add(checkbox)
      c.gridx = 1
      val label = JLabel("<html>" + Utilities.wrap(message, 50).replace("\n", "<br>") + "</html>")
      label.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
          checkbox.isSelected = true
        }
      })
      panel.add(label, c)
      ++c.gridy
    }

    val p = JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION)
    val d = p.createDialog(title)

    d.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
    d.modalityType = Dialog.ModalityType.MODELESS

    p.addPropertyChangeListener { event ->
      if (event.propertyName == "value") {
        d.dispose()
      }
    }

    val lock = ReentrantLock()
    val condition = lock.newCondition()

    d.addWindowListener(object : WindowAdapter() {
      override fun windowClosed(arg0: WindowEvent) {
        lock.withLock {
          d.isVisible = false
          condition.signal()
        }
      }
    })
    d.isVisible = true

    var t: Thread? = object : Thread() {
      override fun run() {
        lock.withLock {
          while (d.isVisible) {
            try {
              condition.await()
            } catch (e: InterruptedException) {
              e.printStackTrace()
            }

          }
        }
      }
    }

    t!!.start()
    while (t != null) {
      try {
        t.join()
        t = null
      } catch (e: InterruptedException) {
      }
    }

    for (checkbox in checkboxes) {
      if (!checkbox.isSelected) {
        return false
      }
    }
    return true
  }

  fun isListOkay(title: String, message: String, items: Array<String>): Boolean {
    val panel = JPanel(BorderLayout())
    panel.add(JLabel("<html>" + Utilities.wrap(message, 50).replace("\n", "<br>") + "</html>"), BorderLayout.NORTH)
    val list = JList(items)
    list.visibleRowCount = 15
    val scroller = JScrollPane(list)
    panel.add(scroller)

    val p = JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION)
    val d = p.createDialog(title)

    d.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
    d.modalityType = Dialog.ModalityType.MODELESS

    p.addPropertyChangeListener { event ->
      if (event.propertyName == "value") {
        d.dispose()
      }
    }

    val lock = ReentrantLock()
    val condition = lock.newCondition()

    d.addWindowListener(object : WindowAdapter() {
      override fun windowClosed(arg0: WindowEvent?) {
        lock.withLock {
          d.isVisible = false
          condition.signal()
        }
      }
    })
    d.isVisible = true

    var t: Thread? = object : Thread() {
      override fun run() {
        lock.withLock {
          while (d.isVisible) {
            try {
              condition.await()
            } catch (e: InterruptedException) {
              e.printStackTrace()
            }
          }
        }
      }
    }

    t!!.start()
    while (t != null) {
      try {
        t.join()
        t = null
      } catch (e: InterruptedException) {
      }
    }

    return p.value == JOptionPane.YES_OPTION
  }
}