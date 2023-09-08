import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NioServer {
    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 45001;

        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.socket().bind(new InetSocketAddress(port));

        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        log("Server started at port " + port  + "\nWaiting for connections...");

        while (true) {
            // Блокирование
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);

                        log("Connected " + channel.getRemoteAddress());

                        sockets.put(channel, ByteBuffer.allocate(1000));
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = sockets.get(channel);

                        int bytesRead = channel.read(buffer);

                        log("Reading from " + channel.getRemoteAddress() + ", bytesRead=" + bytesRead);

                        if (bytesRead == -1) {
                            log("Connection closed " + channel.getRemoteAddress());
                            sockets.remove(channel);
                            channel.close();
                        }

                        // Мы прочитали весь буфер и готовы писать ответ
                        if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
                            channel.register(selector, SelectionKey.OP_WRITE);
                        }
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = sockets.get(channel);

                        buffer.flip();

                        String clientMessage = new String(buffer.array(), buffer.position());

                        String response = clientMessage + ", occurredOn=" + System.currentTimeMillis() + "\r\n";


                        buffer.clear();
                        buffer.put(ByteBuffer.wrap(response.getBytes()));
                        buffer.flip();

                        int bytesWritten = channel.write(buffer);

                        log("Writing to " + channel.getRemoteAddress() + " bytesWritten=" + bytesWritten);

                        if (!buffer.hasRemaining()) {
                            buffer.compact();
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                    }
                } catch (IOException e) {
                    log(e.getMessage());
                }
            }

            selector.selectedKeys().clear();
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }
}