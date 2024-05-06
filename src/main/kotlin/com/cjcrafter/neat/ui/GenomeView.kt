package com.cjcrafter.neat.ui

import com.cjcrafter.neat.NeatImpl
import javax.swing.JFrame

fun main() {
    val frame = JFrame("NEAT")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(800, 600)

    val neat = NeatImpl(3, 1, 150)
    val client = neat.clients[0]

    val panel = ClientRenderPanel(client)
    panel.background = java.awt.Color.WHITE
    frame.add(panel)

    frame.isVisible = true

    // On key press, mutate the genome and repaint the panel
    frame.addKeyListener(object : java.awt.event.KeyAdapter() {
        override fun keyPressed(e: java.awt.event.KeyEvent) {
            when (e.keyCode) {
                java.awt.event.KeyEvent.VK_SPACE -> {
                    println("Mutating")
                    client.mutate()
                    panel.repaint()
                }
            }
        }
    })
}