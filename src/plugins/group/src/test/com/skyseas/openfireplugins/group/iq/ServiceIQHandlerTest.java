package com.skyseas.openfireplugins.group.iq;

import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;

public class ServiceIQHandlerTest extends IQHandlerTest<ServiceIQHandlerTest.MockServiceIQHandler> {

   public ServiceIQHandlerTest(){
       super(new MockServiceIQHandler());
   }

   public void testDispatch() throws Exception {
        // Arrange
        final IQ packet = IQ(
                "<iq from='owner@skysea.com' to='100@group.skysea.com' id='v12' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "  \t<destroy>\n" +
                "  \t\t<reason>再见了各位！</reason>\n" +
                "  \t</destroy>\n" +
                "  </x>\n" +
                "</iq>");

        final IQContext context = new IQContext(packet);
        new NonStrictExpectations(handler){ { } };

        // Act
        handler.dispatch(context);

        // Assert
       new Verifications(){
           {
               handler.process(packet);
               times = 1;
           }
       };

    }

    public static class MockServiceIQHandler extends ServiceIQHandler{
        @Override
        protected void process(IQ packet) {

        }
    }
}