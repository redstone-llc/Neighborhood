package llc.redstone.neighborhood

data class Texture(
    val name: String,
    val id: String, //001-999
    val category: TextureBrowser.Category
) {
    companion object {
        val textures = mutableListOf<Texture>()

        fun initTextures() {
            val file = Texture::class.java.getResourceAsStream("/neighborhood/textures.txt") ?: return
            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(":")
                    if (parts.size == 3) {
                        val name = parts[1].trim()
                        val id = parts[2].trim() ?: return@forEach
                        val category = try {
                            TextureBrowser.Category.valueOf(parts[0].trim().uppercase())
                        } catch (e: IllegalArgumentException) {
                            return@forEach
                        }
                        textures.add(Texture(name, id, category))
                    }
                }
            }

            Neighborhood.LOGGER.info("Loaded ${textures.size} textures from textures.txt")
        }
    }
}