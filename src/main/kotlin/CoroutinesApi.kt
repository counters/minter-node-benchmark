import counters.minter.sdk.minter_api.MinterApiCoroutines
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

class CoroutinesApi(private val minterApiCoroutines: MinterApiCoroutines): AddressCollector {

    private val logger = KotlinLogging.logger {}

    override val address = arrayListOf<String>()

    suspend fun transactions(blocks: List<Long>): Pair<List<String>, Int> = coroutineScope {
        val list = arrayListOf<String>()
        val arrBlocks = arrayListOf<Long>()
        val task = mutableMapOf<Long, Deferred<Unit?>>()
        blocks.forEach { height ->
            task[height] = async {
                minterApiCoroutines.getBlock(height).let {
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

    suspend fun getTransactions(hash: List<String>): Int = coroutineScope {
        val list = arrayListOf<String>()
        val task = mutableMapOf<String, Deferred<Unit?>>()
        var num = 0
        hash.forEach { _hash ->
            task[_hash] = async {
                minterApiCoroutines.getTransaction(_hash)?.let {
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