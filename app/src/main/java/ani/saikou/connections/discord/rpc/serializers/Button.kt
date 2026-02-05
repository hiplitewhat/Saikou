package ani.saikou.connections.discord.rpc.serializers

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Button(
    val label: String,
    val url: String
)