package packetproxy.common;

import packetproxy.Simplex;
import packetproxy.encode.EncodeHTTP;
import packetproxy.encode.EncodeHTTPBase;
import packetproxy.http.Http;
import packetproxy.http2.FramesBase;
import packetproxy.http2.Http2;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MockEndPoint implements Endpoint {
    private InputStream mockInputStream;
    private PipedOutputStream mockOutputStream;
    private PipedInputStream mockPipedInputStream;
    private ByteArrayOutputStream inputClientData = new ByteArrayOutputStream();
    private ArrayList<String> streamIds = new ArrayList<String>();
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

        //mockInputStream = new DelayPipedInputStream(65536);
        //PipedOutputStream pos = new PipedOutputStream((PipedInputStream) mockInputStream);
        //mockInputStream.transferTo(bao);

        mockPipedInputStream = new PipedInputStream(65536);
        mockOutputStream = new PipedOutputStream(mockPipedInputStream);

        //mockOutputStream = new ByteArrayOutputStream();

        Simplex simplexClientToServer = new Simplex(mockPipedInputStream, OutputStream.nullOutputStream());
        simplexClientToServer.addSimplexEventListener(new Simplex.SimplexEventListener() {
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
                EncodeHTTP encodeHTTP = new EncodeHTTP("h2");
                byte [] ret2 = encodeHTTP.decodeClientRequest(ret);
                Http http = new Http(ret2);
                String streamId = http.getHeader("x-packetproxy-http2-stream-id").get(0);
                //byte[] ret3 = encodeHTTP.decodeServerResponse(mockResponseData);
                /*Http http_ = new Http(ret3);
                http_.updateHeader("x-packetproxy-http2-stream-id", streamId);
                FramesBase http2 = new Http2();
                pos.write(http2.encodeServerResponse(http_.toByteArray()));*/
                //mockInputStream.reset();
                streamIds.add(streamId);
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
        simplexClientToServer.start();
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
            int l=0;
            try {
                /*
                TODO: HTTPSでMockResponseを使ったときにHisotry上にレスポンスがリクエストより先に表示されてしまう対策
                暫定でThread.sleep(millis)を使っているがおそらく重いリクエストが来ると先にレスポンスが出てしまう。
                 */
                while(streamIds.size()<=0) {
                    Thread.sleep(300);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return super.read(b, off, len);
        }
    }
    class DelayPipedInputStream extends PipedInputStream{

        DelayPipedInputStream(int pipeSize){
            super(pipeSize);
        }

        @Override
        public synchronized int read(byte b[], int off, int len){
            int l=0;
            try {
                /*
                TODO: HTTPSでMockResponseを使ったときにHisotry上にレスポンスがリクエストより先に表示されてしまう対策
                暫定でThread.sleep(millis)を使っているがおそらく重いリクエストが来ると先にレスポンスが出てしまう。
                 */
                while(streamIds.size()<=0 || l<=0) {
                    Thread.sleep(300);
                    l = super.read(b, off, len);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return l;
        }
    }
}
