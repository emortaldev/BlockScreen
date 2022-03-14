import net.minestom.server.instance.block.Block
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage


val Color.hex get() = "#"+Integer.toHexString(rgb).substring(2);


fun closestBlock(color: Color, map: Map<Color, Block>) = map.minByOrNull { it.key.distance(color) }!!

fun Color.distance(c2: Color): Double {
    val red1: Int = this.red
    val red2: Int = c2.red
    val rmean = red1 + red2 shr 1
    val r = red1 - red2
    val g: Int = this.green - c2.green
    val b: Int = this.blue - c2.blue
    return (((512 + rmean) * r * r shr 8) + 4 * g * g + ((767 - rmean) * b * b shr 8)).toDouble()
}

fun BufferedImage.resize(newW: Int, newH: Int, scaleType: Int = Image.SCALE_DEFAULT): BufferedImage {
    val tmp = getScaledInstance(newW, newH, scaleType)
    val dimg = BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB)
    val g2d = dimg.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()
    return dimg
}

fun averageColor(image: BufferedImage): Color {
    var sumR = 0L
    var sumG = 0L
    var sumB = 0L
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val pixelColor = Color(image.getRGB(x, y))
            sumR += pixelColor.red
            sumG += pixelColor.green
            sumB += pixelColor.blue
        }
    }

    val pixels = image.width * image.height
    return Color((sumR / pixels).toInt(), (sumG / pixels).toInt(), (sumB / pixels).toInt())
}

fun BufferedImage.floydSteinbergDithering(palette: Map<Color, Block>): BufferedImage {
    val d: Array<Array<Color?>> = Array(height) { arrayOfNulls<Color?>(width) }
    for (y in 0 until height) {
        for (x in 0 until width) {
            d[y][x] = Color(getRGB(x, y))
        }
    }
    for (y in 0 until height) {
        for (x in 0 until width) {
            val oldColor: Color = d[y][x]!!
            val newColor: Color = closestBlock(oldColor, palette).key
            setRGB(x, y, newColor.rgb)
            val err: Color = oldColor.sub(newColor)
            if (x + 1 < width) {
                d[y][x + 1] = d[y][x + 1]!!.add(err.mul(7.0 / 16))
            }
            if (x - 1 >= 0 && y + 1 < height) {
                d[y + 1][x - 1] = d[y + 1][x - 1]!!.add(err.mul(3.0 / 16))
            }
            if (y + 1 < height) {
                d[y + 1][x] = d[y + 1][x]!!.add(err.mul(5.0 / 16))
            }
            if (x + 1 < width && y + 1 < height) {
                d[y + 1][x + 1] = d[y + 1][x + 1]!!.add(err.mul(1.0 / 16))
            }
        }
    }
    return this
}

fun Color.add(other: Color) =
    Color((this.red + other.red).coerceIn(0, 255), (this.green + other.green).coerceIn(0, 255), (this.blue + other.blue).coerceIn(0, 255))

fun Color.sub(other: Color) =
    Color((this.red - other.red).coerceIn(0, 255), (this.green - other.green).coerceIn(0, 255), (this.blue - other.blue).coerceIn(0, 255))

fun Color.mul(other: Double) =
    Color((this.red * other).toInt().coerceIn(0, 255), (this.green * other).toInt().coerceIn(0, 255), (this.blue * other).toInt().coerceIn(0, 255))