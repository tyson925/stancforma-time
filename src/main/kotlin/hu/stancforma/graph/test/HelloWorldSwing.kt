package hu.stancforma.graph.test

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JLabel


public class HelloWorldSwing(){
    public fun createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true)

        //Create and set up the window.
        val frame = JFrame("HelloWorldSwing")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.size = Dimension(400,400)

        //Add the ubiquitous "Hello World" label.
        val label = JLabel("Hello World")
        frame.getContentPane().add(label)

        //Display the window.
        frame.pack()
        frame.setVisible(true)
    }
}

public fun main(args: Array<String>) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    val stat = HelloWorldSwing()
    javax.swing.SwingUtilities.invokeLater { stat.createAndShowGUI() }
}
