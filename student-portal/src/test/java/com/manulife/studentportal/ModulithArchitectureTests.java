package com.manulife.studentportal;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith architecture verification tests.
 * Validates module boundaries, dependencies, and structural integrity.
 */
class ModulithArchitectureTests {

    private final ApplicationModules modules = ApplicationModules.of(StudentPortalApplication.class);

    /**
     * Verifies that the modular structure follows Spring Modulith conventions:
     * - All modules are properly defined
     * - Module dependencies are allowed (via @ApplicationModule annotations)
     * - No cycles exist
     * - Internal packages are not accessed from outside modules
     */
    @Test
    void verifiesModularStructure() {
        System.out.println("\n========================================");
        System.out.println("SPRING MODULITH ARCHITECTURE VERIFICATION");
        System.out.println("========================================\n");

        // This will throw an exception if any violations are found
        modules.verify();

        System.out.println("✓ All module boundaries verified successfully!");
    }

    /**
     * Prints the detected modules and their dependencies for inspection.
     */
    @Test
    void printsModuleStructure() {
        System.out.println("\n========================================");
        System.out.println("DETECTED MODULES");
        System.out.println("========================================\n");

        modules.forEach(module -> {
            System.out.println("Module: " + module.getName());
            System.out.println("  Display Name: " + module.getDisplayName());
            System.out.println("  Base Package: " + module.getBasePackage().getName());

            var dependencies = module.getDependencies(modules);
            System.out.println("  Dependencies: " +
                (dependencies.isEmpty() ? "none" : dependencies.stream()
                    .map(dep -> dep.getTargetModule().getName())
                    .toList()));
            System.out.println();
        });
    }

    /**
     * Generates PlantUML documentation for the module structure.
     */
    @Test
    void generateModuleDocumentation() {
        System.out.println("\n========================================");
        System.out.println("GENERATING MODULE DOCUMENTATION");
        System.out.println("========================================\n");

        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();

        System.out.println("✓ Documentation generated in target/spring-modulith-docs/");
    }
}
