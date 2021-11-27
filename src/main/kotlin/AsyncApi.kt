import counters.minter.sdk.minter_api.MinterGrpcApi
import mu.KotlinLogging
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

class AsyncApi(private val minterGrpcApi: MinterGrpcApi): AddressCollector {
    private val logger = KotlinLogging.logger {}
    override val address = arrayListOf<String>()


    fun asyncBlocks(blocks: List<Long>, getTransaction: Boolean= true): Pair<List<String>, Int> {
//        val semaphore = Semaphore((1-blocks.count()))
        val semaphore = Semaphore(1)
        val list = arrayListOf<String>()
//        val arrBlocks = arrayListOf<Long>()
        var num = 0
        var num2 = 0
        val counter = AtomicInteger()
        val counter2 = AtomicInteger()
//        val mutex = Mutex()

        blocks.forEach { height ->
            semaphore.acquireUninterruptibly()
            minterGrpcApi.asyncBlock(height) {
//                logger.warn { "minterApi2.asyncBlock($height): ${it?.height}" }
                it?.let {
                    if ( getTransaction ) {
                        it.transaction.forEach {
                            if (height == it.height) {
                                list.add(it.hash)
                                addAddress(it)
                            }
                        }
                    }
                    if (height == it.height) {
//                        logger.warn { " arrBlocks.add($height)" }
//                        arrBlocks.add(height)
                        counter2.incrementAndGet()
                    } else {
                        logger.error { "Error $it" }
                    }
                } ?: run {
                    logger.error { "Error minterApi2.asyncBlock($height)" }
                }
                num++
                counter.incrementAndGet()
//                mutex.isLocked
//                mutex.withLock { num2++ }

                semaphore.release()
//                semaphore.release()
            }
//            logger.error { "height $height" }
        }

        semaphore.acquire()
//        logger.info { "num $num, $num2, $counter, $counter2" }
        return list to counter2.get()
    }

    fun asyncTransactions(hash: List<String>): Int {
        val semaphore = Semaphore(1)
//        val list = arrayListOf<String>()
        val counter2 = AtomicInteger()
        var num = 0
        hash.forEach { _hash ->
            semaphore.acquireUninterruptibly()
            minterGrpcApi.asyncTransaction(_hash) {
                it?.let {
                    if (_hash==it.hash) {
                        num++
                        counter2.incrementAndGet()
//                        list.add(it.hash)
                    }
                }
                semaphore.release()
            }
        }
        semaphore.acquire()
        return counter2.get()
    }
}