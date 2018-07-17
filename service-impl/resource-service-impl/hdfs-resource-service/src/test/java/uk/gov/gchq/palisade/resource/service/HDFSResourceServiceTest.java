package uk.gov.gchq.palisade.resource.service;

import com.google.common.collect.Maps;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.impl.ResourceServiceTest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.NullConnectionDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static uk.gov.gchq.palisade.resource.service.HDFSResourceDetails.getFileNameFromResourceDetails;

public class HDFSResourceServiceTest extends ResourceServiceTest {


    public static final String FORMAT_VALUE = "txt";
    public static final String TYPE_VALUE = "bob";

    @Override
    public ResourceService getResourceService() throws IOException {
        return new HDFSResourceService(createConf());
    }

    static File TMP_DIRECTORY;

    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceServiceTest.class);

    static {
        final String tmpDirectoryProperty = System.getProperty("java.io.tmpdir");

        if (null != tmpDirectoryProperty) {
            TMP_DIRECTORY = new File(tmpDirectoryProperty);
        } else {
            LOGGER.warn("Could not determine default temporary directory, using current directory.");
            TMP_DIRECTORY = new File(".");
        }
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TMP_DIRECTORY);


    @Test
    public void shouldGetResourceById() throws Exception {
        //given
        final JobConf conf = createConf();
        conf.set(HDFSResourceService.RESOURCE_ROOT_PATH, testFolder.getRoot().getAbsolutePath());

        final Path inputPath = new Path(conf.get(HDFSResourceService.RESOURCE_ROOT_PATH) + "/inputDir");
        final FileSystem fs = FileSystem.get(createConf());
        fs.mkdirs(inputPath);
        final String idValue = "00001";
        writeFile(inputPath, fs, idValue, FORMAT_VALUE, TYPE_VALUE);
        final HashMap<Resource, ConnectionDetail> expected = Maps.newHashMap();
        final Resource key = new FileResource("00001", TYPE_VALUE, FORMAT_VALUE);
        expected.put(key, new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest("00001"));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourceByType() throws Exception {
        //given
        final JobConf conf = createConf();
        conf.set(HDFSResourceService.RESOURCE_ROOT_PATH, testFolder.getRoot().getAbsolutePath());

        final Path inputPath = new Path(conf.get(HDFSResourceService.RESOURCE_ROOT_PATH) + "/inputDir");
        final FileSystem fs = FileSystem.get(createConf());
        fs.mkdirs(inputPath);
        final String idValue = "00001";
        writeFile(inputPath, fs, idValue, FORMAT_VALUE, TYPE_VALUE);
        final HashMap<Resource, ConnectionDetail> expected = Maps.newHashMap();
        final Resource key = new FileResource("00001", TYPE_VALUE, FORMAT_VALUE);
        expected.put(key, new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByType(new GetResourcesByTypeRequest(TYPE_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourceByFormat() throws Exception {
        //given
        final JobConf conf = createConf();
        conf.set(HDFSResourceService.RESOURCE_ROOT_PATH, testFolder.getRoot().getAbsolutePath());

        final Path inputPath = new Path(conf.get(HDFSResourceService.RESOURCE_ROOT_PATH) + "/inputDir");
        final FileSystem fs = FileSystem.get(createConf());
        fs.mkdirs(inputPath);
        final String idValue = "00001";
        writeFile(inputPath, fs, idValue, FORMAT_VALUE, TYPE_VALUE);
        final HashMap<Resource, ConnectionDetail> expected = Maps.newHashMap();
        final Resource key = new FileResource("00001", TYPE_VALUE, FORMAT_VALUE);
        expected.put(key, new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByFormat(new GetResourcesByFormatRequest(FORMAT_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    private void writeFile(final Path inputPath, final FileSystem fs, final String id, final String txt, final String type) throws IOException {
        //Write Some file
        final HDFSResourceDetails resourceDetails = new HDFSResourceDetails(id, type, txt);

        final Path filePath = new Path(inputPath, getFileNameFromResourceDetails(resourceDetails));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(filePath, true)))) {
            writer.write("myContents");
        }
    }


    private JobConf createConf() {
        // Set up local conf
        final JobConf conf = new JobConf();
        conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT);
        return conf;
    }


}
