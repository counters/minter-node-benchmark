# minter-node-benchmark
Minter Node Benchmark console application to test the performance of the api node.

The current version allows:
- Check the download speed of random blocks and transactions*.
- Use different transports (HTTP, gRPC)
- Convenient to configure via yaml config
- Output the final report
- Cross-platform launch (Linux, Windows, macOS)


## Running from source
```shell
git clone https://github.com/counters/minter-node-benchmark.git
cd minter-node-benchmark
./gradlew run
#gradlew.bat run # for Windows
```

## Run from precompiled jar file
```shell
wget -O MinterNodeBenchmark.main.jar https://github.com/counters/minter-node-benchmark/releases/download/v1.0.0/MinterNodeBenchmark.main.jar
wget -O config.yml https://github.com/counters/minter-node-benchmark/raw/v1.0.0/config.yml
java -Dfile.encoding=UTF-8 -jar MinterNodeBenchmark.main.jar
#java -Dfile.encoding=UTF-8 -jar MinterNodeBenchmark.main.jar --config patch_to_config.yml
```

### Example of output
```text
Mode.COROUTINES
Запуск теста: 100 случайных блоков по 10 повторов с паузой 500 миллисекунд
100 blocks in 220 ms, avg 455 block/second
100 blocks in 78 ms, avg 1282 block/second
100 blocks in 78 ms, avg 1282 block/second
100 blocks in 68 ms, avg 1471 block/second
100 blocks in 67 ms, avg 1493 block/second
100 blocks in 69 ms, avg 1449 block/second
100 blocks in 61 ms, avg 1639 block/second
100 blocks in 50 ms, avg 2000 block/second
100 blocks in 40 ms, avg 2500 block/second
100 blocks in 31 ms, avg 3226 block/second
|--------------------------------------------------------------
| Total: load 1000 blocks in 762 ms, avg 1312 block/second
|--------------------------------------------------------------
Запуск теста: 100 транзакций по 10 повторов с паузой 500 миллисекунд
Загружаем хеши транзакций для теста *****************
Loaded 1032 hash
100 transactions in 36 ms, avg 2778 transaction/second
100 transactions in 29 ms, avg 3448 transaction/second
100 transactions in 25 ms, avg 4000 transaction/second
100 transactions in 24 ms, avg 4167 transaction/second
100 transactions in 22 ms, avg 4545 transaction/second
100 transactions in 20 ms, avg 5000 transaction/second
100 transactions in 24 ms, avg 4167 transaction/second
100 transactions in 21 ms, avg 4762 transaction/second
100 transactions in 20 ms, avg 5000 transaction/second
100 transactions in 20 ms, avg 5000 transaction/second
|--------------------------------------------------------------
| Total: load 1000 transactions in 241 ms, avg 4149 transaction/second
|--------------------------------------------------------------

```

### Example (10000 blocks and transactions) of output
```text
Mode.COROUTINES
Запуск теста: 10000 случайных блоков по 10 повторов с паузой 5000 миллисекунд
10000 blocks in 2472 ms, avg 4045 block/second
10000 blocks in 1961 ms, avg 5099 block/second
10000 blocks in 1599 ms, avg 6254 block/second
10000 blocks in 1124 ms, avg 8897 block/second
10000 blocks in 1098 ms, avg 9107 block/second
10000 blocks in 1331 ms, avg 7513 block/second
10000 blocks in 1205 ms, avg 8299 block/second
10000 blocks in 1172 ms, avg 8532 block/second
10000 blocks in 1204 ms, avg 8306 block/second
10000 blocks in 1134 ms, avg 8818 block/second
|--------------------------------------------------------------
| Total: load 100000 blocks in 14300 ms, avg 6993 block/second
|--------------------------------------------------------------
Запуск теста: 10000 транзакций по 10 повторов с паузой 5000 миллисекунд
Загружаем хеши транзакций для теста *******************
Loaded 102402 hash
10000 transactions in 748 ms, avg 13369 transaction/second
10000 transactions in 525 ms, avg 19048 transaction/second
10000 transactions in 507 ms, avg 19724 transaction/second
[main] ERROR counters.minter.sdk.minter_api.MinterApiCoroutines - StatusException: io.grpc.StatusException: FAILED_PRECONDITION: tx (3FC090E6A9B28DBDC1446EBD2B3C208C6B15B1680E7352AF2DE9763E9F423CC9) not found
[main] ERROR counters.minter.sdk.minter_api.MinterApiCoroutines - StatusException: io.grpc.StatusException: FAILED_PRECONDITION: tx (AE71AD8B074AED9068839AC6B62F0AA1D333E3C5848D46A1642E23DDF539BCF7) not found
[main] ERROR CoroutinesApi - Error minterApiCoroutines.transaction(Mt3fc090e6a9b28dbdc1446ebd2b3c208c6b15b1680e7352af2de9763e9f423cc9)
[main] ERROR CoroutinesApi - Error minterApiCoroutines.transaction(Mtae71ad8b074aed9068839ac6b62f0aa1d333e3c5848d46a1642e23ddf539bcf7)
10000 transactions in 569 ms, error read 2 blocks, avg 17575 transaction/second
10000 transactions in 516 ms, avg 19380 transaction/second
10000 transactions in 504 ms, avg 19841 transaction/second
10000 transactions in 527 ms, avg 18975 transaction/second
10000 transactions in 491 ms, avg 20367 transaction/second
10000 transactions in 518 ms, avg 19305 transaction/second
10000 transactions in 497 ms, avg 20121 transaction/second
|--------------------------------------------------------------
| Total: load 99998 transactions in 5402 ms, avg 18511 transaction/second
|--------------------------------------------------------------
```