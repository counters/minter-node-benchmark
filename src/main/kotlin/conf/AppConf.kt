package conf

import com.uchuhimo.konf.ConfigSpec

class AppConf {
    companion object : ConfigSpec("app") {
        val random_blocks  by optional(100)
        val num_transaction  by optional(100)
        val repeat  by optional(10)
        val mode  by optional(Mode.COROUTINES.id)
        val ms_between_repeat  by optional<Long>(1000)
    }
}
