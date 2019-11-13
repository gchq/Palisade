package uk.gov.gchq.palisade.resource.service.impl;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.test.PathUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EgeriaResourceServiceTest {

    public static final String FILE = System.getProperty("os.name").toLowerCase().startsWith("win") ? "file:///" : "file://";
    private static final Logger LOGGER = LoggerFactory.getLogger(EgeriaResourceServiceTest.class);
    public static File TMP_DIRECTORY;

    static {
        TMP_DIRECTORY = PathUtils.getTestDir(EgeriaResourceServiceTest.class);
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TMP_DIRECTORY);
    private EgeriaResourceService resourceService;
    private SimpleConnectionDetail simpleConnection;
    private Configuration conf;
    private String inputPathString;
    private FileSystem fs;
    private SimpleCacheService simpleCache;

    public EgeriaResourceServiceTest() {
    }

    private static String getFileNameFromResourceDetails(final String name, final String type, final String format) {
        //Type, Id, Format
        return String.format(HadoopResourceDetails.FILE_NAME_FORMAT, type, name, format);
    }

    @Before
    public void setUp() throws InvalidParameterException, IOException {
        System.setProperty("hadoop.home.dir", Paths.get(".").toAbsolutePath().normalize().toString() + "/src/test/resources/hadoop-3.0.0");
        conf = createConf();
        inputPathString = testFolder.getRoot().getAbsolutePath() + "/inputDir";
        fs = FileSystem.get(conf);
        fs.mkdirs(new Path(inputPathString));
        simpleConnection = new SimpleConnectionDetail().service(new MockDataService());
        simpleCache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));

        resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:18081", conf, simpleCache);
    }

    private Configuration createConf() {
        // Set up local conf
        final Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, FILE + testFolder.getRoot().getAbsolutePath().replace("\\", "/"));
        return conf;
    }

    @Test
    public void getAll() {
        AssetConsumer assetConsumer;
        List<AssetUniverse> assetUni = new ArrayList();
        try {
            assetConsumer = new AssetConsumer("cocoMDS1", "http://localhost:18081");
            List<String> assetUniverse = assetConsumer.findAssets("peterprofile", ".*file.*", 5, 10);
            assetUniverse.forEach((guid) -> {
                try {
                    assetUni.add(assetConsumer.getAssetProperties("peterprofile", guid));
                } catch (PropertyServerException e) {
                    LOGGER.debug("PropertyServerException: " + e);
                } catch (UserNotAuthorizedException e) {
                    LOGGER.debug("UserNotAuthorizedException: " + e);
                } catch (InvalidParameterException e) {
                    LOGGER.debug("InvalidParameterException: " + e);
                }
            });
            System.out.println(assetUni.size());
            System.out.println(assetUni.get(0));
            System.out.println(assetUni.get(0).getAssetTypeName());

        } catch (InvalidParameterException e) {
            LOGGER.debug("InvalidParameterException: " + e);
        } catch (PropertyServerException e) {
            LOGGER.debug("PropertyServerException: " + e);
        } catch (UserNotAuthorizedException e) {
            LOGGER.debug("UserNotAuthorizedException: " + e);
        }
    }

    @Test
    public void getResourcesByIdTest() {
        //given
        UserId peter = new UserId().id("peterprofile");
        //when
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek3.csv").userId(peter);
        resourceService.getResourcesById(idRequest);
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByType() {
        //given

        //when

        //then
    }

    @Test
    public void shouldBeEqualAfterConfigure() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByFormat() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByResource() {
        //given

        //when

        //then
    }

    @Test
    public void testAddResource() {
    }

    @Test
    public void shouldJSONSerialiser() {
    }

    @Test
    public void shouldErrorWithNoConnectionDetails() {
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() {
    }

    @Test
    public void shouldResolveParents() {
    }

}
