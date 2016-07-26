package hu.stancforma.graph.test

import hu.stancforma.util.readUserDB
import hu.stancforma.workTime.WorkTimeCalculation
import java.awt.BorderLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*

public class GUI : JPanel, ActionListener {

    val newline = "\n"
    var openButton: JButton? = null
    var runButton: JButton? = null
    var addHoursRateButton: JButton? = null
    var log: JTextArea? = null
    var hoursField: JTextField? = null
    val openedFiles = mutableListOf<File>()
    var hours = 0

    companion object {
        //Create a file chooser
        val fileChooser = JFileChooser()

        @JvmStatic fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", java.lang.Boolean.FALSE)
                GUI().createAndShowGUI()
            }
        }
    }
    constructor() {
        log = JTextArea(15, 60)
        log?.setMargin(Insets(5, 5, 5, 5))
        log?.setEditable(false);
        val logScrollPane = JScrollPane(log)
        fileChooser.setMultiSelectionEnabled(true)

        openButton = JButton("Fileok megnyitása...")
        openButton?.addActionListener(this)

        addHoursRateButton = JButton("Óraszámok")
        addHoursRateButton?.addActionListener(this)

        hoursField = JTextField(2)

        runButton = JButton("Futattás")
        runButton?.addActionListener(this)


        //For layout purposes, put the buttons in a separate panel
        val buttonPanel = JPanel() //use FlowLayout
        buttonPanel.add(openButton)
        buttonPanel.add(addHoursRateButton)
        buttonPanel.add(hoursField)
        buttonPanel.add(runButton)

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START)
        add(logScrollPane, BorderLayout.CENTER)
    }

    override fun actionPerformed(e: ActionEvent?) {

        //Handle open button action.
        if (e?.getSource() == openButton) {
            val returnVal = fileChooser.showOpenDialog(this)

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val files = fileChooser.getSelectedFiles()
                openedFiles.addAll(files)
                //This is where a real application would open the file.
                files.forEach { file ->
                    log?.append("Opening: " + file.getName() + "." + newline)
                }

            } else {
                log?.append("Open command cancelled by user." + newline)
            }
            log?.setCaretPosition(log?.getDocument()?.getLength() ?: 0)

            //Handle save button action.
        } else if (e?.getSource() == runButton) {

            val workTimeCalculation = WorkTimeCalculation()
            workTimeCalculation.readUsersData(openedFiles,hours)
            log?.append("Programm futtatasa" + newline)

            workTimeCalculation.getLog().split("\n").forEach { massage ->
                log?.append(massage + newline)
            }

            log?.setCaretPosition(log?.getDocument()?.getLength() ?: 0)
        } else if (e?.getSource() == addHoursRateButton) {
            hours = Integer.parseInt(hoursField?.getText()) ?: 0
            log?.append("Hours: " + hours + "." + newline)

            log?.setCaretPosition(log?.getDocument()?.getLength() ?: 0)
        } else if (e?.getSource() == hoursField) {

        }
    }

    /** Returns an ImageIcon, or null if the path was invalid.  */
    fun createImageIcon(path: String): ImageIcon? {
        val imgURL = GUI::class.java.getResource(path)
        if (imgURL != null) {
            return ImageIcon(imgURL)
        } else {
            System.err.println("Couldn't find file: " + path)
            return null
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public fun createAndShowGUI() {
        //Create and set up the window.
        val frame = JFrame("FileChooserDemo")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        //Add content to the window.
        frame.add(GUI())

        //Display the window.
        frame.pack()
        frame.isVisible = true
    }
}

fun main(args: Array<String>) {

//Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    SwingUtilities.invokeLater {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", java.lang.Boolean.FALSE)
        GUI().createAndShowGUI()
    }
}
