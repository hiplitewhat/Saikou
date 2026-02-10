package ani.saikou.parsers.manga

import ani.saikou.FileUrl
import ani.saikou.client
import ani.saikou.parsers.MangaChapter
import ani.saikou.parsers.MangaImage
import ani.saikou.parsers.MangaParser
import ani.saikou.parsers.ShowResponse
import ani.saikou.tryWithSuspend
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
class AllManga : MangaParser() {
    override val name = "AllManga"
    override val saveName = "AllManga"
    override val hostUrl = "https://kenjitsu.vercel.app"

    private val posterImageReferer: String = "https://allmanga.to/"


    override suspend fun search(query: String): List<ShowResponse> {
        return tryWithSuspend(post = true, snackbar = true) {
            if (query.isEmpty()) return@tryWithSuspend emptyList()
            val response =
                client.get("$hostUrl/api/allmanga/manga/search?q=$query").parsed<SearchResponse>()

            response.data.map {
                ShowResponse(
                    name = it.name ?: it.romaji ?: it.native as String,///should not do this
                    link = it.id,
                    coverUrl = FileUrl(
                        url = it.posterImage as String,
                        mapOf("Referer" to posterImageReferer)
                    )
                )
            }
        } ?: emptyList()
    }

    override suspend fun loadChapters(
        mangaLink: String,
        extra: Map<String, String>?
    ): List<MangaChapter> {
        return tryWithSuspend(post = false, false) {
            if (mangaLink.isEmpty()) return@tryWithSuspend emptyList()
            val response = client.get("$hostUrl/api/allmanga/manga/${mangaLink}/chapters")
                .parsed<ChapterResponse>()

            response.data.map {
                MangaChapter(
                    number = it.chapterNumber,
                    link = it.chapterId,
                    title = it.title,
                )
            }
        } ?: emptyList()
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        return tryWithSuspend(post = true, snackbar = false) {
            if (chapterLink.isEmpty()) return@tryWithSuspend emptyList()
            val response = client.get("$hostUrl/api/allmanga/sources/$chapterLink")
                .parsed<ChapterImageResponse>()

            val referer = response.headers.referer

            val baseHeaders = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0",
                "Accept" to "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5",
                "Accept-Language" to "en-US,en;q=0.5",
                "Accept-Encoding" to "gzip, deflate, br, zstd",
                "Referer" to referer,
            )
            response.data.map {
                MangaImage(
                    url = FileUrl(it.url, baseHeaders)
                )
            }
        } ?: emptyList()
    }

    @Serializable
    private data class SearchResponse(val data: List<SearchItem>)

    @Serializable
    private data class SearchItem(
        val id: String,
        val name: String? = null,
        val romaji: String? = null,
        val native: String? = null,
        val posterImage: String? = null
    )


    @Serializable
    private data class ChapterResponse(val data: List<ChapterItem>)

    @Serializable
    private data class ChapterItem(
        val chapterId: String,
        val title: String? = null,
        val releaseDate: String? = null,
        val chapterNumber: String
    )


    @Serializable
    private data class ChapterImageResponse(
        val headers: Headers,
        val data: List<ChapterImage>
    )

    @Serializable
    private data class Headers(
        @SerialName("Referer")
        val referer: String
    )

    @Serializable
    private data class ChapterImage(
        val url: String,
        val page: Int
    )
}
