package conf

import com.uchuhimo.konf.ConfigSpec

class MinterApiConf {
    companion object : ConfigSpec("minter") {
        val urlapi by optional("http://127.0.0.1:8841")
        val timeout by optional(60.0)

        @Deprecated(level = DeprecationLevel.WARNING, message = "Deprecated")
        val header1_name by optional("")
        @Deprecated(level = DeprecationLevel.WARNING, message = "Deprecated")
        val header1_value by optional("")
        @Deprecated(level = DeprecationLevel.WARNING, message = "Deprecated")
        val header2_name by optional("")
        @Deprecated(level = DeprecationLevel.WARNING, message = "Deprecated")
        val header2_value by optional("")

        val http_headers by optional<Map<String, String>?>(null)


        val grpc_hostname by optional("127.0.0.1")
        val grpc_port by optional(8842)
        val grpc_deadline by optional<Long?>(null)
    }
}