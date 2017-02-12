package tests.integration;

import com.semakin.ApplicationFacade;
import com.semakin.ResultPrinter;
import com.semakin.exceptions.InnerResourceException;
import com.semakin.resourceGetters.ReaderGetterable;
import tests.unit.ResultPrinterMock;
import tests.unit.mocks.ReaderGetterMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Chi on 08.02.2017.
 */
class ApplicationFacadeTest {
    @Test
    void run_Valid() throws InnerResourceException {
        HashMap<String, String> resourcesStub = new HashMap<String, String>(){{
            put("abc","1 1 1 1 1 1 1 1 ");
            put("def","2 0 2 5 6 4 57");
            put("ghi","1 2 3 4 5 6 7 8 9 7");
            put("abcdefef","0 1 -1 2 -5 7 -13 24");
            put("87gu","1 2 3 4");
            put("qwert","2");
            put("кирилица","1");
        }};
        int expectedSum = 68;
        ResultPrinterMock resultPrinter = getResultPrinterMock();

        runApp(resourcesStub, resultPrinter);
        waitThreads(1000);

        String actualLastMessage = resultPrinter.getLastMessage();
        System.out.println(actualLastMessage);
        int actualSum = Integer.parseInt(actualLastMessage);

        Assertions.assertEquals(expectedSum, actualSum);
    }

    @Test
    void run_Invalid_stopPrint() throws InnerResourceException {
        HashMap<String, String> resourcesStub = new HashMap<String, String>(){{
            put("abc","1 2 1 1 1 1 1 1 ");
            put("def","2 0 2 5 6 4 57");
            put("abcdefef","0 1 2 1 1 1 1 1 1 1 2 1 1 1 1 1 1 1 2 1 1 1 1 1 1  1- it is invalid");
            put("ghi","1 2 3 4 5 6 7 8 9 7");
            put("abcdefef","0 1 -1 2 -5 7 -13 24");
        }};
        int unExpectedSum = 68;
        ResultPrinterMock resultPrinter = getResultPrinterMock();

        runApp(resourcesStub, resultPrinter);
        waitThreads(1000);
        String lastMessage = resultPrinter.getLastMessage();
        System.out.println("последнее сообщение: " + lastMessage);
        int actualLastSum = Integer.parseInt(lastMessage);

        Assertions.assertNotEquals(unExpectedSum, actualLastSum);
    }

    private void runApp(HashMap<String, String> resourcesStub, ResultPrinter resultPrinter){
        ApplicationFacade app = getApplicationFacadeByMockResources(resourcesStub, resultPrinter);
        Object[] keyObjects = resourcesStub.keySet().toArray();
        String[] resourceAddresses = Arrays.copyOf(keyObjects, keyObjects.length, String[].class);

        app.Run(resourceAddresses);
    }

    private ApplicationFacade getApplicationFacadeByMockResources(HashMap<String, String> resourcesMock, ResultPrinter resultPrinter){
        return new ApplicationFacade(resultPrinter){
            @Override
            protected ReaderGetterable getReaderGetter() {
                return new ReaderGetterMock(resourcesMock);
            }
        };
    }

    private ResultPrinterMock getResultPrinterMock(){
        return new ResultPrinterMock();
    }

    private void waitThreads(int millis){
        try {
            Thread.currentThread().join(millis); // ожидаем завершения всех потоков
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}