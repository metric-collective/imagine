package co.deepthought.imagine.service;

import com.google.gson.Gson;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BaseServiceTest {

    public static class TestService extends BaseService<TestService.Input, TestService.Output> {

        @Override
        public Class<Input> getInputClass() {
            return Input.class;
        }

        @Override
        public Output handle(Input inputObject) {
            final Output output = new Output();
            output.status = "OK-" + inputObject.name;
            return output;
        }

        public static class Input extends Validated {
            public String name;
            public int value;
            public Input() {}

            @Override
            public void validate() throws ServiceFailure {
                this.validateNonNull(this.name, "name");
            }
        }

        public static class Output {
            public String status;
            public Output() {}
        }

    }

    @Test
    public void testHandleJson() {
        final String inputString = "{\"name\":\"randall\",\"value\":21}";
        final TestService service = new TestService();
        final String outputString = service.handleJson(inputString);
        final Gson gson = new Gson();
        final TestService.Output output = gson.fromJson(outputString, TestService.Output.class);
        assertEquals(output.status, "OK-randall");
    }

    @Test
    public void testHandleJsonError() {
        final String inputString = "{\"value\":21}";
        final TestService service = new TestService();
        final String outputString = service.handleJson(inputString);
        final Gson gson = new Gson();
        final BaseService.FailureOutput output = gson.fromJson(outputString, BaseService.FailureOutput.class);
        assertEquals("failure", output.status);
        assertEquals("name must be non-null", output.error);
    }

    @Test
    public void testValidateInputValid() throws ServiceFailure {
        final String inputString = "{\"name\":\"randall\",\"value\":21}";
        final TestService service = new TestService();
        final TestService.Input input = service.validateInput(inputString);
        assertEquals(input.name, "randall");
        assertEquals(input.value, 21);
    }

    @Test(expected = ServiceFailure.class)
    public void testValidateInputInvalidBadType() throws ServiceFailure {
        final String inputString = "{\"name\":\"randl\",\"value\":\"prabce\"}";
        final TestService service = new TestService();
        service.validateInput(inputString);
    }

    @Test(expected = ServiceFailure.class)
    public void testValidateInputInvalidMissing() throws ServiceFailure {
        final String inputString = "{\"value\":21}";
        final TestService service = new TestService();
        service.validateInput(inputString);
    }

    @Test(expected = ServiceFailure.class)
    public void testValidateInputInvalidJson() throws ServiceFailure {
        final String inputString = "{\"name\":\"randall\",\"value\":";
        final TestService service = new TestService();
        service.validateInput(inputString);
    }

}
