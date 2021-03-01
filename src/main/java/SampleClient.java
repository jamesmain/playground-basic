import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;

public class SampleClient {

	private static final Logger logger = LoggerFactory.getLogger(SampleClient.class.getName());

    public static void main(String[] theArgs) throws IOException {

    	logger.info("Executing.");

    	// Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();

        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

        List<String> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get("surnames.txt"))) {
            list = stream
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        	logger.error("Exception caught.", e);
        }

        for (int i = 1; i <= 3; i++) {
    		if (i > 1) {
    			sleep(1000);  // sleep for 1 sec between runs
    		}
    		if (i == 3) {
    			// The third time the loop of 20 searches is run, the searches should be performed with caching disabled.
    			AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
    			interceptor.addHeaderValue("Cache-Control", "no-cache");
    			client.registerInterceptor(interceptor);
    		}
            MyClientInterceptor myClientInterceptor = new MyClientInterceptor();
            client.registerInterceptor(myClientInterceptor);
            
            list.forEach(surname -> {
                /* Bundle response = */ client
                        .search()
                        .forResource("Patient")
                        .where(Patient.FAMILY.matches().value(surname))
                        .returnBundle(Bundle.class)
                        .execute();
                
            });

    		logger.info("Run #{} average response time: {}ms", i, myClientInterceptor.getAvgResponseTimeMillis());
            client.unregisterInterceptor(myClientInterceptor);
        }
        
    	logger.info("Done.");
    }
    
    private static void sleep(long time) {
		try {
			Thread.sleep(time);
		}
		catch (InterruptedException ignored) {    				
		}
    }
}

class MyPatient implements Comparable<MyPatient> {
	private String firstNames;
	private String lastName;
	private String birthDate;
	public String getFirstNames() {
		return firstNames;
	}
	public void setFirstNames(String firstNames) {
		this.firstNames = firstNames;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}
	@Override
	public int compareTo(MyPatient o) {
		return firstNames.compareTo(o.firstNames);
	}
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("First name(s): ");
		buf.append(firstNames);
		buf.append("\tLast name: ").append(lastName);
		buf.append("\tBirth date: ").append(birthDate == null ? "N/A" : birthDate);
		return buf.toString();
	}
}

class MyClientInterceptor implements IClientInterceptor {
    int numResponses;
    long totalResponseTimeMillis;
	public void interceptRequest(IHttpRequest theRequest) {
	}
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		++numResponses;
		totalResponseTimeMillis += theResponse.getRequestStopWatch().getMillis();		
	}
	public long getAvgResponseTimeMillis() {
		return numResponses > 0 ? totalResponseTimeMillis / numResponses : 0L;
	}		
}
