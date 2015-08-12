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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;

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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HdfsSinkApplication.class)
@WebIntegrationTest({"server.port:0",
        "spring.hadoop.fsUri=file:///",
        "directory=${java.io.tmpdir}/hdfs-sink"})
@DirtiesContext
public class HdfsSinkIntegrationTests {

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
    public void testWritingSomething() throws IOException {
        sink.input().send(new GenericMessage<>("Foo"));
        sink.input().send(new GenericMessage<>("Bar"));
        sink.input().send(new GenericMessage<>("Baz"));
    }

    @After
    public void checkFilesClosedOK() throws IOException {
        applicationContext.close();
        File testOutput = new File(testDir);
        assertTrue(testOutput.exists());
        File[] files = testOutput.listFiles();
        File dataFile = null;
        for (File f : files) {
            if (dataFile == null && f.getAbsolutePath().endsWith(".txt")) {
                dataFile = f;
            }
        }
        assertTrue(files.length > 0);
        assertNotNull(dataFile);
        Assert.assertThat(readFile(dataFile.getPath(), Charset.forName("UTF-8")), equalTo("Foo\nBar\nBaz\n"));
    }

    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
