package com.baeldung.jiralite.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.baeldung.jiralite")
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories = noClasses().that().areAnnotatedWith(RestController.class)
        .should().dependOnClassesThat().areAssignableTo(Repository.class);
}
