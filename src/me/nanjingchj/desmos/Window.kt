package me.nanjingchj.desmos

import org.mariuszgromada.math.mxparser.Function
import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import javax.swing.*
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.reflect.jvm.reflect


class Window : JFrame("Desmos") {
    private val canvas: CanvasView
    private val input = JPanel()
    private val txtInput = JTextField()
    private val functionsList = ArrayList<Function>()

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        preferredSize = Dimension(800, 500)
        pack()
        isVisible = true

        input.layout = GridBagLayout()
        var c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        c.fill = GridBagConstraints.BOTH
        input.add(txtInput, c)

        val pnl = contentPane
        pnl.layout = GridBagLayout()

        c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 0.0
        c.weighty = 1.0
        c.ipadx = 300
        c.fill = GridBagConstraints.BOTH
        pnl.add(input, c)

        c = GridBagConstraints()
        c.gridx = 1
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        c.fill = GridBagConstraints.BOTH
        canvas = CanvasView(functionsList)
        pnl.add(canvas, c)

        canvas.initBufferStrategy()
        revalidate()
    }

    private fun textTyped() {

    }
}

data class Point(val x: Double, val y: Double) {
    companion object {
        @JvmStatic
        val ORIGIN = Point(0.0, 0.0)
    }
}

data class Quadruple<T>(val a: T, val b: T, val c: T, val d: T)

class CanvasView(private val functionList: ArrayList<Function>) : Canvas() {
    // project this whole range of points onto the graph
    private var left: Double = -10.0
    private var right: Double = 10.0
    private var top: Double = 10.0
    private var bottom: Double = -10.0
    private var mouseCaptured: Boolean = false
    private lateinit var mouseLocation: java.awt.Point
    private lateinit var originalTransformation: Quadruple<Double>

    fun initBufferStrategy() {
        createBufferStrategy(2)
    }

    init {
        Thread {
            Thread.sleep(100)
            Timer(10) {
                val g = bufferStrategy.drawGraphics as Graphics2D
                render(g)
                g.dispose()
                bufferStrategy.show()
            }.start()
        }.start()

        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {
                if (mouseCaptured) {
                    val current = getMouseLocation()
                    val dx = (current.x - mouseLocation.x) / (width / (right - left))
                    val dy = (current.y - mouseLocation.y) / (height / (top - bottom))
                    left = originalTransformation.a - dx
                    right = originalTransformation.b - dx
                    top = originalTransformation.c + dy
                    bottom = originalTransformation.d + dy
                }
            }

            override fun mouseDragged(e: MouseEvent) {
                mouseMoved(e)
            }
        })
        // mouse listener
        addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent) {
                mouseCaptured = false
            }

            override fun mouseEntered(e: MouseEvent) {
            }

            override fun mouseClicked(e: MouseEvent) {
            }

            override fun mouseExited(e: MouseEvent) {
            }

            override fun mousePressed(e: MouseEvent) {
                mouseCaptured = true
                mouseLocation = getMouseLocation()
                originalTransformation = Quadruple(left, right, top, bottom)
            }

        })
    }

    private fun render(g: Graphics2D) {
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        // background
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)

        // vertical lines
        g.color = Color.BLACK
        for (x in left.toInt() - 5 until right.toInt() + 5) {
            g.stroke = BasicStroke(if (x % 5 == 0) 2.0f else 0.5f)
            val a = transform(Point(x.toDouble(), top.toDouble()))
            val b = transform(Point(x.toDouble(), bottom.toDouble()))
            g.drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }

        // horizontal lines
        for (y in bottom.toInt() - 5 until top.toInt() + 5) {
            g.stroke = BasicStroke(if (y % 5 == 0) 2.0f else 0.5f)
            val a = transform(Point(left.toDouble(), y.toDouble()))
            val b = transform(Point(right.toDouble(), y.toDouble()))
            g.drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }

        // the axes
        g.stroke = BasicStroke(3.0f);
        // x
        run {
            val a = transform(Point.ORIGIN)
            val b = transform(Point(right.toDouble(), 0.0))
            g.drawArrow(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }
        run {
            val a = transform(Point.ORIGIN)
            val b = transform(Point(left.toDouble(), 0.0))
            g.drawArrow(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }

        // y
        run {
            val a = transform(Point.ORIGIN)
            val b = transform(Point(0.0, top.toDouble()))
            g.drawArrow(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        };
        run {
            val a = transform(Point.ORIGIN)
            val b = transform(Point(0.0, bottom.toDouble()))
            g.drawArrow(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }

        functionList.forEach {

        }
    }

    private fun transform(p: Point): Point {
        return Point((p.x - left) * width / (right - left), (top - p.y) * height / (top - bottom))
    }
}

private const val ARR_SIZE = 10

fun Graphics2D.drawArrow(x1: Int, y1: Int, x2: Int, y2: Int) {
    val g = this.create() as Graphics2D
    val dx = x2 - x1.toDouble()
    val dy = y2 - y1.toDouble()
    val angle = atan2(dy, dx)
    val len = sqrt(dx * dx + dy * dy).toInt()
    val at = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
    at.concatenate(AffineTransform.getRotateInstance(angle))
    g.transform(at)

    // Draw horizontal arrow starting in (0, 0)
    g.drawLine(0, 0, len, 0)
    g.fillPolygon(intArrayOf(len, len - ARR_SIZE, len - ARR_SIZE, len), intArrayOf(0, -ARR_SIZE, ARR_SIZE, 0), 4)
    g.dispose()
}

fun getMouseLocation(): java.awt.Point {
    return MouseInfo.getPointerInfo().location
}