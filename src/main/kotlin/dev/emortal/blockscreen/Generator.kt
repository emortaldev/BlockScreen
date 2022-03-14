package dev.emortal.blockscreen

import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block

class Generator : ChunkGenerator {
    override fun generateChunkData(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {
        for (x in 0..15) {
            for (y in 0..15) {
                batch.setBlock(x, 0, y, Block.IRON_BLOCK)
            }
        }
    }

    override fun getPopulators(): MutableList<ChunkPopulator>? = null
}