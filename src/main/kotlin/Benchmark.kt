import com.uchuhimo.konf.source.yaml
import conf.AppConf
import conf.MinterApiConf
import counters.minter.sdk.minter_api.*
import counters.minter.sdk.minter_api.grpc.GrpcOptions
import counters.minter.sdk.minter_api.http.HttpOptions
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

class Benchmark(config: String) {
    private val configMinterApi = com.uchuhimo.konf.Config { addSpec(MinterApiConf) }.from.yaml.file(config)
    private val configApp = com.uchuhimo.konf.Config { addSpec(AppConf) }.from.yaml.file(config)

//    private val hostname = "xeon24.local"

    private val logger = KotlinLogging.logger {}
    private val grpcOptions = GrpcOptions(
        hostname = configMinterApi[MinterApiConf.grpc_hostname],
        port = configMinterApi[MinterApiConf.grpc_port],
        deadline = configMinterApi[MinterApiConf.grpc_deadline],
    )
    private val httpOptions = HttpOptions(
        raw = configMinterApi[MinterApiConf.urlapi],
        timeout = configMinterApi[MinterApiConf.timeout],
    )
    private val minterApiCoroutines = MinterApiCoroutines(grpcOptions)
    private val minterApi2 = MinterGrpcApi(grpcOptions)
    private val minterHttpApi = MinterApi(null, httpOptions)

    private val asyncApi = AsyncApi(minterApi2)
    private val coroutinesApi = CoroutinesApi(minterApiCoroutines)
    private val httpApi = HttpApi(minterHttpApi)


    private val transactions = arrayListOf<String>()

    private var firsStartHeight = 0L to 0L
//    private val firsStartHeight = initFirsStartHeight()

    data class MeasureJob(
        var measureTimeMillis: Long = 0,
        var num: Int = 0,
        var error: Int = 0,
    )

    private val measureBlock = MeasureJob()
    private val measureTransaction = MeasureJob()

    fun initFirsStartHeight(): Pair<Long, Long>? {
        return getFirsStartHeight()
    }

    init {
        initFirsStartHeight()?.let {
            firsStartHeight = it.first+1 to it.second
        } ?: run {
            logger.error { "Error load block number from API" }
            throw Exception("Error load block number from API")
        }

        val http_headers = configMinterApi[MinterApiConf.http_headers]
//        logger.info { "http_headers $http_headers" }
    }

    fun reset() {

    }

    fun runThreads() {
//        logger.info { "getFirsStartHeight() ${firsStartHeight}" }
        val numRandomBlocks = configApp[AppConf.random_blocks]

        randomBlocks(firsStartHeight, numRandomBlocks).let {
            val numBlocks = it.size
            val transactions = arrayListOf<String>()
            var realNumBlocks = 0
            val msBlock = measureTimeMillis {
                asyncApi.asyncBlocks(it).let {
                    transactions.addAll(it.first)
                    realNumBlocks = it.second
                }
            }
            val numTrs = transactions.count()//, error read ${numBlocks-numTrs}
            val blockInMs = (numBlocks.toDouble() / msBlock.toDouble() * 1000).roundToInt()
            logger.debug { "measureTimeMillis $msBlock, numBlocks $numBlocks, error read ${numBlocks - realNumBlocks}, numTrs $numTrs, $blockInMs block/s" }
    /*        var successTrs = 0
            val msTransaction = measureTimeMillis {
                successTrs = asyncApi.asyncTransactions(transactions)
            }
            val trsInMs = (successTrs.toDouble() / msTransaction.toDouble() * 1000).roundToInt()
            logger.info { "measureTimeMillis $msTransaction, numTrs $numTrs, error read ${numTrs - successTrs}, numTrs $numTrs, $trsInMs trs/s" }
*/
            val error = (numBlocks - realNumBlocks)
            val errorStr = if (error==0) "" else " error read $error blocks,"
            logger.debug { "measureTimeMillis $msBlock, numBlocks $numBlocks, error read $error, numTrs $numTrs, $blockInMs block/s" }
            println("$numBlocks blocks in $msBlock ms,$errorStr avg $blockInMs block/second")
            measureBlock.num += numBlocks
            measureBlock.error += error
            measureBlock.measureTimeMillis += msBlock


        }

    }

    private var transactionsLastIterator = 0

