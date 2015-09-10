/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.hdfs.sink;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HdfsSinkApplication.class)
@WebIntegrationTest({"server.port:0",
        "spring.hadoop.fsUri=file:///",
        "directory=${java.io.tmpdir}/hdfs-sink",
        "closeTimeout=100"})
@DirtiesContext
public class HdfsSinkConfigurationTests {

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Value("${directory}")
    private String testDir;

    @Autowired
    private FsShell fsShell;

    @Autowired
    @Bindings(HdfsSink.class)
    private Sink sink;

    @Before
    public void setup() {
        if (fsShell.test(testDir)) {
            fsShell.rmr(testDir);
        }
    }

    @Test
    public void testWritingSomething() throws IOException, InterruptedException {
        sink.input().send(new GenericMessage<>("Foo"));
        Thread.sleep(150);
        sink.input().send(new GenericMessage<>("Bar"));
        sink.input().send(new GenericMessage<>("Baz"));
    }

    @After
    public void checkFilesClosedOK() throws IOException {
        applicationContext.close();
        File testOutput = new File(testDir);
        assertTrue(testOutput.exists());
        File[] files = testOutput.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".txt")) {
                    return true;
                }
                return false;
            }
        });
        assertTrue(files.length > 1);
    }

}
