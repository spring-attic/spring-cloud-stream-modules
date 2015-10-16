#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * A main application that can be used to run the ${capitalizedNameOfModuleWithoutType} ${typeAsInSourceProcessorOrSink} as a standalone app.
 */
@SpringBootApplication
public class ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}Application.class, args);
	}

}
