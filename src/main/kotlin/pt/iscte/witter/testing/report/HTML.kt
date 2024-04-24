package pt.iscte.witter.testing.report

internal const val STYLE_FONT_POPPINS = "font-family:Poppins, sans-serif;"

internal const val STYLE_FONT_JETBRAINS = "font-family:JetBrains Mono, sans-serif;"

fun font(name: String, sizeEM: Double, weight: Int): String = "font-family:$name, sans-serif;font-size:${sizeEM}em;font-weight:$weight"