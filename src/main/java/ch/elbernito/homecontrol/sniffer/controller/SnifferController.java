package ch.elbernito.homecontrol.sniffer.controller;

import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.exceptions.ModbusResponseException;
import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
import com.digitalpetri.modbus.pdu.*;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import io.netty.buffer.ByteBufUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.stereotype.Controller;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Controller
public class SnifferController {


    public SnifferController() {


    }

    @PostConstruct
    public void postConstruct() {
        try {
            var transport = NettyTcpClientTransport.create(cfg -> {
                cfg.hostname = "192.168.1.7";
                cfg.port = 502;

            });



            var client = ModbusTcpClient.create(transport);
            client.connect();

            final ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(5000, 10);

            ReadHoldingRegistersResponse response = client.readHoldingRegisters(
                    1, request
            );


            ReadCoilsRequest readCoilsRequest = new ReadCoilsRequest(5000, 1);
            ReadCoilsResponse readCoilsResponse = client.readCoils(1, readCoilsRequest);
            System.out.println(Arrays.toString(readCoilsResponse.coils()));

//            ByteBuffer buffer = ByteBuffer.allocate(256);
//            ReadHoldingRegistersResponse.Serializer.encode(response, buffer);
//
//            buffer.flip();
//
//            ReadInputRegistersResponse decoded = ReadInputRegistersResponse.Serializer.decode(buffer);

           // System.out.println(decoded);

//
//
////            String hexString = "fd00000aa8660b5b010006acdc0100000101000100010000";
////            byte[] bytes = Hex.decodeHex(response.toCharArray());
////            System.out.println(new String(bytes, "UTF-8"));
//
//            System.out.println("Response: " + ReflectionToStringBuilder.toString(response));
//            System.out.println(ByteBufUtil.hexDump(response.registers()));
        } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void read(byte[] data){
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN); // Required to use getShort()
        bb.position(0);
        while(bb.hasRemaining())
        {
            int n = bb.getShort() & 0xFFFF;
            System.out.printf("Hex: %04X int value: %d%n", n, n);
        }
    }

    public static  int registerToUnsignedShort(byte[] bytes) {
        return ((bytes[0] & 0xff) << 8 | (bytes[1] & 0xff));
    }

    public static int registersToInt(byte[] bytes) {
        return (((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff));
    }

    public static long registersToLong(byte[] bytes) {
        return ((((long) (bytes[0] & 0xff) << 56) | ((long) (bytes[1] & 0xff) << 48) | ((long) (bytes[2] & 0xff) << 40)
                | ((long) (bytes[3] & 0xff) << 32) | ((long) (bytes[4] & 0xff) << 24) | ((long) (bytes[5] & 0xff) << 16)
                | ((long) (bytes[6] & 0xff) << 8) | (bytes[7] & 0xff)));
    }
}
