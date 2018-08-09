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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import uk.gov.gchq.palisade.example.client.ExampleMapReduceClient;
import uk.gov.gchq.palisade.resource.Resource;

import java.io.File;
import java.io.IOException;

/**
 * An example of a MapReduce job using example data from Palisade. This sets up a Palisade service which can serve
 * exmaple data. The job is then configured to make a request as part of the MapReduce job. The actual MapReduce job is
 * a simple word count example.
 * <p>
 * The word count example is adapted from: https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
 */
public class MapReduceExample extends Configured implements Tool {

    public static final String RESOURCE_TYPE = "exampleObj";

    /**
     * This simple mapper example just extracts the property field of the example object and emits a count of one.
     */
    private static class ExampleMap extends Mapper<Resource, ExampleObj, Text, IntWritable> {
        private static final IntWritable ONE = new IntWritable(1);

        private Text outputKey = new Text();

        protected void map(final Resource key, final ExampleObj value, final Context context) throws IOException, InterruptedException {
            String property = value.getProperty();
            outputKey.set(property);
            context.write(outputKey, ONE);
        }
    }

    /**
     * This simple reducer example counts up the number of times we have seen each value.
     */
    private static class ExampleReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(final Text key, final Iterable<IntWritable> values, final Context context) throws IOException, InterruptedException {
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
        if (args.length < 1) {
            System.out.println("Args: " + MapReduceExample.class.getName() + " <output directory path to create>");
            return 1;
        }


        //create the basic job object and configure it for this example
        Job job = Job.getInstance(getConf(), "Palisade MapReduce Example");
        job.setJarByClass(MapReduceExample.class);

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
        FileOutputFormat.setOutputPath(job, new Path(args[0]));

        // Edit the configuration of the Palisade requests below here
        // ==========================================================

        //configure the Palisade input format on an example client
        final ExampleMapReduceClient client = new ExampleMapReduceClient();

        ExampleMapReduceClient.initialiseJob(job, client, 2);

        //next add a resource request to the job
        ExampleMapReduceClient.addDataRequest(job, "file1", RESOURCE_TYPE, "Alice", "Payroll");
        ExampleMapReduceClient.addDataRequest(job, "file1", RESOURCE_TYPE, "Bob", "Payroll");

        //launch job
        boolean success = job.waitForCompletion(true);

        return (success) ? 0 : 1;
    }

    public static void main(final String... args) throws Exception {
        Configuration conf = new Configuration();
        //Set job tracker to local implementation - REMOVE THIS FOR RUNNING IN DISTRIBUTED MODE
        conf.set("mapred.job.tracker", "local");
        //Set file system to local implementation and set the root to current directory - REMOVE IN DISTRIBUTED MODE
        conf.set("fs.defaultFS", new File(".").toURI().toURL().toString());
        ToolRunner.run(conf, new MapReduceExample(), args);
    }
}
