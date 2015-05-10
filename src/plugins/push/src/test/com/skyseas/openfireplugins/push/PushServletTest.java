package com.skyseas.openfireplugins.push;

import com.skyseas.openfireplugins.push.PacketSender;
import com.skyseas.openfireplugins.push.PushServlet;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.jivesoftware.util.JiveGlobals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xmpp.packet.Packet;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

public class PushServletTest extends TestCase {
    @Mocked
    HttpServletRequest request;
    @Mocked
    HttpServletResponse response;
    @Mocked
    PacketSender sender;
    @Mocked
    PrintWriter writer;
    private PushServlet servlet;

    @Override
    protected void setUp() throws Exception {
        servlet = new PushServlet(sender);
        new NonStrictExpectations() {
            {
                response.getWriter();
                result = writer;
            }
        };
    }

    public void testDoPost_When_Content_Is_Empty() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet){};

        // Act
        servlet.doPost(request, response);

        // Assert
        new Verifications() {
            {
                servlet.finish(response, HttpServletResponse.SC_BAD_REQUEST, "invalid xmpp data.");
                times = 1;
            }
        };
    }


    public void testDoPost() throws Exception {
        // Arrange
        new NonStrictExpectations() {
            {
                request.getParameter(PushServlet.PACKET_CONTENT_PARAMETER_NAME);
                result = "<message from=\"skysea.com/event\" to=\"user@skysea.com\" id=\"v2\">\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/event\">\n" +
                        "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                        "        <field var=\"EVENT_NAME\"> <value>activity_deleted</value> </field>\n" +
                        "        <field var=\"activity_id\"> <value>100</value> </field>\n" +
                        "    </x>\n" +
                        "  </x>\n" +
                        "</message>";
            }
        };

        // Act
        servlet.doPost(request, response);

        // Assert
        new Verifications() {
            {
                sender.send(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message from=\"skysea.com/event\" to=\"user@skysea.com\" id=\"v2\">\n" +
                                        "  <x xmlns=\"http://skysea.com/protocol/event\">\n" +
                                        "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                        "        <field var=\"EVENT_NAME\"> <value>activity_deleted</value> </field>\n" +
                                        "        <field var=\"activity_id\"> <value>100</value> </field>\n" +
                                        "    </x>\n" +
                                        "  </x>\n" +
                                        "</message>",
                                packet.toXML().toString());
                    }
                }));
                times = 1;

                response.setStatus(HttpServletResponse.SC_OK);
                times = 1;
            }
        };
    }

    public void testDoPost_When_Content_Is_Invalid_Packet() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet) {
            {
                request.getParameter(PushServlet.PACKET_CONTENT_PARAMETER_NAME);
                result = "<x from='skysea.com/event' to='user@skysea.com' id='v2'>\n" +
                        "  <x xmlns='http://skysea.com/protocol/event'>\n" +
                        "    <x xmlns='jabber:x:data' type='result'>\n" +
                        "        <field var='EVENT_NAME'> <value>activity_deleted</value> </field>\n" +
                        "        <field var='activity_id'> <value>100</value> </field>\n" +
                        "    </x>\n" +
                        "  </x>\n" +
                        "</x>";
            }
        };

        // Act
        servlet.doPost(request, response);

        // Assert
        new Verifications() {
            {
                servlet.finish(response, HttpServletResponse.SC_BAD_REQUEST, "invalid xmpp data.");
                times = 1;
            }
        };
    }

    public void testService_When_Check_Fail() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet) {
            {
                servlet.doCheck(request);
                result = false;
                times = 1;
            }
        };

        // Act
        servlet.service(request, response);

        // Assert
        new Verifications() {
            {
                servlet.finish(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized");
                times = 1;
            }
        };
    }

    public void testDoPost_When_Send_Fail() throws Exception {
        // Arrange
        new NonStrictExpectations(servlet) {
            {
                request.getParameter(PushServlet.PACKET_CONTENT_PARAMETER_NAME);
                result = "<iq from='skysea.com/event' type='set' id='v2'>\n" +
                        "  <x xmlns='http://skysea.com/protocol/event'>\n" +
                        "    <x xmlns='jabber:x:data' type='result'>\n" +
                        "        <field var='EVENT_NAME'> <value>activity_deleted</value> </field>\n" +
                        "        <field var='activity_id'> <value>100</value> </field>\n" +
                        "    </x>\n" +
                        "  </x>\n" +
                        "</iq>";

                sender.send(withAny((Packet) null));
                result = new IllegalArgumentException();
            }
        };

        // Act
        servlet.doPost(request, response);

        // Assert
        new Verifications() {
            {
                servlet.finish(response, HttpServletResponse.SC_BAD_REQUEST, "invalid xmpp data.");
                times = 1;
            }
        };
    }

    public void testFinish() throws  Exception{
        // Arrange
        int status = HttpServletResponse.SC_BAD_REQUEST;
        final String message = "ok";

        // Act
        servlet.finish(response, status, message);

        // Assert
        new Verifications(){
            {
                writer.write(message);
                times = 1;

                writer.flush();
                times = 1;

                writer.close();
                times = 1;
            }
        };
    }

}