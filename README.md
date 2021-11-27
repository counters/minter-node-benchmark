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

