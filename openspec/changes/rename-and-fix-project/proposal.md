# Proposal: Rename and Fix Project

## Intent
Fix decompiled codebase syntax errors to make the project compile with `mvn clean install` and restructure package/class names to match the `hytale-groups-plugin` style reference.

## Scope

### In Scope
- Relocate package from `com.hytale.extendedteleport` to `dev.mpesteban.hytale.plugin.extendedteleport`.
- Rename main class `Main.java` to `ExtendedTeleportsPlugin.java`.
- Delete redundant decompiled inner-class source files (`TeleporterCommand$*.java` and `TeleporterManager$PlacementCheckResult.java`).
- Fix java compilation issues (primarily missing commas in asynchronous executor lambdas in `TeleporterCommand.java`).
- Update `manifest.json` and `pom.xml` configurations.
- Verify compiling with `mvn clean compile` and package with `mvn clean install`.

### Out of Scope
- Adding new game features.
- Modifying game behavior limits (limit of 9999, history of 9999).
- Re-implementing logic from scratch.

## Capabilities

### New Capabilities
None

### Modified Capabilities
None

## Approach
1. Move files from `src/main/java/com/hytale/extendedteleport` to `src/main/java/dev/mpesteban/hytale/plugin/extendedteleport`.
2. Rename `Main.java` to `ExtendedTeleportsPlugin.java`.
3. In `dev/mpesteban/hytale/plugin/extendedteleport/commands`, delete all `TeleporterCommand$*.java` files.
4. Delete `TeleporterManager$PlacementCheckResult.java`.
5. Update `package` declarations and imports in all migrated java files.
6. Fix compilation syntax errors in `TeleporterCommand.java`.
7. Update `manifest.json` with the new main class name and author.
8. Verify compile and package using Maven.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/resources/manifest.json` | Modified | Update Group, Name, Main class |
| `pom.xml` | Modified | Update Maven compiler settings |
| `src/main/java/dev/mpesteban/hytale/plugin/extendedteleport/` | New | All Java files relocated here |
| `src/main/java/com/hytale/` | Removed | Old package structure deleted |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Package refactoring breaks import references | Low | Standard IDE refactoring or thorough regex search/replace |
| Unresolved syntax errors in decompiled code | Medium | Correct code snippets iteratively and verify with compiler |
| Incompatible manifest metadata | Low | Verify properties align with game format |

## Rollback Plan
Discard git changes to return to original state: `git reset --hard HEAD` and `git clean -fd`.

## Dependencies
None.

## Success Criteria
- [ ] Code compiles successfully via `mvn clean compile`
- [ ] JAR package builds successfully via `mvn clean install`
- [ ] Main plugin class references are fully updated in `manifest.json`
