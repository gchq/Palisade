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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HDFSResourceServiceTest {


    public static final String FORMAT_VALUE = "txt";
    public static final String TYPE_VALUE = "bob";
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceServiceTest.class);
    public static final String FILE_NAME_VALUE_00002 = "00002";

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
    private String inputPathString;
    private FileSystem fs;
    private HashMap<Resource, ConnectionDetail> expected;
    public static final String FILE_NAME_VALUE_00001 = "00001";
    private HashMap<String, ConnectionDetail> dataFormat;
    private HashMap<String, ConnectionDetail> dataType;
    private final SimpleConnectionDetail mockFormatDetails = Mockito.mock(SimpleConnectionDetail.class);
    private final SimpleConnectionDetail mockTypeDetails = (SimpleConnectionDetail) Mockito.mock(SimpleConnectionDetail.class);

    @Before
    public void setUp() throws Exception {
        conf = createConf();
        inputPathString = testFolder.getRoot().getAbsolutePath() + "/inputDir";
        fs = FileSystem.get(conf);
        fs.mkdirs(new Path(inputPathString));
        expected = Maps.newHashMap();
        dataFormat = new HashMap<>();
        dataFormat.put(FORMAT_VALUE, mockFormatDetails);
        dataType = new HashMap<>();
        dataType.put(TYPE_VALUE, mockTypeDetails);
    }

    @Test
    public void shouldGetResourcesByIdOfAFile() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00002", FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(id, TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest("file:///" + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final String found = "hdfs:///" + id;
        try {
            final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest(found));
            fail("exception expected");
        } catch (Exception e) {
            //then
            assertEquals(String.format(HDFSResourceService.ERROR_OUT_SCOPE, found, conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)), e.getMessage());
        }
    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest("file:///" + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByType() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00003", FORMAT_VALUE, TYPE_VALUE + 2);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByType(new GetResourcesByTypeRequest(TYPE_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }


    @Test
    public void shouldGetResourcesByFormat() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00003", FORMAT_VALUE + 2, TYPE_VALUE);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockFormatDetails);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockFormatDetails);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByFormat(new GetResourcesByFormatRequest(FORMAT_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);
        expected.put(new FileResource(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE), TYPE_VALUE, FORMAT_VALUE), mockTypeDetails);
        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<Resource, ConnectionDetail>> resourcesById = service.getResourcesByResource(new GetResourcesByResourceRequest(new DirectoryResource("file:///" + id)));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void testAddResource() throws Exception {
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        try {
            service.addResource(null);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals(HDFSResourceService.ADD_RESOURCE_ERROR, e.getMessage());
        }
    }

    @Test
    public void shouldJSONSerialiser() throws Exception {
        dataFormat.clear();
        dataType.clear();

        dataFormat.put("testKey1", new SimpleConnectionDetail("myDetails"));
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final byte[] serialise = JSONSerialiser.serialise(service, true);

        final String expected = "{\n" +
                "  \"class\" : \"uk.gov.gchq.palisade.resource.service.HDFSResourceService\",\n" +
                "  \"dataFormat\" : {\n" +
                "    \"testKey1\" : {\n" +
                "      \"class\" : \"uk.gov.gchq.palisade.service.request.SimpleConnectionDetail\",\n" +
                "      \"details\" : \"myDetails\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"dataType\" : { },\n" +
                "  \"jobConf\" : {\n" +
                "  }\n" +
                "}\n";

        final String StringOfSerialised = new String(serialise);
        final String[] split = StringOfSerialised.split(System.lineSeparator());
        final StringBuilder modified = new StringBuilder();
        for (String s : split) {
            if (!s.startsWith("    \"fs.defaultFS")) {
                modified.append(s).append(System.lineSeparator());
            }
        }

        final String modifiedActual = modified.toString();
        assertEquals(StringOfSerialised, expected, modifiedActual);
        assertEquals(StringOfSerialised, service, JSONSerialiser.deserialise(serialise, HDFSResourceService.class));
    }

    private void writeFile(final FileSystem fs, final String parentPath, final String name, final String format, final String type) throws IOException {
        writeFile(fs, parentPath + "/" + getFileNameFromResourceDetails(name, type, format));
    }

    private void writeFile(final FileSystem fs, final String filePathString) throws IOException {
        //Write Some file
        final Path filePath = new Path(filePathString);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(filePath, true)))) {
            writer.write("myContents");
        }
    }

    private JobConf createConf() {
        // Set up local conf
        final JobConf conf = new JobConf();
        conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT + testFolder.getRoot().getAbsolutePath());
        return conf;
    }

    private static String getFileNameFromResourceDetails(final String name, final String type, final String format) {
        //Type, Id, Format
        return String.format(HDFSResourceDetails.FILE_NAME_FORMAT, type, name, format);
    }
}
