package io.setl.iobc.valid;

import static io.setl.common.StringUtils.logSafe;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.stereotype.Component;

import io.setl.iobc.model.ErrorDetails;
import io.setl.json.CJArray;
import io.setl.json.CJObject;

/**
 * Support bean validation.
 *
 * @author Simon Greatrix on 23/09/2020.
 */
@Component
public class ObjectValidator {

  private final Validator javaValidator;


  public ObjectValidator(Validator javaValidator) {
    this.javaValidator = javaValidator;
  }


  /**
   * Validate an object.
   *
   * @param value the object to validate
   *
   * @return the error details if the object is invalid
   */
  public ErrorDetails validate(Object value) {
    Set<ConstraintViolation<Object>> violationSet = javaValidator.validate(value);
    if (violationSet.isEmpty()) {
      // all good
      return null;
    }

    CJArray cjArray = new CJArray(violationSet.size());
    for (ConstraintViolation<Object> violation : violationSet) {
      cjArray.add(logSafe(violation.getMessage()) + " @ " + logSafe(violation.getPropertyPath().toString()));
    }

    CJObject cjObject = new CJObject();
    cjObject.put("violations", cjArray);
    cjObject.put("type", value.getClass().getName());

    return ErrorDetails.builder()
        .code("iobc/invalid_data")
        .message("The input data in invalid")
        .parameters(cjObject)
        .build();
  }

}
