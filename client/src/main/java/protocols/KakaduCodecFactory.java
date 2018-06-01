package protocols;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class KakaduCodecFactory implements ProtocolCodecFactory {

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return new KakaduProtocolEncoder();
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new KakaduProtocolDecoder();
    }
}
