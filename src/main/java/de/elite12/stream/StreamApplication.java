package de.elite12.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StreamApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(StreamApplication.class, args);
        }
        catch (Exception e) {
            //Shutdown JVM on Spring Context initialization Errror
            System.exit(-1);
        }
    }

}
