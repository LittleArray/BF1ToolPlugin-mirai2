package data

data class EacInfoJson(
    val data: List<Data>?=null,
    val error_code: Int,
    val error_msg: String?=""
){
    data class Data(
        val case_id: Int,
        val created_at: String,
        val current_name: String,
        val current_status: Int,
        val personaId: Long,
        val report_name: String,
        val user_Id: Long
    )
}
data class EacInfoByPID(
    val data: Data?,
    val error_code: Int,
    val error_msg: Any
){
    data class Data(
        val case_id: Int,
        val created_at: String,
        val current_status: Int,
        val update_at: String
    )
}

data class MultiCheckPostJson(
    val pids: MutableList<Long> = mutableListOf()
)
data class MultiCheckResponse(
    val data: List<Long>,
    val error_code: Int,
    val error_msg: Any
)
data class KickLogPost(
    val logs: List<Log>
){
    data class Log(
        val display_name: String,
        val kicked_at: Int,
        val kicked_from_gameId: Long,
        val kicked_from_name: String,
        val personaId: Long,
        val userId: Int
    )
}
data class ReportToEAC(
    val battlelog_snapshot_url: String,
    val case_body: String,
    val game_type: Int,
    val report_by: ReportBy,
    val target_EAID: String,
    val target_personaId: Int,
    val target_userId: Int
){
    data class ReportBy(
        val report_platform: String,
        val user_id: String
    )
}
