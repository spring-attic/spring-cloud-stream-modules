/*
 *  Copyright 2015 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.cloud.stream.module.firehose.source;

import org.apache.commons.codec.binary.Hex;
import org.cloudfoundry.dropsonde.events.EventFactory;
import org.cloudfoundry.dropsonde.events.HttpFactory;
import org.cloudfoundry.dropsonde.events.MetricFactory;
import org.cloudfoundry.dropsonde.events.UuidFactory;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

import java.math.BigInteger;

/**
 * @author Vinicius Carvalho
 */
public class TupleFactory {
    public static Tuple createTuple(EventFactory.Envelope envelope) {
        Tuple tuple = null;
        switch (envelope.getEventType()) {
            case CounterEvent:
                tuple = fromCounter(envelope);
                break;
            case ContainerMetric:
                tuple = fromContainer(envelope);
                break;
            case HttpStart:
                tuple = fromHttpStart(envelope);
                break;
            case HttpStop:
                tuple = fromHttpStop(envelope);
                break;
            case HttpStartStop:
                tuple = fromHttpStartStop(envelope);
                break;
            case ValueMetric:
                tuple = fromValue(envelope);
                break;
            default:
                tuple = createBuilder(envelope).build();
                break;
        }
        return tuple;
    }


    private static Tuple fromCounter(EventFactory.Envelope envelope) {
        Tuple tuple = createBuilder(envelope)
                .put("type", envelope.getEventType().toString())
                .put("metrics", TupleBuilder.tuple().of(envelope.getCounterEvent().getName(), envelope.getCounterEvent().getTotal()))
                .build();
        return tuple;
    }

    private static Tuple fromContainer(EventFactory.Envelope envelope) {
        MetricFactory.ContainerMetric containerMetric = envelope.getContainerMetric();
        Tuple containerTuple = TupleBuilder.tuple().put("cpu", containerMetric.getCpuPercentage())
                .put("disk", containerMetric.getDiskBytes())
                .put("memory", containerMetric.getMemoryBytes()).build();
        Tuple tuple = createBuilder(envelope)
                .put("metrics", containerTuple)
                .build();

        return tuple;
    }

    private static Tuple fromHttpStart(EventFactory.Envelope envelope) {
        Tuple tuple = createBuilder(envelope).build();
        return tuple;
    }

    private static Tuple fromHttpStop(EventFactory.Envelope envelope) {
        Tuple tuple = createBuilder(envelope).build();
        return tuple;
    }

    private static Tuple fromHttpStartStop(EventFactory.Envelope envelope) {
        HttpFactory.HttpStartStop httpStartStop = envelope.getHttpStartStop();
        TupleBuilder httpTuple = TupleBuilder.tuple()
                .put("startTime", httpStartStop.getStartTimestamp())
                .put("stopTime", httpStartStop.getStopTimestamp())
                .put("peerType", httpStartStop.getPeerType().toString())
                .put("method", httpStartStop.getMethod().toString())
                .put("uri", httpStartStop.getUri())
                .put("remoteAddress", httpStartStop.getRemoteAddress())
                .put("userAgent", httpStartStop.getUserAgent())
                .put("statusCode", httpStartStop.getStatusCode())
                .put("contentLength", httpStartStop.getContentLength())
                .put("requestId", parseUUID(httpStartStop.getRequestId()));


        String applicationId = parseUUID(httpStartStop.getApplicationId());
        if (applicationId != null) {
            httpTuple.put("applicationId", applicationId);
        }
        String parentRequestId = parseUUID(httpStartStop.getParentRequestId());
        if (parentRequestId != null) {
            httpTuple.put("parentRequestId", parentRequestId);
        }
        Tuple tuple = createBuilder(envelope)
                .put("metrics", httpTuple.build())
                .build();
        return tuple;
    }

    private static Tuple fromValue(EventFactory.Envelope envelope) {
        MetricFactory.ValueMetric valueMetric = envelope.getValueMetric();
        Tuple tuple = createBuilder(envelope).put(valueMetric.getName(), valueMetric.getValue()).put("unit", valueMetric.getUnit()).build();
        return tuple;
    }

    private static TupleBuilder createBuilder(EventFactory.Envelope envelope) {
        return TupleBuilder.tuple()
                .put("timestamp", envelope.getTimestamp())
                .put("origin", envelope.getOrigin())
                .put("host", envelope.getIp())
                .put("index", envelope.getIndex())
                .put("type", envelope.getEventType().toString());
    }

    /**
     * Needed due dropsonde decision to use uint64 and little endianess
     *
     * @param uuid
     * @return
     */
    public static String parseUUID(UuidFactory.UUID uuid) {

        String uuidStr = null;

        try {
            BigInteger low = new BigInteger(String.valueOf(uuid.getLow()));
            BigInteger high = new BigInteger(String.valueOf(uuid.getHigh()));
            byte[] lowBytes = reverse(low.toByteArray());
            byte[] highBytes = reverse(high.toByteArray());
            byte[] data1 = new byte[lowBytes.length + highBytes.length];
            System.arraycopy(lowBytes, 0, data1, 0, lowBytes.length);
            System.arraycopy(highBytes, 0, data1, lowBytes.length, highBytes.length);

            StringBuilder builder = new StringBuilder(Hex.encodeHexString(data1));
            uuidStr = builder.insert(8, "-").insert(13, "-").insert(18, "-").insert(23, "-").toString();
        } catch (Exception ex) {

        }


        return uuidStr;

    }

    private static byte[] reverse(byte[] data) {
        byte[] reversed = new byte[8];
        for (int i = 0; i < reversed.length; i++) {
            reversed[i] = data[data.length - 1 - i];
        }
        return reversed;
    }
}
