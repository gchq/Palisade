/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
//import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import uk.gov.gchq.palisade.example.common.Purpose;
//import uk.gov.gchq.palisade.example.config.ServicesCreator;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.config.service.ConfigConsts;
import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
//import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
//import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
//import uk.gov.gchq.palisade.example.config.ServicesConfigurator;
//import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.mapreduce.PalisadeInputFormat;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Optional;
//import java.util.stream.Stream;

import static java.util.Objects.isNull;

/**
 * An example of a MapReduce job using example data from Palisade. This sets up a Palisade service which can serve
 * example data. The job is then configured to make a request as part of the MapReduce job. The actual MapReduce job is
 * a simple word count example.
 * <p>
 * The word count example is adapted from: https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
 */
public class AwsEmrMapReduceExample extends Configured implements Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsEmrMapReduceExample.class);

    protected static final String DEFAULT_OUTPUT_DIR = createOutputDir();
    private static final String RESOURCE_TYPE = "Employee";

    /**
     * This simple mapper example just extracts the property field of the example object and emits a count of one.
     */
    private static class ExampleMap extends Mapper<LeafResource, Employee, Text, IntWritable> {
        private static final IntWritable ONE = new IntWritable(1);

        private Text outputKey = new Text();

        protected void map(final LeafResource key, final Employee value, final Context context) throws IOException, InterruptedException {
            //String property = value.getName();
            String property = value.getTaxCode();
            if (property != null && !property.isEmpty()) {
                outputKey.set(property);
                context.write(outputKey, ONE);
            }
        }
    }

    /**
     * This simple reducer example counts up the number of times we have seen each value.
     */
    private static class ExampleReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(final Text key, final Iterable<IntWritable> values, final Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            //output the totalled value
            result.set(sum);
            context.write(key, result);
        }
    }

    @Override
    public int run(final String... args) throws Exception {
        //usage check
        if (args.length < 2) {
            System.out.println("Example file and MapReduce output directory not specified. Please provide path as argument.");
            return 1;
        }

        String sourceFile = args[0];

        //create the basic job object and configure it for this example
        Job job = Job.getInstance(getConf(), "Palisade MapReduce Example");
        job.setJarByClass(AwsEmrMapReduceExample.class);

        //configure mapper
        job.setMapperClass(ExampleMap.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        //configure reducer
        job.setReducerClass(ExampleReduce.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //set the output format
        job.setOutputFormatClass(TextOutputFormat.class);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // copied from RestExample
        String confString = System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH);
        final InputStream stream = StreamUtil.openStream(this.getClass(), System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        //System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        ServiceState clientConfig = null;
        int times = 0;
        while (isNull(clientConfig) && times < 30) {
            try {
                clientConfig = new Configurator(configService).retrieveConfig(Optional.empty());
            } catch (NoConfigException e) {
                LOGGER.warn("No client configuration present, waiting...");
                Thread.sleep(ConfigConsts.DELAY);
                times++;
            }
        }
        if (isNull(clientConfig)) {
            throw new RuntimeException("Couldn't retrieve client configuration. Is configuration service running?");
        }
        PalisadeService palisade = Configurator.createFromConfig(PalisadeService.class, clientConfig);
        final ExampleSimpleClient client = new ExampleSimpleClient(palisade);
        // end of copied from RestExample

        // Edit the configuration of the Palisade requests below here
        // ==========================================================
        configureJob(job, palisade, 2);

        //next add a resource request to the job
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Alice", Purpose.SALARY.name());
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Alice", Purpose.DUTY_OF_CARE.name());
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Alice", Purpose.STAFF_REPORT.name());
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Bob", Purpose.DUTY_OF_CARE.name());
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Bob", "");
        addDataRequest(job, sourceFile, RESOURCE_TYPE, "Eve", "");

        //launch job
        boolean success = job.waitForCompletion(true);

        return (success) ? 0 : 1;
    }

    /**
     * Configures the given job to use this example client.
     *
     * @param job        the job to configure
     * @param services   the Palisade services factory
     * @param maxMapHint the hint for the maximum number of mappers
     */
    public static void configureJob(final Job job, final PalisadeService services, final int maxMapHint) {
        job.setInputFormatClass(PalisadeInputFormat.class);
        //tell it which Palisade service to use
        PalisadeInputFormat.setPalisadeService(job, services);
        //configure the serialiser to use
        PalisadeInputFormat.setSerialiser(job, new AvroSerialiser<Employee>(Employee.class));
        //set the maximum mapper hint
        PalisadeInputFormat.setMaxMapTasksHint(job, maxMapHint);
    }

    /**
     * Utility method to add a read request to a job.
     *
     * @param context       the job to add the request to
     * @param filename      example filename
     * @param resourceType  the example resource type
     * @param userId        the example user id
     * @param justification the example justification
     */
    public static void addDataRequest(final JobContext context, final String filename, final String resourceType, final String userId, final String justification) {
        final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(filename).userId(new UserId().id(userId)).context(new Context().purpose(justification));
        PalisadeInputFormat.addDataRequest(context, dataRequest);
    }


    public static void main(final String... args) throws Exception {
        final String outputDir;
        if (args.length < 1) {
            System.out.printf("Usage: %s input_file [output_directory]\n", AwsEmrMapReduceExample.class.getTypeName());
            System.out.println("\nfile\tfile containing serialised Employee instances to read");
            System.out.println("output_directory\tdirectory to write mapreduce outputs to");
            System.exit(1);
        }



        String sourceFile = args[0];

        if (args.length < 2) {
            outputDir = DEFAULT_OUTPUT_DIR;
        } else {
            outputDir = args[1];
        }
        //remove this as it needs to be not present when the job runs
        FileUtils.deleteDirectory(new File(outputDir));
        InetAddress ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        Configuration conf = new Configuration();
        //Set job tracker to local implementation - REMOVE THIS FOR RUNNING IN DISTRIBUTED MODE
        //conf.set("mapred.job.tracker", "local");
        //conf.set("mapreduce.framework.name", "local");
        conf.set("mapreduce.framework.name", "yarn");
        //Set file system to local implementation and set the root to current directory - REMOVE IN DISTRIBUTED MODE
        //conf.set(CommonConfigurationKeys.FS_DEFAULT_NAME_KEY, new File(".").toURI().toURL().toString());
        //set defaultFS to hdfs on cluster
        conf.set("fs.defaultFS", ("hdfs://" + hostname));
        //conf.set("fs.defaultFS", ("hdfs://localhost"));           // uncomment to run locally rather than on cluster
        //set address of resource manager
        conf.set("yarn.resourcemanager.address", (hostname + ":8032"));
        //conf.set("yarn.resourcemanager.address", ("localhost:8032"));          // uncomment to run locally rather than on cluster
        ToolRunner.run(conf, new AwsEmrMapReduceExample(), new String[]{sourceFile, outputDir});
    }

    private static String createOutputDir() {
        try {
            return Files.createTempDirectory("mapreduce-example-").toAbsolutePath().toString();
        } catch (IOException e) {
            LOGGER.error("Failed to create an output directory.");
            throw new RuntimeException(e);
        }
    }
}
