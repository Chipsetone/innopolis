package tests.unit.threading;

import com.semakin.ResultPrinter;
import com.semakin.threading.MessageProcessor;
import com.semakin.threading.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.mocks.ResultPrinterMock;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Chi on 11.02.2017.
 */
class MessageProcessorTest {

    @Test
    void pushMessage_AddingValidMessages() {
        LinkedList<String> printerTarget = new LinkedList<>();
        MessageProcessor processor = getMessageProcessor(printerTarget);

        int expectedProcessingMessages = 5;

        pushValidMessages(processor, expectedProcessingMessages);
        processor.runProcessingMessages();
        int actualProcessingMessages = printerTarget.size();

        Assertions.assertEquals(expectedProcessingMessages, actualProcessingMessages);
    }

    @Test
    void pushMessage_AddingInvalidMessage_Stop() {
        LinkedList<String> printerTarget = new LinkedList<>();
        MessageProcessor processor = getMessageProcessor(printerTarget);

        int expectedProcessingMessagesCount = 7;
        int unprocessingMessagesCount = 5;
        pushValidMessages(processor, expectedProcessingMessagesCount);
        pushInvalidMessages(processor, unprocessingMessagesCount);

        processor.runProcessingMessages();
        int actualProcessingMessages = printerTarget.size();

        Assertions.assertEquals(expectedProcessingMessagesCount, actualProcessingMessages);
    }

    @Test
    void getLastMessage_AllValidMessages_ContainNumber() {
        LinkedList<String> printerTarget = new LinkedList<>();
        MessageProcessor processor = getMessageProcessor(printerTarget);

        int expectedProcessedMessagesCount = 8;
        pushValidMessages(processor, expectedProcessedMessagesCount);

        processor.runProcessingMessages();

        int actualMessagesCount = printerTarget.size();

        Assertions.assertEquals(expectedProcessedMessagesCount, actualMessagesCount);
    }

    @Test
    void getLastMessage_InvalidMessageInMiddle_StopQueueBeforeInvalid(){
        LinkedList<String> printerTarget = new LinkedList<>();
        MessageProcessor processor = getMessageProcessor(printerTarget);

        int expectedLastMessageValue = 999;
        Message expectedLastValidMessage = new Message(expectedLastMessageValue);
        processor.pushMessage(expectedLastValidMessage);

        Exception somethingBadException = new Exception("smthg bad");
        Message invalidMessage = new Message(somethingBadException);
        processor.pushMessage(invalidMessage);

        int unExpectedMessageValue = 674897234;
        String unExpectedMessageString = "" + unExpectedMessageValue;
        Message unExpectedMessage = new Message(unExpectedMessageValue);
        processor.pushMessage(unExpectedMessage);

        processor.runProcessingMessages();
        String actualLastPrintedString = printerTarget.getLast();

        int actualPrintedCount = printerTarget.size();

        Assertions.assertNotEquals(unExpectedMessageString, actualLastPrintedString);
        Assertions.assertEquals(1, actualPrintedCount);
    }

    private void pushValidMessages(MessageProcessor messageProcessor, int countOfMessages){
        for (int i = 0; i < countOfMessages; i++) {
            Message validMessage = new Message(i);
            messageProcessor.pushMessage(validMessage);
        }
    }

    private void pushInvalidMessages(MessageProcessor messageProcessor, int countOfMessages){
        for (int i = 0; i < countOfMessages; i++) {
            Exception innerMessageException = new Exception("something Bad: " + i);
            Message validMessage = new Message(innerMessageException);
            messageProcessor.pushMessage(validMessage);
        }
    }

    private MessageProcessor getMessageProcessor(LinkedList<String> printerTarget){
        ResultPrinter printer = getFakeLogPrinter(printerTarget);
        return new MessageProcessor(printer);
    }

    private ResultPrinter getFakeLogPrinter(LinkedList<String> fakePrinterTarget){
        return new ResultPrinterMock(fakePrinterTarget);
    }

}