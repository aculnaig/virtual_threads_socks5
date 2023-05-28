import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Main {
    public static void main(String[] args) throws IOException {
        final ThreadFactory factory = Thread.ofVirtual().factory();
        final ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);

        try (final ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("127.0.0.1", 8080));

            System.out.format("Listening from %s\n", server.getInetAddress().getHostAddress());

            final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

            while (true) {
                final Socket client = server.accept();

                executor.submit(() -> {
                    // No authentication needed
                    buffer.clear();
                    buffer.put((byte) 0x05);
                    buffer.put((byte) 0x00);
                    buffer.flip();
                    buffer.reset();

                    try {
                        client.getOutputStream().write(buffer.array());
                        client.getOutputStream().flush();

                        buffer.clear();
                        buffer.flip();
                        buffer.reset();

                        client.getInputStream().read(buffer.array());

                        final byte[] request = buffer.array();

                        final byte addressType = request[3];
                        switch (addressType) {
                            case (byte)0x01:
                                buffer.clear();
                                buffer.flip();
                                buffer.reset();

                                buffer.put((byte)0x05);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x01);
                                buffer.put(request[4]);
                                buffer.put(request[5]);
                                buffer.put(request[6]);
                                buffer.put(request[7]);
                                buffer.put(request[8]);
                                buffer.put(request[9]);

                                buffer.flip();

                                break;
                            case (byte)0x03:
                                buffer.clear();
                                buffer.flip();
                                buffer.reset();

                                buffer.put((byte)0x05);
                                buffer.put((byte)0x08);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x01);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x00);
                                buffer.flip();

                                break;
                            case (byte)0x04:
                                buffer.clear();
                                buffer.flip();
                                buffer.reset();

                                buffer.put((byte)0x05);
                                buffer.put((byte)0x08);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x01);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x00);
                                buffer.flip();

                                break;
                            default:
                                // Unsupported address type, sends 0x08 response code
                                buffer.put((byte)0x05);
                                buffer.put((byte)0x08);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x01);
                                buffer.put((byte)0x00);
                                buffer.put((byte)0x00);
                                buffer.flip();

                                break;
                        }

                        client.getOutputStream().write(buffer.array());
                        client.getOutputStream().flush();

                    } catch (IOException e) {
                        Thread.currentThread().interrupt();
                    }

                });
            }
        }
    }
}