    private fun getNextTransaction(num: Int): List<String> {
        val transactionsIterator = transactions.listIterator(transactionsLastIterator)
//        transactionsIterator.i
        val newTransactions = arrayListOf<String>()

        var i = 0
        do {
            if (transactionsIterator.hasNext()) {
//                logger.info { transactionsIterator.next()}
                newTransactions.add(transactionsIterator.next())
            }
            i++
        } while (i < num)

        transactionsLastIterator = transactionsIterator.previousIndex()
//        println(";")
        return newTransactions
    }

    fun runTransaction() {
        val newTransactions = getNextTransaction(configApp[AppConf.num_transaction])
        val numTrs = newTransactions.count()

        var successTrs: Int
        val msTransaction = measureTimeMillis {
            successTrs = asyncApi.asyncTransactions(newTransactions)
        }
        val trsInMs = (numTrs.toDouble() / msTransaction.toDouble() * 1000.0).roundToInt()
        val error = (numTrs - successTrs)
        val errorStr = if (error==0) "" else " error read $error blocks,"
        logger.debug { "measureTimeMillis $msTransaction, numTrs $numTrs, error read $error, numTrs $successTrs, $trsInMs trs/s" }
        println("$numTrs transactions in $msTransaction ms,$errorStr avg $trsInMs transaction/second")

        measureTransaction.num += successTrs
        measureTransaction.error += (numTrs - successTrs)
        measureTransaction.measureTimeMillis += msTransaction
//            logger.info { measureTransaction }
    }

    fun runCoroutinesTransaction() {
        val newTransactions = getNextTransaction(configApp[AppConf.num_transaction])
        val numTrs = newTransactions.count()

        runBlocking {
            var successTrs: Int
            val msTransaction = measureTimeMillis {
                successTrs = coroutinesApi.getTransactions(newTransactions)
            }
            val trsInMs = (numTrs.toDouble() / msTransaction.toDouble() * 1000.0).roundToInt()
            val error = (numTrs - successTrs)
            val errorStr = if (error==0) "" else " error read $error blocks,"
            logger.debug { "measureTimeMillis $msTransaction, numTrs $numTrs, error read $error, numTrs $successTrs, $trsInMs trs/s" }
            println("$numTrs transactions in $msTransaction ms,$errorStr avg $trsInMs transaction/second")

            measureTransaction.num += successTrs
            measureTransaction.error += (numTrs - successTrs)
            measureTransaction.measureTimeMillis += msTransaction
//            logger.info { measureTransaction }
        }
    }

    fun runHttpTransaction() {
        val newTransactions = getNextTransaction(configApp[AppConf.num_transaction])
        val numTrs = newTransactions.count()

        runBlocking {
            var successTrs: Int
            val msTransaction = measureTimeMillis {
                successTrs = httpApi.getTransactionsCoroutines(newTransactions)
            }
            val trsInMs = (numTrs.toDouble() / msTransaction.toDouble() * 1000.0).roundToInt()
            val error = (numTrs - successTrs)
            val errorStr = if (error==0) "" else " error read $error blocks,"
            logger.debug { "measureTimeMillis $msTransaction, numTrs $numTrs, error read $error, numTrs $successTrs, $trsInMs trs/s" }
            println("$numTrs transactions in $msTransaction ms,$errorStr avg $trsInMs transaction/second")

            measureTransaction.num += successTrs
            measureTransaction.error += (numTrs - successTrs)
            measureTransaction.measureTimeMillis += msTransaction
//            logger.info { measureTransaction }
        }
    }

    fun runAsyncHttp() {

        runBlocking {

            val numRandomBlocks = configApp[AppConf.random_blocks]
            randomBlocks(firsStartHeight, numRandomBlocks).let {
                val numBlocks = it.count()
                var realNumBlocks = 0
                val transactions = arrayListOf<String>()
                val msBlock = measureTimeMillis {
//                    httpApi.transactions(it).let {
                    httpApi.transactionsCoroutines(it).let {
                        transactions.addAll(it.first)
                        realNumBlocks = it.second
                    }
                    /*httpApi.transactions(it).let {
                        transactions.addAll(it.first)
                        realNumBlocks = it.second
                    }*/
                }
                val numTrs = transactions.count()
                val blockInMs = (numBlocks.toDouble() / msBlock.toDouble() * 1000).roundToInt()
                val error = (numBlocks - realNumBlocks)
                val errorStr = if (error==0) "" else " error read $error blocks,"
                logger.debug { "measureTimeMillis $msBlock, numBlocks $numBlocks, error read $error, numTrs $numTrs, $blockInMs block/s" }
                println("$numBlocks blocks in $msBlock ms,$errorStr avg $blockInMs block/second")
                measureBlock.num += numBlocks
                measureBlock.error += error
                measureBlock.measureTimeMillis += msBlock
            }
        }
    }

