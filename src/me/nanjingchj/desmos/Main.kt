package me.nanjingchj.desmos

fun main() {
    System.setProperty("sun.java2d.opengl", "True")
    val w = Window()
}

class JMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            main()
        }
    }
}