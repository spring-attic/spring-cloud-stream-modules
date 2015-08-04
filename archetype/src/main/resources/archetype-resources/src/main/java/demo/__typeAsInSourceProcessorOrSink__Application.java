#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import ${package}.${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink};

@SpringBootApplication
@ComponentScan(basePackageClasses= ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}.class)
public class ${typeAsInSourceProcessorOrSink}Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(${typeAsInSourceProcessorOrSink}Application.class, args);
	}

}
