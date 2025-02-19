/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.oracle.xstream;

import java.util.Map;

import io.debezium.connector.oracle.BaseChangeRecordEmitter;
import io.debezium.data.Envelope.Operation;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.Table;
import io.debezium.util.Clock;

import oracle.streams.ColumnValue;
import oracle.streams.RowLCR;

/**
 * Emits change data based on a single {@link RowLCR} event.
 *
 * @author Gunnar Morling
 */
public class XStreamChangeRecordEmitter extends BaseChangeRecordEmitter<ColumnValue> {

    private final RowLCR lcr;
    private final Map<String, Object> chunkValues;

    public XStreamChangeRecordEmitter(Partition partition, OffsetContext offset, RowLCR lcr,
                                      Map<String, Object> chunkValues, Table table, Clock clock) {
        super(partition, offset, table, clock);
        this.lcr = lcr;
        this.chunkValues = chunkValues;
    }

    @Override
    protected Operation getOperation() {
        switch (lcr.getCommandType()) {
            case RowLCR.INSERT:
                return Operation.CREATE;
            case RowLCR.DELETE:
                return Operation.DELETE;
            case RowLCR.UPDATE:
                return Operation.UPDATE;
            default:
                throw new IllegalArgumentException("Received event of unexpected command type: " + lcr);
        }
    }

    @Override
    protected Object[] getOldColumnValues() {
        return getColumnValues(lcr.getOldValues());
    }

    @Override
    protected Object[] getNewColumnValues() {
        return getColumnValues(lcr.getNewValues());
    }

    private Object[] getColumnValues(ColumnValue[] columnValues) {
        Object[] values = new Object[table.columns().size()];
        for (ColumnValue columnValue : columnValues) {
            int index = table.columnWithName(columnValue.getColumnName()).position() - 1;
            values[index] = columnValue.getColumnData();
        }

        // Overlay chunk values into non-chunk value array
        for (Map.Entry<String, Object> entry : chunkValues.entrySet()) {
            final int index = table.columnWithName(entry.getKey()).position() - 1;
            values[index] = entry.getValue();
        }

        return values;
    }

}
