package com.skyseas.openfireplugins.push;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.jivesoftware.util.JiveGlobals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhangzhi on 2014/11/12.
 */
@RunWith(Parameterized.class)
public class DoCheckTest {
    @Parameterized.Parameters
    public static Iterable<Object[]> getData() {
        return Arrays.asList(new Object[][]{
                {"192.168.1.1", "192.168.1.2;192.168.1.1;", true},
                {"192.168.1.1", "192.168.1.221;192.168.1.8;", false},
                {"192.168.1.1", null, false},
        });
    }

    @Parameterized.Parameter(0)
    public String clientIP;

    @Parameterized.Parameter(1)
    public String allowIPs;

    @Parameterized.Parameter(2)
    public boolean checkResult;

    @Mocked
    HttpServletRequest request;

    @Mocked
    HttpServletResponse response;

    @Mocked
    PacketSender sender;

    @Test
    public void testDoCheck() {
        // Arrange
        PushServlet pushServlet = new PushServlet(sender);
        new NonStrictExpectations(JiveGlobals.class) {
            {
                request.getRemoteAddr();
                result = clientIP;

                JiveGlobals.getProperty(PushServlet.ALLOW_IP_LIST_KEY);
                result = allowIPs;
            }
        };


        // Act
        boolean res = pushServlet.doCheck(request);

        // Assert
        assertEquals(checkResult, res);
    }


}
