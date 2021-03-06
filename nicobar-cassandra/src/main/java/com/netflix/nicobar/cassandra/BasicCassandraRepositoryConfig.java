/*
 *
 *  Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.nicobar.cassandra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.netflix.astyanax.Keyspace;
import com.netflix.nicobar.core.archive.GsonScriptModuleSpecSerializer;
import com.netflix.nicobar.core.archive.ScriptModuleSpecSerializer;

/**
 * Configuration provider for the {@link CassandraArchiveRepository}
 *
 * @author James Kojo
 */
public class BasicCassandraRepositoryConfig implements CassandraArchiveRepositoryConfig {
    /** default number of shards to separate the archives into */
    public static final int DEFAULT_SHARD_COUNT = 10;

    /** Default number of archives to fetch per round-trip */
    public static final int DEFAULT_FETCH_BATCH_SIZE = 10;

    /** Default module spec serializer */
    public static final ScriptModuleSpecSerializer DEFAULT_SPEC_SERIALIZER = new GsonScriptModuleSpecSerializer();

    public static class Builder {
        private String repositoryId;
        private int shardCount = DEFAULT_SHARD_COUNT;
        private int fetchBatchSize = DEFAULT_FETCH_BATCH_SIZE;
        private Path archiveOutputDirectory;
        private ScriptModuleSpecSerializer specSerializer = DEFAULT_SPEC_SERIALIZER;
        private CassandraGateway cassandraGateway;

        /** Build by constructing a cassandra gateway for the given keyspace and column family */
        public Builder(Keyspace keyspace, String columnFamily) {
            this.cassandraGateway = new CassandraGatewayImpl(keyspace, columnFamily);
        }

        /** Build with the given cassandra gateway */
        public Builder(CassandraGateway gateway) {
            this.cassandraGateway = gateway;
        }

        /** Set a unique, descriptive identifier used for reporting and display*/
        public Builder setRepositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }
        /** Number of shards or buckets the archives should be put into */
        public Builder setShardCount(int shardCount) {
            this.shardCount = shardCount;
            return this;
        }
        /** Number of archives to fetch per round-trip to the database */
        public Builder setFetchBatchSizeCount(int fetchBatchSize) {
            this.fetchBatchSize = fetchBatchSize;
            return this;
        }
        /** Output Directory for the script archives that were downloaded  */
        public Builder setArchiveOutputDirectory(Path archiveOutputDirectory) {
            this.archiveOutputDirectory = archiveOutputDirectory;
            return this;
        }
        /** Set a customer serializer for the module specification */
        public Builder setModuleSpecSerialize(ScriptModuleSpecSerializer specSerializer) {
            this.specSerializer = specSerializer;
            return this;
        }
        /** Construct the config with defaults if necessary */
        public CassandraArchiveRepositoryConfig build() throws IOException {
            Keyspace keyspace = cassandraGateway.getKeyspace();
            String columnFamilyName = cassandraGateway.getColumnFamily();
            String buildRepositoryId = repositoryId;
            if (buildRepositoryId == null) {
                buildRepositoryId = keyspace.getKeyspaceName() + "-" + columnFamilyName;
            }
            Path buildArchiveDir = archiveOutputDirectory;
            if (buildArchiveDir == null) {
                buildArchiveDir = Files.createTempDirectory("ScriptArchiveOutputDir");
            }
            return new BasicCassandraRepositoryConfig(buildRepositoryId, cassandraGateway, shardCount, fetchBatchSize, buildArchiveDir, specSerializer);
        }
    }

    private final String repositoryId;
    private final int shardCount;
    private final int fetchBatchSize;
    private final Path archiveOutputDirectory;
    private final ScriptModuleSpecSerializer moduleSpecSerializer;
    private final CassandraGateway cassandraGateway;

    protected BasicCassandraRepositoryConfig(String repositoryId, CassandraGateway gateway, int shardCount,
            int fetchBatchSize, Path archiveOutputDirectory, ScriptModuleSpecSerializer moduleSpecSerializer) {
        this.repositoryId =  Objects.requireNonNull(repositoryId, "repositoryId");
        this.cassandraGateway = Objects.requireNonNull(gateway, "cassandraGateway");
        this.shardCount = shardCount;
        this.fetchBatchSize = fetchBatchSize;
        this.archiveOutputDirectory = Objects.requireNonNull(archiveOutputDirectory, "archiveOutputDirectory");
        this.moduleSpecSerializer = Objects.requireNonNull(moduleSpecSerializer, "moduleSpecSerializer");
    }

    @Override
    public CassandraGateway getCassandraGateway() {
        return cassandraGateway;
    }

    @Override
    public int getShardCount() {
        return shardCount;
    }

    @Override
    public int getArchiveFetchBatchSize() {
        return fetchBatchSize;
    }

    @Override
    public Path getArchiveOutputDirectory() {
        return archiveOutputDirectory;
    }

    @Override
    public ScriptModuleSpecSerializer getModuleSpecSerializer() {
        return moduleSpecSerializer;
    }

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }
}
