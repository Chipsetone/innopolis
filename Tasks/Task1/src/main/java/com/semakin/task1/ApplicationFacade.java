package com.semakin.task1;

import com.semakin.task1.calculation.SumCalculatorFactory;
import com.semakin.task1.parsers.StringConverter;
import com.semakin.task1.parsers.StringValidConverter;
import com.semakin.task1.resourceGetters.ReaderGetterFactory;
import com.semakin.task1.resourceGetters.ReaderGetterable;
import com.semakin.task1.threading.MessageQueueProcessor;
import com.semakin.task1.threading.ResourceCalculator;
import com.semakin.task1.threading.RunnableService;
import com.semakin.task1.validation.EvenPositiveNumberValidator;
import com.semakin.task1.validation.NumberValidatorable;
import com.semakin.task1.validation.StringAsNumberValidator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Приложение для обработки ресурсов,
 * скрывает в себе всю инициализацию/конфигурацию
 * @author Виктор Семакин
 */
public class ApplicationFacade {
    private SumCalculatorFactory sumCalculatorFactory;
    private RunnableService runService;
    private MessageQueueProcessor messageProcessor;
    private AtomicBoolean isComplete = new AtomicBoolean(false);
    private final ResultPrinter resultPrinter;

    public static final Logger logger = Logger.getLogger(ApplicationFacade.class);

    /**
     * Инициализатор приложения для обработки
     * @param resultPrinter
     */
    public ApplicationFacade(ResultPrinter resultPrinter) {
        this.resultPrinter = resultPrinter;
        init(resultPrinter);
    }

    /**
     * Запуск обработки ресурсов
     * Обработка каждого ресурса происходит в отдельном потоке.
     * @param resourceAddresses адреса ресурсов
     */
    public void Run(String resourceAddresses[]) {
        List<ResourceCalculator> resourceCalculators = getResourceCalculators(resourceAddresses);
        calculateResources(resourceCalculators);
        while (true){
            messageProcessor.runProcessingMessages();

            if (isCalculatingDone()){
                break;
            }
        }
        logger.trace("Очередь обработки завершена");
    }

    /**
     * Возвращает получатель считывателя потока из ресурса
     * Переопределяется в целях тестирования
     * @return возвращатель читателя потока (Stream) на основании адреса
     */
    protected ReaderGetterable getReaderGetter() {
        ReaderGetterFactory readerGetterFactory = new ReaderGetterFactory();
        return readerGetterFactory.getReaderGetter();
    }

    private void init(ResultPrinter resultPrinter) {
        runService = new RunnableService();
        messageProcessor = new MessageQueueProcessor(resultPrinter);
        sumCalculatorFactory = newSumCalculationFactory();
        logger.debug("ApplicationFacade инициализирован.");
    }

    private SumCalculatorFactory newSumCalculationFactory() {
        NumberValidatorable numberValidator = new EvenPositiveNumberValidator();
        StringAsNumberValidator stringValidator = new StringAsNumberValidator();
        StringConverter stringConverter = new StringValidConverter(stringValidator, numberValidator);
        ReaderGetterable readerGetter = getReaderGetter();

        return new SumCalculatorFactory(readerGetter, stringConverter, messageProcessor);
    }

    private void calculateResources(List<ResourceCalculator> resourceCalculators){
        try {
            logger.trace("Запуск обработки " + resourceCalculators.size() + " ресурсов");
            runService.invokeAll(resourceCalculators);
        } catch (InterruptedException e) {
            logger.error("Ошибка при работе с потоками", e);
            e.printStackTrace();
        }
        logger.debug("Обработка ресурсов завершена");
        isComplete.set(true);
    }

    private List<ResourceCalculator> getResourceCalculators(String resourceAddresses[]) {
        List<ResourceCalculator> resourceCalculators = new ArrayList<>();

        for (String resourceAddress : resourceAddresses) {

            resourceAddress = resourceAddress.trim();
            if(resourceAddress.length() == 0){
                logger.error("Обнаружен ресурс с пустым именем!");
                continue;
            }

            ResourceCalculator resourceCalculator = new ResourceCalculator(resourceAddress, sumCalculatorFactory);
            resourceCalculators.add(resourceCalculator);
        }

        return resourceCalculators;
    }

    private boolean isCalculatingDone(){
        // TODO вместо isComplete обрабатывать службу запуска потоков
        return isComplete.get() && messageProcessor.isStopped();
    }

    private void setComplete(){
        logger.debug("Обработка ресурсов завершена");
        isComplete.set(true);
    }
}
