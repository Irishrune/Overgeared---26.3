# Fabric 26.3 port: initial full compiler baseline

Date: November 3, 2026.

## Verified milestones

- Confirmed write access to this repository and imported the modified Phase 9 source from the pinned Overgeared 1.21.1 multiloader branch.
- Verified the compressed source delta against the complete local Phase 9 archive before import.
- GitHub Actions installed Java 25, Gradle 9.8.2 and Fabric Loom 1.19.3.
- All isolated regression tests completed successfully in the CI build.
- Fixed the obsolete access-widener `named` namespace; Minecraft setup now succeeds far enough to reach Java compilation.

## First real compilation

[Open the first Java compile job](https://github.com/Irishrune/Overgeared---26.3/actions/runs/41719838509).

The `:fabric:compileJava` task fails. The compiler reported 100 errors (its default maximum) across roughly 26 source files before truncating diagnostics. That count is **not** the total number of incompatibilities.

Observed source categories:
- Old villager profession and trade APIs
- Moved or removed Fabric lifecycle, fuel, item-group and screen-handler APIs
- Removed or reorganized client recipe-book categories and their mixins
- Old potion brewing, interaction-result and loot-constant symbols
- Advancement criteria trigger API changes
- Arrow entity and rendering generics
- Remaining annotation/dependency imports and Forge Config API Port call sites

## Next steps

1. Update and validate Fabric platform initialization and event wiring against the 26.3 APIs.
2. Update villager/trade and advancement integration.
3. Adapt recipe-book mixins and client UI to modern Minecraft classes.
4. Port and test the custom entities/renderers, remaining Mixins and data/asset formats.
5. Keep repeating Java 25 builds, then run dedicated-server and client smoke tests.

**Status: source tests passing, complete mod compilation failing, no installable JAR.**
Do not run this revision in a valued survival world.
