# PurrCollections

Paper 1.21+ — Kotlin — Hypixel-inspired collection progression system with rewards and tier unlocks.

Track items collected (mining, farming, combat, etc.) and unlock rewards at tier milestones. Integrates with PurrSkills for skill XP rewards.

## Features

- **Collection Tracking**: Automatically track materials gathered (ores, crops, mob drops, etc.)
- **Tier System**: Unlock progressive tiers by gathering more items
- **Rewards**: Earn rewards at each tier:
  - Stat Bonuses (damage, defense, etc.)
  - Skill XP (PurrSkills integration)
  - Recipe Unlocks (crafting recipes)
  - Item Rewards (custom items via PurrItems)
- **Collection Types**: Mining, Farming, Combat, Foraging
- **Persistent Storage**: HikariCP database via PurrCore
- **GUI**: Interactive collection menu with progress bars

## Commands

- `/collection` - Open your collections menu
- `/collection [player]` - View another player's collections (requires permission)

**Aliases**: `/collections`, `/c`

## Permissions

- `purrcollections.view` - View own collections (default: true)
- `purrcollections.view.others` - View other players' collections (default: op)
- `purrcollections.admin` - Admin commands and debugging (default: op)

## Collection Example

**Mining → Cobblestone:**
- Tier 1 (50): +5 coins
- Tier 2 (100): +1 Mining XP
- Tier 3 (250): +1% mining speed
- Tier 4 (500): Recipe unlock (Stone Pickaxe)
- ... up to Tier 10+

## Configuration

Collections are defined in code (see `Collections.kt` and collection-specific files like `MiningCollections.kt`). Future versions will support config-based collection definitions.

## Building

```bash
gradle shadowJar
# → build/libs/PurrCollections-1.0.0.jar
```

Requires `PurrCore` and `PurrSkills`. Optionally integrates with `PurrItems` for item rewards.

## Testing

```bash
gradle test
# 124 unit tests covering domain, progression, rewards, registry, idempotency
```

Coverage: 58% (30/51 files) - all critical business logic tested.

## Development

Collections are registered via `CollectionRegistry`. Progression calculated by `ProgressionCalculator`. Rewards executed by `RewardExecutor` with handler pattern for different reward types.

See `src/main/kotlin/gay/nyaa/purrcollections/collections/` for collection definitions.
