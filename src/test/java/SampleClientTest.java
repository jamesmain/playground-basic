import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleClientTest {

	private static final Logger logger = LoggerFactory.getLogger(SampleClientTest.class.getName());

	@Test
	public void testReadFile() throws Exception {
		logger.info("Executing");

        List<String> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get("surnames.txt"))) {
            list = stream
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        	logger.error("Exception caught.", e);
        }

		assertEquals(20, list.size());
	}

}
