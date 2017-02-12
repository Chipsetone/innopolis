package com.semakin.parsers;

import com.semakin.exceptions.InnerResourceException;
import com.semakin.validation.ValidSymbols;
import com.semakin.validation.StringAsNumberValidator;

/**
 * Преобразователь строки в целое число
 * @author Виктор Семакин
 */
public class StringConverter {
    private StringAsNumberValidator stringAsNumberValidator;

    /**
     * @param stringAsNumberValidator валидатор преобразуемой строки
     */
    public StringConverter(StringAsNumberValidator stringAsNumberValidator){
        this.stringAsNumberValidator = stringAsNumberValidator;
    }

    /**
     * Преобразует строку в целое число
     * @param value строка
     * @return целое число
     * @throws InnerResourceException когда строка не удовлетворяет условиям валидатора
     */
    public int toInt(String value) throws InnerResourceException {
        value = value.replace(ValidSymbols.hyphen, ValidSymbols.minus);
        value = value.trim();

        if(stringAsNumberValidator.isNumber(value)){
            try {
                return Integer.parseInt(value);
            }
            catch(NumberFormatException ex){
                throw new InnerResourceException("Не удалось распознать число: " + value, ex);
            }
        }

        throw new InnerResourceException("Не удалось распознать число: " + value);
    }
}
