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

import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
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
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HDFSFileResourceServiceTest {


    public static final String FORMAT_VALUE = "txt";
    public static final String TYPE_VALUE = "bob";
    public static final String FILE_NAME_VALUE_00001 = "00001";
    public static final String FILE_NAME_VALUE_00002 = "00002";
    public static final String FILE = "file:///";
    public static final String HDFS = "hdfs:///";
    public static File TMP_DIRECTORY;
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TMP_DIRECTORY);

    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSFileResourceServiceTest.class);
    private SimpleConnectionDetail simpleFormat;
    private SimpleConnectionDetail simpleType;
    private HashMap<String, ConnectionDetail> dataFormat;
    private HashMap<String, ConnectionDetail> dataType;
    private JobConf conf;
    private String inputPathString;
    private FileSystem fs;
    private HashMap<uk.gov.gchq.palisade.resource.Resource, ConnectionDetail> expected;

    static {
        final String tmpDirectoryProperty = System.getProperty("java.io.tmpdir");

        if (null != tmpDirectoryProperty) {
            TMP_DIRECTORY = new File(tmpDirectoryProperty);
        } else {
            LOGGER.warn("Could not determine default temporary directory, using current directory.");
            TMP_DIRECTORY = new File(".");
        }
    }

    public HDFSFileResourceServiceTest() {
    }

    @Before
    public void setUp() throws Exception {
        conf = createConf();
        inputPathString = testFolder.getRoot().getAbsolutePath() + "/inputDir";
        fs = FileSystem.get(conf);
        fs.mkdirs(new Path(inputPathString));
        expected = Maps.newHashMap();
        dataFormat = new HashMap<>();
        simpleFormat = new SimpleConnectionDetail().service(new MockDataService());
        dataFormat.put(FORMAT_VALUE, simpleFormat);
        dataType = new HashMap<>();
        simpleType = new SimpleConnectionDetail().service(new MockDataService());
        dataType.put(TYPE_VALUE, simpleType);
    }

    @Test
    public void shouldGetResourcesByIdOfAFile() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final String found = HDFS + "/unknownDir/" + id;
        try {
            service.getResourcesById(new GetResourcesByIdRequest().resourceId(found));
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
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

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
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesByType(new GetResourcesByTypeRequest().type(TYPE_VALUE));

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
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesBySerialisedFormat(new GetResourcesBySerialisedFormatRequest().serialisedFormat(FORMAT_VALUE));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);
        expected.put(new FileResource().id(id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleType);
        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesByResource(new GetResourcesByResourceRequest().resource(new DirectoryResource().id("file:///" + id)));

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
            assertEquals(HDFSResourceService.ERROR_ADD_RESOURCE, e.getMessage());
        }
    }

    @Test
    public void shouldJSONSerialiser() throws Exception {
        dataFormat.clear();
        dataType.clear();

        dataFormat.put("testKey1", new SimpleConnectionDetail().service(new MockDataService()));
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final byte[] serialise = JSONSerialiser.serialise(service, true);

        final String expected = "{\n" +
                "  \"@id\" : 1,\n" +
                "  \"class\" : \"uk.gov.gchq.palisade.resource.service.HDFSResourceService\",\n" +
                "  \"conf\" : {\n" +
                "  },\n" +
                "  \"dataFormat\" : {\n" +
                "    \"testKey1\" : {\n" +
                "      \"class\" : \"uk.gov.gchq.palisade.service.request.SimpleConnectionDetail\",\n" +
                "      \"service\" : {\n" +
                "        \"class\" : \"uk.gov.gchq.palisade.data.service.impl.MockDataService\",\n" +
                "        \"@id\" : 2,\n" +
                "        \"class\" : \"uk.gov.gchq.palisade.data.service.impl.MockDataService\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"dataType\" : { }\n" +
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

    @Test
    public void shouldErrorWithNotConnectionDetails() throws Exception {
        //given
        dataFormat.clear();
        dataType.clear();
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE), simpleType);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        try {
            final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));
            resourcesById.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            //then
            assertEquals(String.format(HDFSResourceService.ERROR_DETAIL_NOT_FOUND, TYPE_VALUE, FORMAT_VALUE), e.getCause().getMessage());
        }
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() throws Exception {
        //given
        dataType.clear();
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(inputPathString).parent(
                        new SystemResource().id(testFolder.getRoot().getAbsolutePath())
                )
        ), simpleFormat);

        //when
        final HDFSResourceService service = new HDFSResourceService(conf, dataFormat, dataType);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = service.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }


    @Test
    public void shouldResolveParents() throws Exception {
        final String parent = testFolder.getRoot().getAbsolutePath() + "/inputDir" + "/" + "folder1" + "/" + "folder2";
        final String id = parent + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        final FileResource fileResource = new FileResource().id(id);
        HDFSResourceService.resolveParents(fileResource, conf);

        final ParentResource parent1 = fileResource.getParent();
        assertEquals(parent, parent1.getId());

        assertTrue(parent1 instanceof ChildResource);
        assertTrue(parent1 instanceof DirectoryResource);
        final ChildResource child = (ChildResource) parent1;
        HDFSResourceService.resolveParents(child, conf);
        final ParentResource parent2 = child.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath() + "/inputDir" + "/" + "folder1", parent2.getId());

        assertTrue(parent2 instanceof ChildResource);
        assertTrue(parent2 instanceof DirectoryResource);
        final ChildResource child2 = (ChildResource) parent2;
        HDFSResourceService.resolveParents(child2, conf);
        final ParentResource parent3 = child2.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath() + "/inputDir", parent3.getId());

        assertTrue(parent3 instanceof ChildResource);
        assertTrue(parent3 instanceof DirectoryResource);
        final ChildResource child3 = (ChildResource) parent3;
        HDFSResourceService.resolveParents(child3, conf);
        final ParentResource parent4 = child3.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath(), parent4.getId());

        assertTrue(parent4 instanceof SystemResource);
        assertFalse(parent4 instanceof DirectoryResource);
        final SystemResource sysRes = (SystemResource) parent4;
        assertEquals(testFolder.getRoot().getAbsolutePath(), sysRes.getId());


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
