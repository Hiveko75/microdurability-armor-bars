# microDurability Armor Bars

Tiny durability bars for your worn armor, drawn just above the hotbar (boots at the bottom,
helmet at the top), for Minecraft 26.1.2+ (Fabric).

A stripped-down version of **microDurability** by pajic: it keeps only the armor bars, with the
same position and look, and drops the low-durability warning and the config screen, so it needs
nothing but Fabric API.

- Bars use the vanilla durability colour and length.
- They move up when you have XP levels, and are hidden in creative/spectator mode and with F1.
- Undamaged armor shows a full bar.

Replaces microDurability; don't install both.

## Building

Requires JDK 25: `./gradlew build` (the jar is written to `build/libs/`). Every push to `main`
is also built by GitHub Actions.

## License

MIT. Based on microDurability by pajic (MIT); the icon is from microDurability.
