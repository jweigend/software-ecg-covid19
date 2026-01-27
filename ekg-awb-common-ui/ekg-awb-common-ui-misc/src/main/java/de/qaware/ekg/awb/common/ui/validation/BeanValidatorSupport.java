//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.validation;

import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validation Support combines the javax.validation with the validation strategies of controls fx.
 */
public class BeanValidatorSupport extends ValidationSupport {

    @Inject
    private Validator validator;


    public BeanValidatorSupport(){
        int i = 0;
    }

    /**
     * Register a new validation for control <code>c</code> that validates the property <code>propertyName</code> in the
     * given model.
     *
     * @param c            The control that should show the message.
     * @param model        The validation model
     * @param propertyName The property to validate.
     * @param <T>          The type of the model
     * @return true if the validation was successful.
     */
    public <T> boolean registerValidation(Control c, T model, String propertyName) {
        return registerValidator(c, (control, o) -> validateControl(c, model, propertyName));
    }

    /**
     * Perform the validation of a control.
     *
     * @param c            The control to validate.
     * @param model        The validation model.
     * @param propertyName The property to validate.
     * @param <T>          The type of the model.
     * @return A {@link ValidationResult} that contains the results of validation.
     */
    private <T> ValidationResult validateControl(Control c, T model, String propertyName) {
        Set<ValidationMessage> validationMessages = validator.validateProperty(model, propertyName).stream()
                .map(violation -> ValidationMessage.error(c, violation.getMessage()))
                .collect(Collectors.toSet());
        if (validationMessages.size() == 0) {
            return new ValidationResult();
        }
        return ValidationResult.fromMessages(validationMessages);
    }
}
