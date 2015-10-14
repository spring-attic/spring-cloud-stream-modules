/*
 * Copyright 2015 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.cassandra.sink;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.module.cassandra.test.domain.Book;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.fn.Supplier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CassandraSinkApplication.class)
@DirtiesContext
public abstract class CassandraSinkIntegrationTests {

	public static final String CASSANDRA_KEYSPACE = "cassandraTest";

	private static final String CASSANDRA_STORAGE_CONFIG = "spring-cassandra.yaml";

	private static final int PORT = 9043; // See spring-cassandra.yaml - native_transport_port

    @Autowired
    @Bindings(CassandraSink.class)
    protected Sink sink;

    @Autowired
    ConfigurableApplicationContext applicationContext;

	private static Cluster cluster;

	private static CassandraOperations cassandraTemplate;

	@BeforeClass
	public static void setUp() throws ConfigurationException, IOException, TTransportException {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra(CASSANDRA_STORAGE_CONFIG, "target/embeddedCassandra");
		cluster = Cluster.builder()
				.addContactPoint("localhost")
				.withPort(PORT)
				.build();

		cluster.connect().execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s" +
				"  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };", CASSANDRA_KEYSPACE));

		cassandraTemplate = new CassandraTemplate(cluster.connect(CASSANDRA_KEYSPACE));

	}

	@AfterClass
	public static void cleanup() {
		if (cluster != null) {
			cluster.close();
		}
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}

	@WebIntegrationTest({"spring.cassandra.port=9043", 
		"spring.cassandra.keyspace=cassandraTest",
		"spring.cassandra.init-script=int-db.cql",
		"spring.cassandra.entity-base-packages=org.springframework.cloud.stream.module.cassandra.test.domain",
		"query-type=INSERT"})
	public static class CassandraEntityInsertTests extends CassandraSinkIntegrationTests {

		@Test
		public void testInsert() throws InterruptedException {
			Book book = new Book();
			book.setIsbn(UUID.randomUUID());
			book.setTitle("Spring Integration Cassandra");
			book.setAuthor("Cassandra Guru");
			book.setPages(521);
			book.setSaleDate(new Date());
			book.setInStock(true);
	
			sink.input().send(new GenericMessage<>(book));
	
			final Select select = QueryBuilder.select().all().from("book");
	
			assertEqualsEventually(1, new Supplier<Integer>() {
	
				@Override
				public Integer get() {
					return cassandraTemplate.select(select, Book.class).size();
				}
	
			});
	
			cassandraTemplate.delete(book);
		}
	}


	@WebIntegrationTest({"spring.cassandra.port=9043", 
		"spring.cassandra.keyspace=cassandraTest",
		"spring.cassandra.init-script=int-db.cql",
		"query-type=INSERT",
		"ingest-query=insert into book (isbn, title, author, pages, saleDate, inStock) values (?, ?, ?, ?, ?, ?)"})
	public static class CassandraSinkIngestTests extends CassandraSinkIntegrationTests {

		@Test
		public void testIngestQuery() throws Exception {
			List<Book> books = getBookList(5);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			Jackson2JsonObjectMapper mapper = new Jackson2JsonObjectMapper(objectMapper);

			sink.input().send(new GenericMessage<>(mapper.toJson(books)));

			final Select select = QueryBuilder.select().all().from("book");

			assertEqualsEventually(5, new Supplier<Integer>() {

				@Override
				public Integer get() {
					return cassandraTemplate.select(select, Book.class).size();
				}

			});

			cassandraTemplate.truncate("book");
		}
	}

	
	private static List<Book> getBookList(int numBooks) {

		List<Book> books = new ArrayList<>();

		Book b;
		for (int i = 0; i < numBooks; i++) {
			b = new Book();
			b.setIsbn(UUID.randomUUID());
			b.setTitle("Spring XD Guide");
			b.setAuthor("XD Guru");
			b.setPages(i * 10 + 5);
			b.setInStock(true);
			b.setSaleDate(new Date());
			books.add(b);
		}

		return books;
	}

	private static <T> void assertEqualsEventually(T expected, Supplier<T> actualSupplier) throws InterruptedException {
		int n = 0;
		while (!actualSupplier.get().equals(expected) && n++ < 100) {
			Thread.sleep(100);
		}
		assertTrue(n < 10);
	}

}
