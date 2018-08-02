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
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.palisade.example.client.ExampleMapReduceClient;
import uk.gov.gchq.palisade.service.PalisadeService;

import java.io.File;
import java.util.Iterator;

public class MapReduceExample extends Configured implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapReduceExample.class);

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        //Set job tracker to local implementation
        conf.set("mapred.job.tracker", "local");
        //Set file system to local implementation and set root to current directory
        conf.set("fs.defaultFS", new File(".").toURI().toURL().toString());
        ToolRunner.run(conf, new MapReduceExample(), args);
    }

    @Override
    public int run(String[] strings) throws Exception {
        final ExampleMapReduceClient client = new ExampleMapReduceClient();
        final PalisadeService palisadeService = client.getPalisadeService();
        FileSystem fs=FileSystem.get(getConf());
        System.out.println(fs.getClass());
        RemoteIterator<LocatedFileStatus> it=fs.listFiles(new Path("."),false);
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        return 0;
    }
}
