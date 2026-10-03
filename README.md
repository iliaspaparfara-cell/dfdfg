# Utility Mod (Fabric, MC 1.21.11)

## Get the .jar
Option A - no setup (GitHub):
  1. Create a new GitHub repo, upload everything in this folder (keep .github/).
  2. Open the Actions tab -> "Build mod jar" -> wait ~3 min.
  3. Download the "utility-mod-jar" artifact. Unzip it: utility-mod-1.0.0.jar is the mod.

Option B - local:
  Install JDK 21 + Gradle 8.14, then run:  gradle build
  Jar ends up in build/libs/utility-mod-1.0.0.jar (not the -sources one).

## Install
Fabric Loader 0.18+ and Fabric API 0.141+ for 1.21.11, drop the jar in .minecraft/mods.

Controls: Right Shift opens the GUI. Left click toggles / drags sliders, right click expands settings.
Modules: Auto Mace (swap + smash while falling), Silent Aim (hold attack, hits nearest target without turning).
