# Just a World Map

A Fabric client mod for Minecraft 1.21.11.

The map records chunks as they load while you explore. Unvisited areas appear dim. Waypoints support icon, block, and text tags, with highlighted block positions.

## Build

Install JDK 21 and Gradle, then run `gradle build`. The mod jar is created in `build/libs`.

## Controls

- `M`: Open the map
- `N`: Add a waypoint at your current position

The map uses the current world seed to identify the world and keeps its discovered chunk map and waypoints locally per world. Since Minecraft does not expose safe generation of arbitrary client-side chunks, the seed identifies and separates map data; terrain details become visible as chunks load.
