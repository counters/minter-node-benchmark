import counters.minter.sdk.minter.Enum.TransactionTypes
import counters.minter.sdk.minter.MinterRaw.MultisendItemRaw
import counters.minter.sdk.minter.MinterRaw.TransactionRaw
import java.util.ArrayList

interface AddressCollector {

    val address: ArrayList<String>

//    private var collectAddress = false

    fun addAddress(transaction: TransactionRaw) {
        address.add(transaction.from)
        if (transaction.type == TransactionTypes.TypeMultiSend.int){
            (transaction.optList as List<MultisendItemRaw>).forEach {
                address.add(it.address)
            }
        } else {
            transaction.to?.let { address.add(it) }
        }
    }

    fun getUniqueAddress(): List<String>{
        return LinkedHashSet(address).toList()
    }

/*    fun collectAddress(set: Boolean){

    }*/

}