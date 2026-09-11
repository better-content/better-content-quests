# Better Content Quests

Forge 1.20.1 integration mod owning the gameplay criteria and two custom task types used by Better Content's secondary FTB Quests ledger.

## Verification

Run `./gradlew verifyFull stageRuntimeJar` before committing. `verifyFast` runs deterministic resource checks and the GameTest evidence validator's negative controls. `verifyFull` also runs all three production GameTests: actual enchantment predicates, supported criterion dispatch into real FTB team/task progress, and criterion task configuration through NBT and network serialization.

The criterion fixture uses isolated in-memory FTB quest and team instances and restores the server instances before returning. It verifies team isolation, unrelated/unsupported criteria, and harmless repeat delivery without loading or saving the pack's quest files.

Each server run uses a fresh `build/gametest/<run-token>/` directory and retains its world, logs, and `execution.json`. The reviewed `gametest/profiles/full.txt` must exactly match both runtime discovery and successful completion. Missing, stale, empty, partial, duplicate, or failed evidence fails the task. Preserve failed run directories for diagnosis; `clean` removes build evidence.
