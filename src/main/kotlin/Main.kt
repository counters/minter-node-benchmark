import com.uchuhimo.konf.source.yaml
import conf.AppConf
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val config by parser.option(ArgType.String, shortName = "c", description = "patch to config.yml").default("./config.yml")
//    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name").required()
    val stringFormat by parser.option(
        ArgType.Choice(listOf("html", "csv", "pdf"), { it }), shortName = "sf",
        description = "Format as string for output file"
    ).default("csv").multiple()
    val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)

    parser.parse(args)
    val configApp = com.uchuhimo.konf.Config { addSpec(AppConf) }.from.yaml.file(config)

    val benchmark = Benchmark(config)


    val msBetweenRepeat = configApp[AppConf.ms_between_repeat]
    val repeat = configApp[AppConf.repeat]
    val mode = configApp[AppConf.mode]

    when (mode) {
        Mode.COROUTINES.id -> {
            println("Mode.COROUTINES")
        }
        Mode.THREADS.id -> {
            println("Mode.THREADS")
        }
        Mode.ASYNC_HTTP_API.id -> {
            println("Mode.ASYNC_HTTP_API")
        }
    }

    println("Запуск теста: ${configApp[AppConf.random_blocks]} случайных блоков по $repeat повторов с паузой $msBetweenRepeat миллисекунд")
    repeat(configApp[AppConf.repeat]) {
        if (configApp[AppConf.mode] == Mode.COROUTINES.id) {
            benchmark.runCoroutines()
        } else if (configApp[AppConf.mode] == Mode.THREADS.id) {
            benchmark.runThreads()
//            val thread = Thread { benchmark.run() }
//            thread.start()
//            thread.join()
        } else if (configApp[AppConf.mode] == Mode.ASYNC_HTTP_API.id) {
            benchmark.runAsyncHttp()
        }
        runBlocking { delay(configApp[AppConf.ms_between_repeat]) }
    }

    benchmark.printMeasureBlock()
    println("Запуск теста: ${configApp[AppConf.num_transaction]} транзакций по $repeat повторов с паузой $msBetweenRepeat миллисекунд")
    print("Загружаем хеши транзакций для теста ")
    runBlocking {
        benchmark.loadTransactions()
        delay(msBetweenRepeat)
    }
    repeat(configApp[AppConf.repeat]) {
        runBlocking { delay(configApp[AppConf.ms_between_repeat]) }

        if (configApp[AppConf.mode] == Mode.COROUTINES.id) {
            benchmark.runCoroutinesTransaction()
        } else if (configApp[AppConf.mode] == Mode.THREADS.id) {
            benchmark.runTransaction()
        } else if (configApp[AppConf.mode] == Mode.ASYNC_HTTP_API.id) {
            benchmark.runHttpTransaction()
        }
    }

    benchmark.printMeasureTransaction()



}

