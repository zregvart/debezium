/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.oracle.logminer.processor;

import io.debezium.config.Configuration;
import io.debezium.connector.oracle.OracleConnectorConfig;
import io.debezium.connector.oracle.junit.SkipWhenAdapterNameIsNot;
import io.debezium.connector.oracle.logminer.processor.infinispan.InfinispanLogMinerEventProcessor;
import io.debezium.connector.oracle.util.TestHelper;

/**
 * @author Chris Cranford
 */
@SkipWhenAdapterNameIsNot(value = SkipWhenAdapterNameIsNot.AdapterName.LOGMINER, reason = "Only applicable for LogMiner")
public class InfinispanProcessorTest extends AbstractProcessorUnitTest<InfinispanLogMinerEventProcessor> {
    @Override
    protected Configuration.Builder getConfig() {
        return TestHelper.defaultConfig()
                .with(OracleConnectorConfig.LOG_MINING_BUFFER_TYPE, "infinispan")
                .with(OracleConnectorConfig.LOG_MINING_BUFFER_LOCATION, "./target/data")
                .with(OracleConnectorConfig.LOG_MINING_BUFFER_DROP_ON_STOP, true);
    }

    @Override
    protected boolean isTransactionAbandonmentSupported() {
        return false;
    }

    @Override
    protected InfinispanLogMinerEventProcessor getProcessor(OracleConnectorConfig connectorConfig) {
        return new InfinispanLogMinerEventProcessor(context,
                connectorConfig,
                connection,
                dispatcher,
                partition,
                offsetContext,
                schema,
                metrics);
    }
}
