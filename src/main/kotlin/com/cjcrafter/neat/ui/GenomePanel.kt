package com.cjcrafter.neat.ui

import com.cjcrafter.neat.Client
import org.joml.Vector2f
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import kotlin.math.abs

class ClientRenderPanel(val client: Client) : JPanel() {

    /**
     * Renders the nodes and connections of the genome.
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        // Clear out everything to black
        val graphics = g!!.create() as Graphics2D
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, width, height)

        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        val genome = client.genome
        val nodes = genome.nodes
        val connections = genome.connections
        val nodePositionCache: MutableMap<Int, Vector2f> = mutableMapOf()

        fun Float.toScreenX(): Int = (this * width).toInt()
        fun Float.toScreenY(): Int = (this * height).toInt()


        val nodeSize = 20

        // Cache nodes
        for (node in nodes) {
            nodePositionCache[node.id] = Vector2f(node.position)
        }

        // Render connections
        for (connection in connections) {
            // Update stroke width based on weight
            val weight = connection.weight
            val strokeWidth = (minStrokeWidth + maxStrokeWidth * abs(weight))
            graphics.stroke = BasicStroke(strokeWidth)

            val from = nodePositionCache[connection.fromId]!!
            val to = nodePositionCache[connection.toId]!!
            var color = if (connection.weight > 0) positiveWeightColor else negativeWeightColor
            if (!connection.enabled) color = disabledColor
            graphics.color = color
            graphics.drawLine(from.x.toScreenX() + nodeSize / 2, from.y.toScreenY() + nodeSize / 2, to.x.toScreenX() + nodeSize / 2, to.y.toScreenY() + nodeSize / 2)
        }

        // set width of the brush
        graphics.stroke = BasicStroke(3f)

        // Render nodes
        for (node in nodes) {
            graphics.color = Color.BLACK
            graphics.fillOval(node.position.x().toScreenX(), node.position.y().toScreenY(), nodeSize, nodeSize)
            graphics.color = Color.WHITE
            graphics.drawOval(node.position.x().toScreenX(), node.position.y().toScreenY(), nodeSize, nodeSize)
        }

        // Dispose of the graphics object
        graphics.dispose()
    }

    companion object {
        var negativeWeightColor = Color(0xffb3ba)
        var positiveWeightColor = Color(0xbaffc9)
        var disabledColor = Color(0x666666)
        var minStrokeWidth = 1.0f
        var maxStrokeWidth = 5.0f
    }
}