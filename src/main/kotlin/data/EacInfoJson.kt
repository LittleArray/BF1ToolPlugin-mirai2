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