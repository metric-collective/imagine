package co.deepthought.imagine.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The base class for any service, utilized by the server.
 * @param <InputType>
 * @param <OutputType>
 */
public abstract class BaseService<InputType extends Validated, OutputType> {

    public BaseService() {}

    public static class FailureOutput {
        public String error;
        public String status;
        public FailureOutput(final String error) {
            this.status = "failure";
            this.error = error;
        }
    }

    public String handleJson(final String inputJson) {
        final Gson gson = new Gson();
        final InputType inputObject;
        try {
            inputObject = this.validateInput(inputJson);
            final OutputType outputObject = this.handle(inputObject);
            return gson.toJson(outputObject);
        }
        catch (final ServiceFailure failure) {
            final FailureOutput output = new FailureOutput(failure.getMessage());
            return gson.toJson(output);
        }
    }

    public InputType validateInput(final String inputJson) throws ServiceFailure {
        final Gson gson = new Gson();
        try {
            final InputType inputObject = gson.fromJson(inputJson, this.getInputClass());
            if(inputObject == null) {
                throw new ServiceFailure("no payload");
            }
            else {
                inputObject.validate();
                return inputObject;
            }
        }
        catch (final JsonSyntaxException failure) {
            throw new ServiceFailure(failure.getMessage());
        }
    }

    public abstract Class<InputType> getInputClass();
    public abstract OutputType handle(final InputType inputObject) throws ServiceFailure;

}