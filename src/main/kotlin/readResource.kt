fun test(javaClass: Class<*>) {
    val pathToShader = "compute/doubler.glsl"
    val resourceStream = javaClass.getResourceAsStream(pathToShader)!!
    val content = resourceStream.bufferedReader().use { it.readText() }
    println(content)
}