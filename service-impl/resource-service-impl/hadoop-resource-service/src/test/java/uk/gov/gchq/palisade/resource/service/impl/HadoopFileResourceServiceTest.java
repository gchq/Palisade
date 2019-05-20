package uk.gov.gchq.palisade.resource.service.impl;

import com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
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
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

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

public class HadoopFileResourceServiceTest {


    public static final String FORMAT_VALUE = "txt";
    public static final String TYPE_VALUE = "bob";
    public static final String FILE_NAME_VALUE_00001 = "00001";
    public static final String FILE_NAME_VALUE_00002 = "00002";
    public static final String FILE = "file:///";
    public static final String HDFS = "hdfs:///";
    public static File TMP_DIRECTORY;
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TMP_DIRECTORY);

    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopFileResourceServiceTest.class);
    private SimpleConnectionDetail simpleConnection;
    private Configuration conf;
    private String inputPathString;
    private FileSystem fs;
    private HashMap<uk.gov.gchq.palisade.resource.Resource, ConnectionDetail> expected;
    private SimpleCacheService simpleCache;

    private HadoopResourceService hadoopService;

    static {
        final String tmpDirectoryProperty = System.getProperty("java.io.tmpdir");

        if (null != tmpDirectoryProperty) {
            TMP_DIRECTORY = new File(tmpDirectoryProperty);
        } else {
            LOGGER.warn("Could not determine default temporary directory, using current directory.");
            TMP_DIRECTORY = new File(".");
        }
    }

    public HadoopFileResourceServiceTest() {
    }

    @Before
    public void setUp() throws Exception {
        conf = createConf();
        inputPathString = testFolder.getRoot().getAbsolutePath() + "/inputDir";
        fs = FileSystem.get(conf);
        fs.mkdirs(new Path(inputPathString));
        expected = Maps.newHashMap();
        simpleConnection = new SimpleConnectionDetail().service(new MockDataService());

        simpleCache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));

        hadoopService = new HadoopResourceService(conf, simpleCache);
        hadoopService.addDataService(simpleConnection);
    }

    @Test
    public void shouldGetResourcesByIdOfAFile() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id("file://" + id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

//    @Test
//    public void shouldGetResourcesOutsideOfScope() throws Exception {
//        //given
//        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
//
//        //when
//        final String found = HDFS + "/unknownDir/" + id;
//        try {
//            hadoopService.getResourcesById(new GetResourcesByIdRequest().resourceId(found));
//            fail("exception expected");
//        } catch (Exception e) {
//            //then
//            assertEquals(String.format(HadoopResourceService.ERROR_OUT_SCOPE, found, conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)), e.getMessage());
//        }
//    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

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
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        GetResourcesByTypeRequest getResourcesByTypeRequest = new GetResourcesByTypeRequest().type(TYPE_VALUE);
        getResourcesByTypeRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByType"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesByType(getResourcesByTypeRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldBeEqualAfterConfigure() {
        //given
        ServiceState is = new ServiceState();
        hadoopService.recordCurrentConfigTo(is);

        //when
        HadoopResourceService actual = new HadoopResourceService();
        actual.applyConfigFrom(is);

        //then
        assertEquals(hadoopService, actual);
    }

    @Test
    public void shouldGetResourcesByFormat() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00003", FORMAT_VALUE + 2, TYPE_VALUE);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        GetResourcesBySerialisedFormatRequest getResourcesBySerialisedFormatRequest = new GetResourcesBySerialisedFormatRequest().serialisedFormat(FORMAT_VALUE);
        getResourcesBySerialisedFormatRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByFormat"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesBySerialisedFormat(getResourcesBySerialisedFormatRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given
        final String id = inputPathString;
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id("file://" + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        //when
        GetResourcesByResourceRequest getResourcesByResourceRequest = new GetResourcesByResourceRequest().resource(new DirectoryResource().id("file:///" + id));
        getResourcesByResourceRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByResource"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesByResource(getResourcesByResourceRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void testAddResource() throws Exception {
        try {
            hadoopService.addResource(null);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals(HadoopResourceService.ERROR_ADD_RESOURCE, e.getMessage());
        }
    }

    @Test
    public void shouldJSONSerialiser() throws Exception {
        //use local copy for this test
        final HadoopResourceService service = new HadoopResourceService(conf, simpleCache);

        final byte[] serialise = JSONSerialiser.serialise(service, true);
        final String expected = "{\n" +
                "  \"@id\" : 1,\n" +
                "  \"class\" : \"uk.gov.gchq.palisade.resource.service.impl.HadoopResourceService\",\n" +
                "  \"cacheService\" : {\n" +
                "    \"@id\" : 2,\n" +
                "    \"class\" : \"uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService\",\n" +
                "    \"backingStore\" : {\n" +
                "      \"class\" : \"uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore\",\n" +
                "      \"useStatic\" : true\n" +
                "    },\n" +
                "    \"codecs\" : { },\n" +
                "    \"maximumLocalCacheDuration\" : 300.000000000\n" +
                "  },\n" +
                "  \"conf\" : {\n" +
                "  }\n" +
                "}\n";

        final String stringOfSerialised = new String(serialise);
        final String[] split = stringOfSerialised.split(System.lineSeparator());
        final StringBuilder modified = new StringBuilder();
        for (String s : split) {
            if (!s.startsWith("    \"fs.defaultFS")) {
                modified.append(s).append(System.lineSeparator());
            }
        }

        final String modifiedActual = modified.toString();
        assertEquals(stringOfSerialised, expected, modifiedActual);
        assertEquals(stringOfSerialised, service, JSONSerialiser.deserialise(serialise, HadoopResourceService.class));
    }

    @Test
    public void shouldErrorWithNoConnectionDetails() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE), simpleConnection);

        //when
        try {
            //this test needs a local HDFS resource service
            final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = new HadoopResourceService(conf, simpleCache)
                    .getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));
            resourcesById.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            //then
            assertEquals(HadoopResourceService.ERROR_NO_DATA_SERVICES, e.getCause().getMessage());
        }
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() throws Exception {
        //given
        final String id = inputPathString + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id("file://" + id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id("file://" + inputPathString).parent(
                        new SystemResource().id("file://" + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = hadoopService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }


    @Test
    public void shouldResolveParents() throws Exception {
        final String parent = testFolder.getRoot().getAbsolutePath() + "/inputDir" + "/" + "folder1" + "/" + "folder2";
        final String id = parent + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        final FileResource fileResource = new FileResource().id(id);
        HadoopResourceService.resolveParents(fileResource, conf);

        final ParentResource parent1 = fileResource.getParent();
        assertEquals(parent, parent1.getId());

        assertTrue(parent1 instanceof ChildResource);
        assertTrue(parent1 instanceof DirectoryResource);
        final ChildResource child = (ChildResource) parent1;
        HadoopResourceService.resolveParents(child, conf);
        final ParentResource parent2 = child.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath() + "/inputDir" + "/" + "folder1", parent2.getId());

        assertTrue(parent2 instanceof ChildResource);
        assertTrue(parent2 instanceof DirectoryResource);
        final ChildResource child2 = (ChildResource) parent2;
        HadoopResourceService.resolveParents(child2, conf);
        final ParentResource parent3 = child2.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath() + "/inputDir", parent3.getId());

        assertTrue(parent3 instanceof ChildResource);
        assertTrue(parent3 instanceof DirectoryResource);
        final ChildResource child3 = (ChildResource) parent3;
        HadoopResourceService.resolveParents(child3, conf);
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

    private Configuration createConf() {
        // Set up local conf
        final Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT + testFolder.getRoot().getAbsolutePath());
        return conf;
    }

    private static String getFileNameFromResourceDetails(final String name, final String type, final String format) {
        //Type, Id, Format
        return String.format(HadoopResourceDetails.FILE_NAME_FORMAT, type, name, format);
    }
}
