package top.burdukowsky.keeneticpolicychangerjava;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

/**
 * @see <a href="https://stackoverflow.com/a/14098926">Source</a>
 */
public class SimpleTelnetClient {
    static class Responder extends Thread {
        private StringBuilder builder = new StringBuilder();
        private final SimpleTelnetClient checker;
        private CountDownLatch latch;
        private String waitFor = null;
        private boolean isKeepRunning = true;

        Responder(SimpleTelnetClient checker) {
            this.checker = checker;
        }

        boolean foundWaitFor(String waitFor) {
            return builder.toString().contains(waitFor);
        }

        public synchronized String getAndClearBuffer() {
            String result = builder.toString();
            builder = new StringBuilder();
            return result;
        }

        @Override
        public void run() {
            while (isKeepRunning) {
                String s;

                try {
                    s = checker.messageQueue.take();
                } catch (InterruptedException e) {
                    break;
                }

                synchronized (Responder.class) {
                    builder.append(s);
                }

                if (waitFor != null && latch != null && foundWaitFor(waitFor)) {
                    latch.countDown();
                }
            }
        }

        public String waitFor(String waitFor) {
            synchronized (Responder.class) {
                if (foundWaitFor(waitFor)) {
                    return getAndClearBuffer();
                }
            }

            this.waitFor = waitFor;
            latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }

            String result = null;
            synchronized (Responder.class) {
                result = builder.toString();
                builder = new StringBuilder();
            }

            return result;
        }
    }

    static class TelnetReader extends Thread {
        private final SimpleTelnetClient checker;
        private final TelnetClient tc;

        TelnetReader(SimpleTelnetClient checker, TelnetClient tc) {
            this.checker = checker;
            this.tc = tc;
        }

        @Override
        public void run() {
            InputStream instr = tc.getInputStream();

            try {
                byte[] buff = new byte[1024];
                int ret_read = 0;

                do {
                    ret_read = instr.read(buff);
                    if (ret_read > 0) {
                        checker.sendForResponse(new String(buff, 0, ret_read));
                    }
                } while (ret_read >= 0);
            } catch (Exception e) {
                System.err.println("Exception while reading socket:" + e.getMessage());
            }

            try {
                tc.disconnect();
                checker.stop();
                System.out.println("Disconnected.");
            } catch (Exception e) {
                System.err.println("Exception while closing telnet:" + e.getMessage());
            }
        }
    }

    private String host;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
    private int port;
    private TelnetReader reader;
    private Responder responder;
    private TelnetClient tc;

    public SimpleTelnetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    protected void stop() {
        responder.isKeepRunning = false;
        responder.interrupt();
    }

    public void send(String command) {
        PrintStream ps = new PrintStream(tc.getOutputStream());
        ps.println(command);
        ps.flush();
    }

    public void sendForResponse(String s) {
        messageQueue.add(s);
    }

    public void connect() throws Exception {
        tc = new TelnetClient();

        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        try {
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        } catch (InvalidTelnetOptionException e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
        }

        tc.connect(host, port);
        reader = new TelnetReader(this, tc);
        reader.start();

        responder = new Responder(this);
        responder.start();
    }

    public String waitFor(String s) {
        return responder.waitFor(s);
    }
}