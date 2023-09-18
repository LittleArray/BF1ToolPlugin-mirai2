package top.ffshaozi.data.eac

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/18
 */
data class PlayerPID(
    val data: Data,
    val error_code: Int,
    val error_msg: Any
){
    data class Data(
        val dateCreated: String,
        val displayName: String,
        val isVisible: Boolean,
        val lastAuthenticated: String,
        val name: String,
        val namespaceName: String,
        val personaId: Long,
        val pidId: Long,
        val showPersona: String,
        val status: String,
        val statusReasonCode: String
    )
}
