# minter-node-benchmark
Minter Node Benchmark

## Running from source
```shell
git clone https://github.com/counters/minter-node-benchmark.git
cd minter-node-benchmark
./gradlew run
```

## Run from precompiled jar file
```shell
wget -O MinterNodeBenchmark.main.jar https://github.com/counters/minter-node-benchmark/releases/download/v1.0.0/MinterNodeBenchmark.main.jar
wget -O config.yml https://github.com/counters/minter-node-benchmark/raw/v1.0.0/config.yml
java -Dfile.encoding=UTF-8 -jar MinterNodeBenchmark.main.jar
#java -Dfile.encoding=UTF-8 -jar MinterNodeBenchmark.main.jar --config patch_to_config.yml
```

