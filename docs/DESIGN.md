# Cosmic Agent - Design Log

Kotlin rewrite of `Blended-XP/src/questManager/CosmosAgent` (C#). Reference only: understand it, then write it fresh.

## Decisions made

| # | Decision | Why |
|---|----------|-----|
| D1 | Kotlin + Gradle, built in Android Studio | Official docs/samples are Kotlin; better debugging |
| D2 | Device admin only (no device owner) | Device owner is not realistic on Quest right now; admin covers lock + uninstall protection |
| D3 | Java SignalR client (`com.microsoft.signalr`) | Same hub (`HeadsetControlHub`), no server change |
| D4 | App switching happens only on SignalR commands from LocalServer | No on-device app-switch logic except the new auto-launch |
| D5 | Package `com.blendedxr.gls.agent` (namespace + applicationId match). Old agent deprovisioned, so no in-place upgrade needed | Ghostlight Studios naming |
| D6 | Project lives at `~/AndroidStudioProjects/ControlAgent` (not Cosmic_Agent) | Where the IDE created it |
| D7 | Own private repo for now; later copy/clone into Blended-XP and delete the C# CosmosAgent files | Keeps the rewrite isolated until it reaches parity |

## Goal beyond parity

Same behaviour as the C# agent, plus: auto-launch a default app after boot and keep it in place (configurable).

## Stages

1. Skeleton installs on headset, shows in Unknown Sources
2. Permissions + capability screen + provisioning script (adb grants)
3. Boot receiver + foreground service, survives reboot
4. Hub connection (Hello, reconnect, telemetry)
5. Commands (Launch, GoHome, Lock) + accessibility service
6. Configurability (settings, auto-launch default app)

## Open questions (ask when the stage arrives)

| # | Question | Stage | Status |
|---|----------|-------|--------|
| Q1 | Accessibility service vs. exact alarm / SYSTEM_ALERT_WINDOW for background app launch. Accessibility works today and launches quietly; test alternatives, keep if they are not better | 2 | Open: leaning keep accessibility |
| Q2 | Foreground-app detection: window events + usage-stats polling (5s) today. Is it needed? Cheaper option? Efficiency exploration | 4-5 | Open |
| Q3 | Config delivery: adb broadcast today (needs PC). Should config come from the hub so auto-launch app can change remotely? | 6 | Open |
| Q4 | Boot ordering: when is Quest ready to launch a VR app after BOOT_COMPLETED? (wait for Wi-Fi / shell) | 3 | Open |
| Q5 | Does Quest kill the foreground service in sleep/standby? Wake lock / keepalive needed? | 3 | Open |
| Q6 | `testOnly` and signing for production; Linux-friendly keystore location | 1 / later | Open |
| Q7 | "Keep it in place": how to enforce the default app (relaunch on exit? only when idle? operator override?) | 6 | Open |
| Q8 | minSdk: template says 34, old agent 29. Test headset reports SDK 34. Is the whole fleet on 34+? Lower minSdk only if older headsets exist | 1 | Open |
| Q9 | UI toolkit for the status screen: Compose (template) vs plain Views. XR/spatial libs are to be removed either way | 1 | Open |

## Closed questions

| # | Question | Answer |
|---|----------|--------|
| Q10 | Where does the repo live? | Own private repo for now, merged into Blended-XP later (D7) |
