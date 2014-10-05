package org.haox.event.tcp;

import org.haox.event.EventType;
import org.haox.transport.tcp.StreamingDecoder;

import java.nio.ByteBuffer;

public class TestTcpBase {
    protected String serverHost = "127.0.0.1";
    protected short serverPort = 8181;
    protected String TEST_MESSAGE = "Hello world!";
    protected String clientRecvedMessage;

    protected enum TestEventType implements EventType {
        FINISHED
    }

    protected String recvBuffer2String(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }

    protected StreamingDecoder createStreamingDecoder() {
        return new StreamingDecoder() {
            @Override
            public DecodingResult decode(ByteBuffer streaming) {
                return new MessageResult(TEST_MESSAGE.getBytes().length);
            }
        };
    }
}
