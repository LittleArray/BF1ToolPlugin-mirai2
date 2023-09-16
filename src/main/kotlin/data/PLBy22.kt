package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/15
 */
data class PLBy22(
    var isSuccessful:Boolean =false,
    val GDAT: List<_GDAT>?=null
){
    data class _GDAT(
        val ADMN: List<Long>,
        //服务器内部信息
        val ATTR: ATTR,
        val CAP: List<Long>,
        val GID: Long,
        val GMRG: Long,
        //服务器名称
        val GNAM: String,
        val GSET: Long,
        val GSTA: Long,
        val GTYP: Long,
        val GURL: String,
        val HNET: List<HNET>,
        val HOST: Long,
        val HSES: Long,
        val MODE: String,
        val NTOP: Long,
        val PCNT: List<Long>,
        val PRES: Long,
        val PSAS: String,
        val PSID: String,
        val QCAP: Long,
        val QCNT: Long,
        val RNFO: RNFO,
        //玩家列表
        val ROST: List<ROST>,
        val SID: Long,
        val TCAP: Long,
        val TINF: List<TINF>,
        val VOIP: Long,
        val VSTR: String
    )
    data class ATTR(
        val admins1: String,
        val admins2: String,
        val admins3: String,
        val admins4: String,
        val bannerurl: String,
        val country: String,
        val description1: String,
        val description2: String,
        val experience: String,
        val fairfight: String,
        val gameroundid: String,
        val hash: String,
        val kitmask: String,
        //地图
        val level: String,
        val levellocation: String,
        val lowrankonly: String,
        val mapmask: String,
        val maps1: String,
        val maps10: String,
        val maps11: String,
        val maps12: String,
        val maps13: String,
        val maps14: String,
        val maps15: String,
        val maps16: String,
        val maps2: String,
        val maps3: String,
        val maps4: String,
        val maps5: String,
        val maps6: String,
        val maps7: String,
        val maps8: String,
        val maps9: String,
        val mapsinfo: String,
        val message: String,
        //模式
        val mode: String,
        val modemask: String,
        val officialexperienceid: String,
        val operationindex: String,
        val operationstate: String,
        val owner: String,
        val pingsite: String,
        val preset: String,
        val progress: String,
        val providerid: String,
        val region: String,
        val rspexpired: String,
        val rsprestart: String,
        val rspupdated: String,
        val scalemask: String,
        val secret: String,
        val servertype: String,
        val settingmask: String,
        val settings1: String,
        val settings2: String,
        val tickRate: String,
        val tickRateMax: String,
        val type: String,
        val vehiclemask: String,
        val vips1: String,
        val vips2: String,
        val vips3: String,
        val vips4: String,
        val weaponmask: String
    )
    data class HNET(
        val EXIP: EXIP,
        val INIP: INIP,
        val MACI: Long,
        val _endFlag: Boolean
    )
    data class RNFO(
        val CRIT: CRIT
    )
    data class ROST(
        val ENID: String,
        val EXID: Long,
        val JGTS: Long,
        val LOC: Long,
        val NAME: String,
        val NASP: String,
        val PATT: PATT?=null,
        val PID: Long,
        val RCTS: Long,
        val ROLE: String,
        val STAT: Long,
        val STYP: Long,
        val TIDX: Long
    )
    data class TINF(
        val RMAP: RMAP,
        val TID: Long,
        val TSZE: Long
    )
    data class EXIP(
        val IP: Long,
        val MACI: Long,
        val PORT: Long
    )
    data class INIP(
        val IP: Long,
        val MACI: Long,
        val PORT: Long
    )
    data class CRIT(
        val soldier: Soldier
    )
    data class Soldier(
        val RCAP: Long
    )
    data class PATT(
        val latency: String?=null,
        val premium: String?=null,
        val rank: String?=null,
    )
    data class RMAP(
        val soldier: Long
    )
}
