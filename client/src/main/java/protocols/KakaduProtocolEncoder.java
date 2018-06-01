package protocols;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;


public class KakaduProtocolEncoder implements ProtocolEncoder {
    public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
        byte[] messageBytes;
        int messageCode;
        System.out.println("Encoding: "+o.getClass());
        if(o instanceof Protocols.LoginToServerRequest) {
            messageCode = 1;
            messageBytes = ((Protocols.LoginToServerRequest) o).toByteArray();
        } else if(o instanceof Protocols.LoginToServerResponse) {
            messageCode = 2;
            messageBytes = ((Protocols.LoginToServerResponse) o).toByteArray();
        }else if(o instanceof Protocols.LeaveServerRequest) {
            messageCode = 3;
            messageBytes = ((Protocols.LeaveServerRequest) o).toByteArray();
        }else if(o instanceof Protocols.LeaveServerResponse) {
            messageCode = 4;
            messageBytes = ((Protocols.LeaveServerResponse) o).toByteArray();
        } else if(o instanceof Protocols.ManageRoomRequest) {
            messageCode = 5;
            messageBytes = ((Protocols.ManageRoomRequest) o).toByteArray();
        } else if(o instanceof Protocols.ManageRoomResponse) {
            messageCode = 6;
            messageBytes = ((Protocols.ManageRoomResponse) o).toByteArray();
        } else throw new Exception("Unrecognized protocol");

        IoBuffer ioBuffer = IoBuffer.allocate(8+messageBytes.length);
        ioBuffer.putInt(messageCode);
        ioBuffer.putInt(messageBytes.length);
        ioBuffer.put(messageBytes);
        ioBuffer.flip();
        System.out.println("Encodind end, write message");
        protocolEncoderOutput.write(ioBuffer);
    }

    public void dispose(IoSession ioSession) throws Exception {

    }
}
