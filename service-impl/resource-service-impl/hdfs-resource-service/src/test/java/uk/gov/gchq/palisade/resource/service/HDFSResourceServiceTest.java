package uk.gov.gchq.palisade.resource.service;

import com.google.common.collect.Maps;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
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
import static org.junit.Assert.fail;
import static uk.gov.gchq.palisade.resource.service.HDFSResourceDetails.getFileNameFromResourceDetails;

public class HDFSResourceServiceTest {


    public static final String FORMAT_VALUE = "txt";
    public static final String TYPE_VALUE = "bob";
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceServiceTest.class);

    public static File TMP_DIRECTORY;

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
    private JobConf conf;
    private Path inputPath;
    private FileSystem fs;
    private HashMap<Resource, ConnectionDetail> expected;
    public static final String ID_VALUE_00001 = "00001";

    @Before
    public void setUp() throws Exception {
        conf = createConf();
        conf.set(HDFSResourceService.RESOURCE_ROOT_PATH, testFolder.getRoot().getAbsolutePath());
        inputPath = new Path(conf.get(HDFSResourceService.RESOURCE_ROOT_PATH) + "/inputDir");
        fs = FileSystem.get(createConf());
        fs.mkdirs(inputPath);
        expected = Maps.newHashMap();
    }

    @Test
    public void shouldGetResourcesById() throws Exception {
        //given
        writeFile(inputPath, fs, ID_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(ID_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest("00001"));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByType() throws Exception {
        //given
        writeFile(inputPath, fs, ID_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(ID_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByType(new GetResourcesByTypeRequest(TYPE_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByFormat() throws Exception {
        //given
        writeFile(inputPath, fs, ID_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(ID_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByFormat(new GetResourcesByFormatRequest(FORMAT_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given
        writeFile(inputPath, fs, ID_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(ID_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), new NullConnectionDetail());

        //when
        final HDFSResourceService service = new HDFSResourceService(conf);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByResource(new GetResourcesByResourceRequest(new FileResource(ID_VALUE_00001)));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void testAddResource() throws Exception {
        final HDFSResourceService service = new HDFSResourceService(conf);
        try {
            service.addResource(null);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals(HDFSResourceService.ADD_RESOURCE_ERROR, e.getMessage());
        }
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
