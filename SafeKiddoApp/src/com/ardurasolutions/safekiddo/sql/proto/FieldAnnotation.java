package com.ardurasolutions.safekiddo.sql.proto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface FieldAnnotation {
	/**
	 * extra dane do zapytania CREATE TABLE ..., np: DEFAULT NULL
	 * @return
	 */
    public String extra() default "";
    
    /**
     * to pole okre�la czy kolumna ma by� wyko�ystywana tylko lokalnie 
     * czy ma by� te� zapisywana po stronie servera
     * @return boolean <i>default false</i>
     */
    public boolean onlyLocal() default false;
    
    /**
     * pole nie istniejące w bazie danych, wynika tylko z konstrukcji zapytania SQL<br>
     * lub wykożystywane do przechowania dodatkowych informacji
     * @return boolean <i>default false</i>
     */
    public boolean virtualField() default false;
    
    /**
     * klucz główny
     * @return boolean <i>default false</i>
     */
    public boolean isPrimaryKey() default false;
    
    /**
     * system fields, not used in data deisplay
     * @return boolean <i>default false</i>
     */
    public boolean isGeneratedField() default false;
}
