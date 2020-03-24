package packetproxy.common;

import packetproxy.Simplex;
import packetproxy.encode.EncodeHTTP;
import packetproxy.encode.EncodeHTTPBase;
import packetproxy.http.Http;

import java.io.*;
import java.net.InetSocketAddress;

public class MockEndPoint implements Endpoint {
    private InputStream mockInputStream;
    private PipedOutputStream mockOutputStream;
    private PipedInputStream mockPipedInputStream;
    private ByteArrayOutputStream inputClientData = new ByteArrayOutputStream();
    //private OutputStream mockOutputStream;
    InetSocketAddress addr;

    public MockEndPoint(InetSocketAddress addr, byte[] mockResponseData){
        this.addr = addr;
        try {
            init(mockResponseData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init(byte[] mockResponseData) throws Exception{
        mockInputStream = new DelayByteArrayInputStream(mockResponseData);

        mockPipedInputStream = new PipedInputStream(65536);
        mockOutputStream = new PipedOutputStream(mockPipedInputStream);

        //mockOutputStream = new ByteArrayOutputStream();

        Simplex simplex = new Simplex(mockInputStream, OutputStream.nullOutputStream());
        simplex.addSimplexEventListener(new Simplex.SimplexEventListener() {
            @Override
            public void onChunkArrived(byte[] data) throws Exception {
                inputClientData.write(data);
            }
            @Override
            public byte[] onChunkPassThrough() throws Exception {
                return new byte[]{};
            }
            @Override
            public byte[] onChunkAvailable() throws Exception {
                if(inputClientData.size()<=0){
                    return null;
                }
                byte[] ret = inputClientData.toByteArray();
                inputClientData.reset();
                String b = ret.toString();

                EncodeHTTP encodeHTTP = new EncodeHTTP("h2");
                byte [] ret2 = encodeHTTP.decodeClientRequest(ret);
                Http bb = new Http(ret2);
                return ret;
            }
            @Override
            public byte[] onChunkReceived(byte[] data) throws Exception {
                return data;
            }
            @Override
            public int onPacketReceived(byte[] data) throws Exception {
                return data.length;
            }
            @Override
            public byte[] onChunkSend(byte[] data) throws Exception {
                return data;
            }
        });
        simplex.start();

        /*
        Thread mockServerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] inputBuf = new byte[65536];
                    int inputLen = 0;
                    while ((inputLen = mockPipedInputStream.read(inputBuf)) > 0) {
                        EncodeHTTP encodeHTTP = new EncodeHTTP("h2");
                        String b = inputBuf.toString();

                        //byte httpRaw[] = encodeHTTP.decodeClientRequest(inputBuf);
                        //Http http = new Http(httpRaw);

                        int a = 1;
                        a+=1;
                    }
                    mockPipedInputStream.close();
                } catch (Exception e) {
                    try {
                        mockPipedInputStream.close();

                    } catch (Exception e1) {
                        //e1.printStackTrace();
                    }
                    //e.printStackTrace();
                }
            }
        });
        mockServerThread.start();
        */
    }

    @Override
    public InputStream getInputStream() {
        return mockInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        //return OutputStream.nullOutputStream();
        return mockOutputStream;
    }

    @Override
    public InetSocketAddress getAddress() {
        return addr;
    }

    @Override
    public int getLocalPort() {
        throw new java.lang.UnsupportedOperationException("MockEndPoint is not supported getLocalPort!");
    }

    @Override
    public String getName() {
        return addr.getHostName();
    }

    class DelayByteArrayInputStream extends ByteArrayInputStream{

        public DelayByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public synchronized int read(byte b[], int off, int len){
            try {
                /*
                TODO: HTTPSでMockResponseを使ったときにHisotry上にレスポンスがリクエストより先に表示されてしまう対策
                暫定でThread.sleep(millis)を使っているがおそらく重いリクエストが来ると先にレスポンスが出てしまう。
                 */
                Thread.sleep(300);
            }catch (Exception e){
                e.printStackTrace();
            }
            return super.read(b, off, len);
        }
    }
}
