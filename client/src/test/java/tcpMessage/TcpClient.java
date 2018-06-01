package tcpMessage;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import protocols.KakaduCodecFactory;

import java.net.InetSocketAddress;

public class TcpClient {
    private static String ip;
    private static int port;

    private static IoSession session;
    IoConnector connector;

    public TcpClient(String ip, int port){
        this.ip=ip;
        this.port=port;
        connector = new NioSocketConnector();
        connector.getSessionConfig().setReadBufferSize(2048);

        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new KakaduCodecFactory()));

        connector.setHandler(new MinaClientHandler());
        ConnectFuture future = connector.connect(new InetSocketAddress(ip, port));
        future.awaitUninterruptibly();

        if (!future.isConnected())
        {
            return;
        }
        session = future.getSession();
        session.getConfig().setUseReadOperation(true);


//        System.out.println("After Writing");
//        connector.dispose();
    }

    public Object send(Object message) throws InterruptedException {
        session.write(message);
        final ReadFuture readFuture = session.read();
        readFuture.awaitUninterruptibly();
        Object response = readFuture.getMessage();
        connector.dispose();
        return response;
    }

}
