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

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import uk.gov.gchq.palisade.example.client.ExampleMapReduceClient;
import uk.gov.gchq.palisade.service.PalisadeService;

public class MapReduceExample extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        new MapReduceExample().run(args);
    }

    @Override
    public int run(String[] strings) throws Exception {
        final ExampleMapReduceClient client=new ExampleMapReduceClient();
        final PalisadeService palisadeService= client.getPalisadeService();
        return 0;
    }
}
