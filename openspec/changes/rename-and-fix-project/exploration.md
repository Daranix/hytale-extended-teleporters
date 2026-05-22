## Exploration: Rename and Fix Project

### Current State
- The project `hytale-extended-teleports` is a decompiled Hytale plugin using Maven.
- Package structure: `com.hytale.extendedteleport`
- Main class: `Main` (`com.hytale.extendedteleport.Main`)
- The project contains duplicate `.java` files for nested static classes (e.g., `TeleporterCommand$*.java` and `TeleporterManager$PlacementCheckResult.java`).
- There are compilation errors due to decompilation artifacts, particularly missing commas in asynchronous executor lambdas (e.g., `}(Executor)world` instead of `}, (Executor)world` or `}(Executor)world` syntax errors).

### Affected Areas
- `pom.xml` — Verify Maven compiler versions and configurations.
- `src/main/resources/manifest.json` — Update Group, Name, and Main class.
- All `.java` files in `src/main/java/com/hytale/extendedteleport` — Relocate to package `dev.mpesteban.hytale.plugin.extendedteleport`, rename `Main.java` to `ExtendedTeleportsPlugin.java`, delete redundant class files (`TeleporterCommand$*.java` and `TeleporterManager$PlacementCheckResult.java`), and fix decompilation syntax errors.

### Approaches
1. **Full Relocation & Decompiled Code Cleanup (Recommended)**
   - Relocate the code to `dev.mpesteban.hytale.plugin.extendedteleport`.
   - Rename main class to `ExtendedTeleportsPlugin`.
   - Delete duplicate/redundant inner class files (`TeleporterCommand$*.java`, `TeleporterManager$PlacementCheckResult.java`).
   - Fix syntax issues in `TeleporterCommand.java` (specifically missing commas on lambda executor references).
   - Update config references.
   - Pros: Cleans up the code structure, conforms to the reference project pattern, and resolves compilation errors.
   - Cons: Large number of files moved.
   - Effort: Medium.

### Recommendation
Follow Approach 1. This matches the user's explicit request to change class names, package structure, reference the config folder, and fix compiling issues.

### Risks
- Package migration errors (missing imports or incorrect package lines in migrated files).
- Remaining syntax errors in other decompiled files.
- Run-time configuration mismatches.
- We will mitigate these via a verification build of the project using `mvn clean compile`.

### Ready for Proposal
Yes.
