package com.demo.auth_code_flow.projects;

import com.demo.auth_code_flow.projects.dto.CreateProjectRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProjectRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMissingProjectNumberAndName() {
        CreateProjectRequest request = new CreateProjectRequest(" ", null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("projectNumber", "name");
    }
}
