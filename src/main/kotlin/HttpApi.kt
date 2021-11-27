import counters.minter.sdk.minter.Enum.TransactionTypes
import counters.minter.sdk.minter.MinterRaw.*
import counters.minter.sdk.minter_api.MinterApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

class HttpApi(private val minterHttpApi: MinterApi): AddressCollector {

    private val logger = KotlinLogging.logger {}

    override val address = arrayListOf<String>()


    suspend fun transactions(blocks: List<Long>): Pair<List<String>, Int> = coroutineScope {
        val list = arrayListOf<String>()
        val arrBlocks = arrayListOf<Long>()
        val task = mutableMapOf<Long, Deferred<Unit?>>()
        blocks.forEach { height ->
            task[height] = async {
                minterHttpApi.getBlock(height).let {
                    it?.let {
                        it.transaction.forEach {
                            list.add(it.hash)
                            addAddress(it)
                        }
                        if (height == it.height) arrBlocks.add(height)
                    }
                }
            }
        }

        blocks.forEach {
            task[it]?.let {
                it.await()
            } ?: run {
                logger.error { "Error async.getBlock($it)" }
//                return@coroutineScope null
            }
        }
        list to arrBlocks.count()
    }

    suspend fun transactionsCoroutines(blocks: List<Long>): Pair<List<String>, Int> = coroutineScope {
        val list = arrayListOf<String>()
        val arrBlocks = arrayListOf<Long>()
        val task = mutableMapOf<Long, Deferred<Unit?>>()
        blocks.forEach { height ->
            task[height] = async {
//                minterHttpApi.test_getBlockCoroutines(height).let {
                minterHttpApi.getBlockCoroutines(height).let {
                    it?.let {
                        it.transaction.forEach {
                            list.add(it.hash)
                            addAddress(it)
                        }
                        if (height == it.height) arrBlocks.add(height)
                    }
                }
            }
        }

        blocks.forEach {
            task[it]?.let {
                it.await()
            } ?: run {
                logger.error { "Error async.getBlock($it)" }
//                return@coroutineScope null
            }
        }
        list to arrBlocks.count()
    }


    suspend fun getTransactionsCoroutines(hash: List<String>): Int = coroutineScope {

        val list = arrayListOf<String>()
        val task = mutableMapOf<String, Deferred<Unit?>>()
        var num = 0
        hash.forEach { _hash ->
            task[_hash] = async {
                minterHttpApi.getTransactionCoroutines(_hash)?.let {
//                minterHttpApi.getTransaction(_hash)?.let {
                    if (_hash==it.hash) {
                        num++
                        list.add(it.hash)
                    }
                    logger.debug { it.hash }
                }
            }
        }
        hash.forEach {
            task[it]?.let {
                it.await()
            } ?: run {
                logger.error { "Error minterApiCoroutines.transaction($it)" }
            }
        }
        list.count()

    }

}