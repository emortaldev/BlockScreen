import Main.dither
import Main.scaleType
import commands.ScaleTypeCommand
import commands.ToggleDitherCommand
import net.minestom.server.MinecraftServer
import net.minestom.server.attribute.Attribute
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.batch.RelativeBlockBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.map.MapColors
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import org.slf4j.LoggerFactory
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listenOnly
import java.awt.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists


private const val assetsURL = "https://github.com/InventivetalentDev/minecraft-assets/zipball/refs/heads/1.18.2"
private const val assetsFilePath = "./mctextures.zip"

val logger = LoggerFactory.getLogger("BlockScreenshare")

object Main {
    var scaleType: Int = Image.SCALE_DEFAULT
    var dither: Boolean = true
}

fun main() {
    logger.info("Starting!")

    if (!Files.exists(Path.of("./blocktextures"))) downloadAssets()

    logger.info("Populating colors map...")

    val blockColors = mutableMapOf<Color, Block>()

    val blackList = listOf(Block.SPAWNER, Block.BEACON)

    val blockValues = Block.values()
    blockValues.forEach { it ->
        if (!it.isSolid) return@forEach
        // wow shulker boxes lag, do not re-enable this if you want to keep your computer!
        if (it.name().endsWith("shulker_box") || it.name().endsWith("glass") || it.name().endsWith("leaves") || blackList.contains(it)) return@forEach

        val shape = it.registry().collisionShape()
        if (shape.relativeStart() != Vec.ZERO || shape.relativeEnd() != Vec.ONE) return@forEach

        val file = File("./blocktextures/${it.namespace().path()}.png")
        if (file.exists()) {
            // Find averages of all textures, then add them to the colour map
            val avg = averageColor(ImageIO.read(file))
            blockColors[avg] = it
        }
    }
    logger.info("\rFinished populating colors map!")

    logger.info("Starting server...")

    val minecraftServer = MinecraftServer.init()

    val robot = Robot()
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val rectangle = Rectangle(screenSize.width, screenSize.height)

    val defaultX = screenSize.width / 10
    val defaultY = screenSize.height / 10

    val customSizeX =
        try {
            Integer.parseInt(System.getProperty("customBlockWidth", "$defaultX"))
        } catch (e: NumberFormatException) {
            logger.error("Invalid custom block width, defaulting to $defaultX")
            screenSize.width / 10
        }
    val customSizeY =
        try {
            Integer.parseInt(System.getProperty("customBlockHeight", "$defaultY"))
        } catch (e: NumberFormatException) {
            logger.error("Invalid custom block height, defaulting to $defaultY")
            screenSize.height / 10
        }


    //MinecraftServer.setCompressionThreshold(0)
    //MinecraftServer.setChunkViewDistance(32)

    val instanceManager = Manager.instance
    val global = Manager.globalEvent

    val dimension = DimensionType.builder(NamespaceID.from("fullbright"))
        .ambientLight(2f)
        .build()
    Manager.dimensionType.addDimension(dimension)
    val instance = instanceManager.createInstanceContainer(dimension)
    instance.chunkGenerator = Generator()

    global.listenOnly<PlayerLoginEvent> {
        setSpawningInstance(instance)
        player.respawnPoint = Pos(0.0, 2.0, 0.0)
    }

    global.listenOnly<PlayerSpawnEvent> {
        player.isFlying = true
        player.isAllowFlying = true
        player.getAttribute(Attribute.FLYING_SPEED).baseValue = 10f
        player.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 1f

        object : MinestomRunnable(repeat = Duration.ofMillis(50)) {
            override fun run() {
                // Create capture of screen, then resize it to the block X,Y, then dither if enabled
                val img = robot.createScreenCapture(rectangle)
                    .resize(customSizeX, customSizeY, scaleType)
                    .also { if (dither) it.floydSteinbergDithering(blockColors) }

                val batch = RelativeBlockBatch()

                // Loop through each block and find the closest block to the pixel colour on screen
                for (x in 0 until img.width) {
                    for (y in 0 until img.height) {
                        val pixel = Color(img.getRGB(x, y))
                        batch.setBlock(x, 1, y, closestBlock(pixel, blockColors).value)
                    }
                }
                img.flush()

                batch.apply(instance) {}
            }
        }


    }

    ScaleTypeCommand.register()
    ToggleDitherCommand.register()

    minecraftServer.start("0.0.0.0", 25565)
}

fun downloadAssets() {
    print("Downloading Minecraft assets...")

    val fos = FileOutputStream(assetsFilePath)
    val httpConnection = URL(assetsURL).openConnection()
    val completeFileSize = httpConnection.contentLength.toLong()
    val input = httpConnection.inputStream
    val data = ByteArray(1024)
    var current = 0
    var downloaded = 0
    while (current != -1) {
        fos.write(data, 0, current)
        current = input.read(data, 0, 1024)

        downloaded += current
        val currentProgress = (downloaded.toDouble() / completeFileSize.toDouble() * 100.0).toInt()

        print("\rDownloading Minecraft assets... ${currentProgress}%")
    }
    println("\rDownloading Minecraft assets... DONE!")

    // Unzip assets
    println("Unzipping assets...")
    val destinationDir = File("./temp")
    val buffer = ByteArray(1024)
    val zis = ZipInputStream(FileInputStream(assetsFilePath))

    var zipEntry = zis.nextEntry
    while (zipEntry != null) {
        val newFile: File = newFile(destinationDir, zipEntry)
        if (zipEntry.isDirectory) {
            if (!newFile.isDirectory && !newFile.mkdirs()) {
                throw IOException("Failed to create directory $newFile")
            }
        } else {
            // fix for Windows-created archives
            val parent = newFile.parentFile
            if (!parent.isDirectory && !parent.mkdirs()) {
                throw IOException("Failed to create directory $parent")
            }

            // write file content
            val fos2 = FileOutputStream(newFile)
            var len: Int
            while (zis.read(buffer).also { len = it } > 0) {
                fos2.write(buffer, 0, len)
            }
            fos2.close()
        }
        zipEntry = zis.nextEntry
    }

    zis.closeEntry()
    zis.close()

    Path.of(assetsFilePath).deleteIfExists()

    try {
        Files.move(Path.of("${destinationDir.listFiles().first().path}/assets/minecraft/textures/block"), Path.of("./blocktextures"), StandardCopyOption.REPLACE_EXISTING)
    } catch (_: Exception) {}
    Files.walk(destinationDir.toPath())
        .sorted(Comparator.reverseOrder())
        .forEach(Path::deleteIfExists)
}

@Throws(IOException::class)
fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
    val destFile = File(destinationDir, zipEntry.name)
    val destDirPath = destinationDir.canonicalPath
    val destFilePath = destFile.canonicalPath
    if (!destFilePath.startsWith(destDirPath + File.separator)) {
        throw IOException("Entry is outside of the target dir: " + zipEntry.name)
    }
    return destFile
}


