package ani.saikou.connections.discord.auth.serializers

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class User(
    @SerialName("id")
    val id: String,
    @SerialName("username")
    val username: String,
    @SerialName("global_name")
    val globalName: String? = null,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("discriminator")
    val discriminator: String? = "0"
) {

    fun getAvatarUrl(): String {
        return if (avatar != null) {

            "https://cdn.discordapp.com/avatars/$id/$avatar.png"
        } else {

            val index = if (discriminator == "0" || discriminator == null) {
                (id.toLong() shr 22) % 6
            } else {
                discriminator.toInt() % 5
            }
            "https://cdn.discordapp.com/embed/avatars/$index.png"
        }
    }
}