    fun runCoroutines() {

        runBlocking {
//            val firsStartHeight = getFirsStartHeightCoroutines()
//            logger.info { "getFirsStartHeight() ${firsStartHeight}" }
            val numRandomBlocks = configApp[AppConf.random_blocks]

            randomBlocks(firsStartHeight, numRandomBlocks).let {
                val numBlocks = it.count()
                var realNumBlocks = 0
                val transactions = arrayListOf<String>()
                val msBlock = measureTimeMillis {
                    coroutinesApi.transactions(it).let {
                        transactions.addAll(it.first)
                        realNumBlocks = it.second
                    }
                }
                val numTrs = transactions.count()
                val blockInMs = (numBlocks.toDouble() / msBlock.toDouble() * 1000).roundToInt()
                val error = (numBlocks - realNumBlocks)
                val errorStr = if (error==0) "" else " error read $error blocks,"
//                logger.info { "address num ${coroutinesApi.address.count()} ${coroutinesApi.getUniqueAddress().count()}" }
                logger.debug { "measureTimeMillis $msBlock, numBlocks $numBlocks, error read $error, numTrs $numTrs, $blockInMs block/s" }
                println("$numBlocks blocks in $msBlock ms,$errorStr avg $blockInMs block/second")
                measureBlock.num += numBlocks
                measureBlock.error += error
                measureBlock.measureTimeMillis += msBlock
            }
        }
    }


    private suspend fun getFirsStartHeightCoroutines(): Pair<Long, Long>? {
        minterApiCoroutines.getStatus()?.let {
            return it.initial_height to it.height
        }
        logger.info { "null" }
        return null
    }

    private fun getFirsStartHeight(): Pair<Long, Long>? {
        minterApi2.getStatus()?.let {
            return it.initial_height to it.height
        }
        logger.info { "null" }
        return null
    }

    private fun randomBlocks(firs_last: Pair<Long, Long>, num: Int) = randomBlocks(firs_last.first, firs_last.second, num)

    private fun randomBlocks(first: Long, last: Long, num: Int): List<Long> {
        val list = arrayListOf<Long>()
        repeat(num) {
            list.add((first..last).random())
        }
        return list
    }

    suspend fun loadTransactions() {

        do {
            randomBlocks(firsStartHeight, configApp[AppConf.num_transaction]).let {
                coroutinesApi.transactions(it).let {
                    transactions.addAll(it.first)
                }
            }
            print("*")
        } while (transactions.count() < (configApp[AppConf.num_transaction] * configApp[AppConf.repeat]))
        println("")
        println("Loaded ${transactions.count()} hash")
    }

    fun printMeasureTransaction() {
        val speed = (measureTransaction.num.toDouble()/measureTransaction.measureTimeMillis.toDouble()*1000.0).roundToInt()
        val error = measureBlock.error
        val errorStr = if (error==0) "" else ", error $error transactions"
        logger.debug { "| Total trs: $measureTransaction" }
        println("|--------------------------------------------------------------")

        println( "| Total: load ${measureTransaction.num} transactions$errorStr" +
                " in ${measureTransaction.measureTimeMillis} ms," +
                " avg $speed transaction/second")
        println("|--------------------------------------------------------------")

    }

    fun printMeasureBlock() {
        logger.debug { "Total block: $measureBlock" }
//        logger.info { "${measureBlock.num} ${measureBlock.measureTimeMillis}" }
        val speed = (measureBlock.num.toDouble()/measureBlock.measureTimeMillis.toDouble()*1000.0).roundToInt()
        val error = measureBlock.error
        val errorStr = if (error==0) "" else ", error $error blocks"
        println("|--------------------------------------------------------------")
        println( "| Total: load ${measureBlock.num} blocks$errorStr" +
                " in ${measureBlock.measureTimeMillis} ms," +
                " avg $speed block/second")
        println("|--------------------------------------------------------------")
    }


}