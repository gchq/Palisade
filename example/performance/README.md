# Performance

Palisade includes a performance tool for testing some simple scenarios. It uses the example data and generates some fake HR data using
the [HR data generator](../hr-data-generator/README.md).

The tool can run various performance tests, some operate on
a large data file and some operate on a smaller data file. You can set the sizes for each of these.

The tests will read the files natively, i.e. without Palisade and then read the same files by using Palisade, both with and
without a redaction policy set. The test names state briefly what is being tested, e.g. "read_small_native" is a read of
the smaller data file without using Palisade, "read_large_with_policy" is a read of the larger file via Palisade with policy enforced.
The general naming scheme is "test_size_variant".

Once you have completed [building](#building) below, you can get detail on each test by running:
```bash
./example/deployment/bash-scripts/perf.sh help run
```

## Usage

### Building
<a name="building"></a>
To use the performance tool, you must first build Palisade (from the Palisade root directory):
```bash
mvn clean install -P example
./example/deployment/local-jvm/bash-scripts/buildServices.sh
```
You can then run the following command to start the Palisade services running on your local machine.
```bash
./example/deployment/local-jvm/bash-scripts/startAllServices.sh
```
Wait for those to initialize, then switch to a new terminal:
```bash
./example/deployment/bash-scripts/perf.sh
```

### Setting up
Without arguments, the tool will give some usage information.

The first step is to create some data for the tool to test against. For the performance tests, we use a "small" file and a "large" file.
You can decide the number of HR records to go in the small and large files (depending on how long you want the tests to run!), or even
make them the same size!

First, we tell Palisade where its bootstrap configuration file is:
```bash
export PALISADE_REST_CONFIG_PATH=./example/example-model/src/main/resources/configRest.json
```

Now, to create some test data in a `perf_test` directory:

```bash
mkdir perf_test
./example/deployment/bash-scripts/perf.sh 1000 10000 perf_test
```

This will produce output similar to:
```bash
example.perf.Perf INFO  - Going to create 1000 records in file employee_small.avro and 10000 records in file employee_large.avro in sub-directory
example.perf.Perf INFO  - Creation tasks submitted...
example.perf.Perf INFO  - Small file written successfully true
example.perf.Perf INFO  - Large file written successfully true
example.perf.Perf INFO  - Copying small file
example.perf.Perf INFO  - Copying large file
```

We must then instruct Palisade to create some compliance policies regarding these files:
```bash
./example/deployment/bash-scripts/perf.sh policy perf_test
```
This then produces:
```bash
example.perf.Perf INFO  - Specified path perf_test has been normalised to file:///<user home directory>/Palisade/perf_test/
example.perf.Perf INFO  - Security policy has been set for file:///<user home directory>//Palisade/perf_test/employee_small.avro: true
example.perf.Perf INFO  - Security policy has been set for file:///<user home directory>//Palisade/perf_test/large/employee_large.avro: true
example.perf.Perf INFO  - Security policy has been set for file:///<user home directory>/Palisade/perf_test/employee_small-nopolicy.avro: true
example.perf.Perf INFO  - Security policy has been set for file:///<user home directory>//Palisade/perf_test/large/employee_large-nopolicy.avro: true
```

### Running

Now we can run the performance tests:
```bash
./example/deployment/bash-scripts/perf.sh run perf_test 2 5
```
The first number is the number of "dry runs" to perform; these are tests where the results are discarded to allow JVM optimisation/class loading
occur and not interfere with the results. The second number is the number of live runs to perform.

Depending on the size of the files you created, this may take some time to run. Output will be similar to the following:
```bash
example.perf.Perf INFO  - Specified path perf_test has been normalised to file:///<user home directory>/Palisade/perf_test/
example.perf.Perf INFO  - Starting dry runs
Starting test read_large_native:10000

<<< output snipped >>>

example.perf.Perf INFO  - Starting live tests
Starting test read_large_native:10000

<<< output snipped >>>

All times in seconds.

Test                              # trials         Min         Max        Mean    Std.dev.         25%         50%         75%         99%        Norm
read_small_native                    5.000       0.064       0.081       0.072       0.007       0.066       0.069       0.078       0.081       1.000
read_large_native                    5.000       0.660       1.199       0.840       0.198       0.709       0.727       0.906       1.187       1.000
request_small_with_policy            5.000       0.114       0.242       0.190       0.047       0.156       0.215       0.221       0.241       0.000
read_small_no_policy                 5.000       0.158       0.500       0.234       0.133       0.163       0.170       0.180       0.487       3.264
read_small_with_policy               5.000       0.266       0.507       0.384       0.103       0.271       0.382       0.491       0.507       5.348
read_large_with_policy               5.000       1.609       3.603       2.128       0.749       1.624       1.876       1.930       3.536       2.533
read_large_no_policy                 5.000       1.360       1.759       1.513       0.147       1.415       1.430       1.602       1.753       1.801
```
The tool reports several statistics, but the most useful are the mean and standard deviation. The percentage columns are the various
percentile levels. The "Norm" column is the normalised column, showing how long various tests took compared to reading the files
natively (without Palisade); tests against large and small files are normalised to the corresponding native test.
